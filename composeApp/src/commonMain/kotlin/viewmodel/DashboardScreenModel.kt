package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
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
    val activeCategories: List<TransactionCategory> = emptyList(),
    val currentDateTime: String = ""
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
        startClock()
    }

    // localized clock updater formatting ISO strings natively to keep it readable
    private fun startClock() {
        screenModelScope.launch {
            while (true) {
                val rawTime = kotlin.time.Clock.System.now().toString()
                // strips milliseconds and replaces the "T" divider with a standard space
                val formattedTime = rawTime.substringBefore(".").replace("T", " ")

                _state.update { it.copy(currentDateTime = formattedTime) }
                delay(1000) // tick every second
            }
        }
    }

    private fun loadDashboardData() {
        screenModelScope.launch {
            // observing flows allows real-time ui updates when db changes
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