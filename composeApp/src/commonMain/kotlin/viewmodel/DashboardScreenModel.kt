package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import models.AccountUiModel
import models.CategoryUiModel
import models.TransactionUiModel
import models.toUiModel
import service.AccountService
import service.CategoryService
import service.TransactionService

data class DashboardState(
    val isLoading: Boolean = true,
    val accounts: List<AccountUiModel> = emptyList(),
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val activeCategories: List<CategoryUiModel> = emptyList(),
    val currentDateTime: String = ""
)

class DashboardScreenModel(
    private val userId: String,
    private val accountService: AccountService,
    private val transactionService: TransactionService,
    private val categoryService: CategoryService
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
                delay(1000)
            }
        }
    }

    private fun loadDashboardData() {
        screenModelScope.launch {
            // observing flows allows real-time ui updates when db changes
            launch {
                accountService.getUserAccountsFlow(userId).collect { userAccs ->
                    _state.update {
                        it.copy(
                            accounts = userAccs.map { acc -> acc.toUiModel() }, // Load all accounts to allow scrolling
                            isLoading = false
                        )
                    }
                }
            }
            launch {
                combine(
                    transactionService.getUserTransactionsFlow(userId),
                    categoryService.getAllCategoriesFlow()
                ) { userTrans, allCats ->
                    userTrans
                        .sortedByDescending { it.base.date }
                        .take(20)
                        .map { tr -> tr.toUiModel(allCats) }
                }.collect { mappedTrans ->
                    _state.update { it.copy(recentTransactions = mappedTrans) }
                }
            }
            launch {
                categoryService.getAllCategoriesFlow().collect { allCats ->
                    _state.update { it.copy(activeCategories = allCats.take(20).map { cat -> cat.toUiModel() }) }
                }
            }
        }
    }
}