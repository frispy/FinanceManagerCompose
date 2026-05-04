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
import repository.CategoryRepository
import repository.TransactionRepository
import service.AnalyticsService
import service.analytics.CategoryExpenseStrategy
import service.analytics.TimeLineStrategy

data class AnalyticsState(
    val categoryReport: List<CategoryAnalyticUiItem> = emptyList(),
    val timeReport: List<TimeAnalyticUiItem> = emptyList(),
    val reportCurrency: CurrencyType = CurrencyType.USD,
    val isLoading: Boolean = true
)

class AnalyticsScreenModel(
    private val userId: String,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val analyticsService: AnalyticsService
) : ScreenModel {

    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            combine(
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllCategoriesFlow()
            ) { allTrans, allCats ->
                val userTrans = allTrans.filter { it.base.userId == userId }
                val currentCurrency = _state.value.reportCurrency

                val catStrategy = CategoryExpenseStrategy(allCats)
                val timeStrategy = TimeLineStrategy()

                // get domain models from service
                val domainCategoryReport = analyticsService.analyze(userTrans, catStrategy, currentCurrency)
                val domainTimeReport = analyticsService.analyze(userTrans, timeStrategy, currentCurrency)

                AnalyticsState(
                    categoryReport = domainCategoryReport.map { it.toUiModel(currentCurrency.name) },
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