package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import models.AccountUiModel
import models.TransactionUiModel
import models.toUiModel
import service.AccountService
import service.CategoryService
import service.TransactionService

data class AccountDetailsState(
    val account: AccountUiModel? = null,
    val transactions: List<TransactionUiModel> = emptyList(),
    val isLoading: Boolean = true
)

class AccountDetailsScreenModel(
    private val accountId: String,
    private val accountService: AccountService,
    private val transactionService: TransactionService,
    private val categoryService: CategoryService
) : ScreenModel {

    private val _state = MutableStateFlow(AccountDetailsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            val acc = accountService.getAccountById(accountId)
            if (acc != null) {
                _state.value = _state.value.copy(account = acc.toUiModel())

                combine(
                    transactionService.getAccountTransactionsFlow(accountId),
                    categoryService.getAllCategoriesFlow()
                ) { trans, cats ->
                    trans.sortedByDescending { it.base.date }.map { it.toUiModel(cats) }
                }.collect { list ->
                    _state.value = _state.value.copy(transactions = list, isLoading = false)
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}