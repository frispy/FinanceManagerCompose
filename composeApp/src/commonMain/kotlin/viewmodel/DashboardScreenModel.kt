package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.account.Account
import model.transaction.Transaction
import model.transaction.TransactionCategory
import repository.AccountRepository
import repository.CategoryRepository
import repository.TransactionRepository

data class DashboardState(
    val isLoading: Boolean = true,
    val topAccounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val activeCategories: List<TransactionCategory> = emptyList()
)

class DashboardScreenModel(
    private val userId: String,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        screenModelScope.launch {
            // observing flows allows real-time UI updates when DB changes
            launch {
                accountRepository.getAllAccountsFlow().collect { allAccs ->
                    _state.update {
                        it.copy(
                            topAccounts = allAccs.filter { acc -> acc.userId == userId }.take(3),
                            isLoading = false
                        )
                    }
                }
            }
            launch {
                transactionRepository.getAllTransactionsFlow().collect { allTrans ->
                    _state.update {
                        it.copy(recentTransactions = allTrans.filter { tr -> tr.base.userId == userId }.take(5))
                    }
                }
            }
            launch {
                categoryRepository.getAllCategoriesFlow().collect { allCats ->
                    _state.update { it.copy(activeCategories = allCats.take(6)) }
                }
            }
        }
    }
}