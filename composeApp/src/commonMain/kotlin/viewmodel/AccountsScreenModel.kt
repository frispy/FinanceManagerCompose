package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import model.transaction.Transaction
import models.AccountUiModel
import models.TransactionUiModel
import models.toUiModel
import service.AccountService
import service.CategoryService
import service.TransactionService

data class AccountWithHistory(
    val account: AccountUiModel,
    val recentTransactions: List<TransactionUiModel>
)

data class AccountsState(
    val accounts: List<AccountWithHistory> = emptyList()
)

class AccountsScreenModel(
    private val userId: String,
    private val accountService: AccountService,
    private val transactionService: TransactionService,
    private val categoryService: CategoryService
) : ScreenModel {

    private val _state = MutableStateFlow(AccountsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            combine(
                accountService.getUserAccountsFlow(userId),
                transactionService.getUserTransactionsFlow(userId),
                categoryService.getAllCategoriesFlow()
            ) { accs, trans, cats ->
                accs.map { acc ->
                    val history = trans.filter { it.accountId == acc.id || (it is Transaction.Transfer && it.targetAccountId == acc.id) }
                        .sortedByDescending { it.base.date }
                        .take(3)
                        .map { it.toUiModel(cats) }

                    AccountWithHistory(acc.toUiModel(), history)
                }
            }.collect { list ->
                _state.value = AccountsState(accounts = list)
            }
        }
    }
}