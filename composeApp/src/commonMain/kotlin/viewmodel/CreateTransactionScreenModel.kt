package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.enum.CurrencyType
import model.enum.TransactionType
import model.params.TransactionCreationParams
import models.AccountUiModel
import models.CategoryUiModel
import models.toUiModel
import service.AccountService
import service.CategoryService
import service.TransactionService

data class CreateTransactionState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val note: String = "",
    val selectedCurrency: CurrencyType = CurrencyType.USD,
    val selectedAccountId: String = "",
    val selectedTargetAccountId: String = "",
    val selectedCategoryId: String = "",
    val availableAccounts: List<AccountUiModel> = emptyList(),
    val availableCategories: List<CategoryUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateTransactionScreenModel(
    private val userId: String,
    private val accountService: AccountService,
    private val categoryService: CategoryService,
    private val transactionService: TransactionService
) : ScreenModel {

    private val _state = MutableStateFlow(CreateTransactionState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            launch {
                accountService.getUserAccountsFlow(userId).collect { accounts ->
                    val userAccounts = accounts.map { it.toUiModel() }
                    _state.update {
                        it.copy(
                            availableAccounts = userAccounts,
                            selectedAccountId = it.selectedAccountId.ifEmpty { userAccounts.firstOrNull()?.id ?: "" }
                        )
                    }
                }
            }
            launch {
                categoryService.getAllCategoriesFlow().collect { categories ->
                    val uiCats = categories.map { it.toUiModel() }
                    _state.update {
                        it.copy(
                            availableCategories = uiCats,
                            selectedCategoryId = it.selectedCategoryId.ifEmpty { uiCats.firstOrNull()?.id ?: "" }
                        )
                    }
                }
            }
        }
    }

    fun onTypeChange(type: TransactionType) = _state.update { it.copy(selectedType = type) }
    fun onAmountChange(amount: String) = _state.update { it.copy(amount = amount) }
    fun onNoteChange(note: String) = _state.update { it.copy(note = note) }
    fun onCurrencyChange(currency: CurrencyType) = _state.update { it.copy(selectedCurrency = currency) }
    fun onAccountChange(id: String) = _state.update { it.copy(selectedAccountId = id) }
    fun onTargetAccountChange(id: String) = _state.update { it.copy(selectedTargetAccountId = id) }
    fun onCategoryChange(id: String) = _state.update { it.copy(selectedCategoryId = id) }

    fun submitTransaction(onSuccess: () -> Unit) {
        val st = _state.value
        val parsedAmount = st.amount.toLongOrNull() ?: 0L

        if (parsedAmount <= 0) {
            _state.update { it.copy(error = "invalid amount") }
            return
        }

        if (st.selectedType == TransactionType.TRANSFER && st.selectedAccountId == st.selectedTargetAccountId) {
            _state.update { it.copy(error = "Source and Target accounts must be different") }
            return
        }

        val commonParams = TransactionCreationParams.Common(
            userId = userId,
            accountId = st.selectedAccountId,
            amount = parsedAmount,
            currency = st.selectedCurrency,
            date = "",
            categoryId = st.selectedCategoryId.ifBlank { null },
            note = st.note
        )

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val success = when (st.selectedType) {
                TransactionType.EXPENSE -> transactionService.expense(TransactionCreationParams.Expense(commonParams))
                TransactionType.INCOME -> transactionService.income(TransactionCreationParams.Income(commonParams))
                TransactionType.TRANSFER -> transactionService.transfer(TransactionCreationParams.Transfer(commonParams, st.selectedTargetAccountId))
            }

            if (success) {
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _state.update { it.copy(isLoading = false, error = "Transaction failed") }
            }
        }
    }
}