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
import repository.CategoryRepository
import repository.TransactionRepository
import service.TransactionService

data class TransactionsState(
    val transactions: List<TransactionUiModel> = emptyList(),
    val categories: List<CategoryUiModel> = emptyList()
)

class TransactionsScreenModel(
    private val userId: String,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionService: TransactionService
) : ScreenModel {
    private val _state = MutableStateFlow(TransactionsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            launch {
                combine(
                    transactionRepository.getAllTransactionsFlow(),
                    categoryRepository.getAllCategoriesFlow()
                ) { trans, cats ->
                    trans.filter { tr -> tr.base.userId == userId }.map { it.toUiModel(cats) }
                }.collect { mappedTrans ->
                    _state.update { it.copy(transactions = mappedTrans) }
                }
            }
            launch {
                categoryRepository.getAllCategoriesFlow().collect { list ->
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