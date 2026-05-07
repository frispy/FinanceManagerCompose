package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.enum.CurrencyType
import models.CategoryAnalyticUiItem
import models.TimeAnalyticUiItem
import models.toUiModel
import service.AnalyticsService
import service.CategoryService
import service.TransactionService
import service.analytics.CategoryExpenseStrategy
import service.analytics.CategoryIncomeStrategy
import service.analytics.TimeLineStrategy

data class AnalyticsState(
    val categoryExpenseReport: List<CategoryAnalyticUiItem> = emptyList(),
    val categoryIncomeReport: List<CategoryAnalyticUiItem> = emptyList(),
    val timeReport: List<TimeAnalyticUiItem> = emptyList(),
    val reportCurrency: CurrencyType = CurrencyType.USD,
    val isLoading: Boolean = true
)

class AnalyticsScreenModel(
    private val userId: String,
    private val transactionService: TransactionService,
    private val categoryService: CategoryService,
    private val analyticsService: AnalyticsService
) : ScreenModel {

    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            combine(
                transactionService.getUserTransactionsFlow(userId),
                categoryService.getAllCategoriesFlow()
            ) { userTrans, allCats ->
                val currentCurrency = _state.value.reportCurrency

                val catExpenseStrategy = CategoryExpenseStrategy(allCats)
                val catIncomeStrategy = CategoryIncomeStrategy(allCats)
                val timeStrategy = TimeLineStrategy()

                // get domain models from service
                val domainExpenseReport = analyticsService.analyze(userTrans, catExpenseStrategy, currentCurrency)
                val domainIncomeReport = analyticsService.analyze(userTrans, catIncomeStrategy, currentCurrency)
                val domainTimeReport = analyticsService.analyze(userTrans, timeStrategy, currentCurrency)

                AnalyticsState(
                    categoryExpenseReport = domainExpenseReport.map { it.toUiModel(currentCurrency.name) },
                    categoryIncomeReport = domainIncomeReport.map { it.toUiModel(currentCurrency.name) },
                    timeReport = domainTimeReport.map { it.toUiModel(currentCurrency.name) },
                    reportCurrency = currentCurrency,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun setCurrency(currency: CurrencyType) {
        _state.update { it.copy(reportCurrency = currency, isLoading = true) }
    }
}