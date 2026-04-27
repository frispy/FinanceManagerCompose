package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.transaction.Transaction
import model.transaction.TransactionCategory
import repository.CategoryRepository
import repository.TransactionRepository
import service.TransactionService

data class TransactionsState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<TransactionCategory> = emptyList()
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
                transactionRepository.getAllTransactionsFlow().collect { list ->
                    _state.update { it.copy(transactions = list.filter { tr -> tr.base.userId == userId }) }
                }
            }
            launch {
                categoryRepository.getAllCategoriesFlow().collect { list ->
                    _state.update { it.copy(categories = list) }
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