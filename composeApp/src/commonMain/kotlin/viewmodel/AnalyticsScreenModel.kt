package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.enum.CurrencyType
import model.transaction.Transaction
import models.CategoryAnalyticUiItem
import models.TimeAnalyticUiItem
import models.toUiModel
import service.AccountService
import service.AnalyticsService
import service.CategoryService
import service.CurrencyExchangeService
import service.TransactionService
import service.analytics.CategoryExpenseStrategy
import service.analytics.CategoryIncomeStrategy
import service.analytics.TimeLineStrategy

data class AccountFlowUiItem(
    val accountName: String,
    val totalFlowRaw: Long, // Combined absolute volume of cash moved
    val displayFlow: String
)

data class AnalyticsState(
    val categoryExpenseReport: List<CategoryAnalyticUiItem> = emptyList(),
    val categoryIncomeReport: List<CategoryAnalyticUiItem> = emptyList(),
    val timeReport: List<TimeAnalyticUiItem> = emptyList(),
    val accountFlowReport: List<AccountFlowUiItem> = emptyList(),
    val reportCurrency: CurrencyType = CurrencyType.USD,
    val isLoading: Boolean = true
)

class AnalyticsScreenModel(
    private val userId: String,
    private val transactionService: TransactionService,
    private val categoryService: CategoryService,
    private val analyticsService: AnalyticsService,
    private val accountService: AccountService,
    private val currencyExchangeService: CurrencyExchangeService
) : ScreenModel {

    private val selectedCurrencyFlow = MutableStateFlow(CurrencyType.USD)

    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            combine(
                transactionService.getUserTransactionsFlow(userId),
                categoryService.getAllCategoriesFlow(),
                accountService.getUserAccountsFlow(userId),
                selectedCurrencyFlow
            ) { userTrans, allCats, userAccs, currentCurrency ->

                val catExpenseStrategy = CategoryExpenseStrategy(allCats)
                val catIncomeStrategy = CategoryIncomeStrategy(allCats)
                val timeStrategy = TimeLineStrategy()

                val domainExpenseReport = analyticsService.analyze(userTrans, catExpenseStrategy, currentCurrency)
                val domainIncomeReport = analyticsService.analyze(userTrans, catIncomeStrategy, currentCurrency)
                val domainTimeReport = analyticsService.analyze(userTrans, timeStrategy, currentCurrency)

                val accountFlows = userAccs.map { acc ->
                    val accTrans = userTrans.filter { it.accountId == acc.id || (it is Transaction.Transfer && it.targetAccountId == acc.id) }
                    var totalVolume = 0L

                    accTrans.forEach { tx ->
                        val converted = currencyExchangeService.convert(tx.amount, tx.currency, currentCurrency)
                        totalVolume += converted
                    }

                    AccountFlowUiItem(
                        accountName = acc.name,
                        totalFlowRaw = totalVolume,
                        displayFlow = "$totalVolume ${currentCurrency.name}"
                    )
                }.sortedByDescending { it.totalFlowRaw } // Highest flow volume first

                AnalyticsState(
                    categoryExpenseReport = domainExpenseReport.map { it.toUiModel(currentCurrency.name) },
                    categoryIncomeReport = domainIncomeReport.map { it.toUiModel(currentCurrency.name) },
                    timeReport = domainTimeReport.map { it.toUiModel(currentCurrency.name) },
                    accountFlowReport = accountFlows,
                    reportCurrency = currentCurrency,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun setCurrency(currency: CurrencyType) {
        // triggers the combine flow and immediately puts ui into loading state
        _state.update { it.copy(isLoading = true) }
        selectedCurrencyFlow.value = currency
    }
}