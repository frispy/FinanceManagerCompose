package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import models.CategoryUiModel
import models.TransactionUiModel
import models.toUiModel
import service.CategoryService
import service.TransactionService

data class TransactionsState(
    val transactions: List<TransactionUiModel> = emptyList(),
    val categories: List<CategoryUiModel> = emptyList()
)

class TransactionsScreenModel(
    private val userId: String,
    private val transactionService: TransactionService,
    private val categoryService: CategoryService
) : ScreenModel {
    private val _state = MutableStateFlow(TransactionsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            launch {
                combine(
                    transactionService.getUserTransactionsFlow(userId),
                    categoryService.getAllCategoriesFlow()
                ) { trans, cats ->
                    trans.map { it.toUiModel(cats) }
                }.collect { mappedTrans ->
                    _state.update { it.copy(transactions = mappedTrans) }
                }
            }
            launch {
                categoryService.getAllCategoriesFlow().collect { list ->
                    _state.update { it.copy(categories = list.map { it.toUiModel() }) }
                }
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        screenModelScope.launch {
            transactionService.deleteTransactionRecord(transactionId)
        }
    }
}