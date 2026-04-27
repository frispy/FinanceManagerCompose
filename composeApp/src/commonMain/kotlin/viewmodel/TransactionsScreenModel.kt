package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.transaction.Transaction
import repository.TransactionRepository

data class TransactionsState(val transactions: List<Transaction> = emptyList())

class TransactionsScreenModel(
    private val userId: String,
    private val transactionRepository: TransactionRepository
) : ScreenModel {
    private val _state = MutableStateFlow(TransactionsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            transactionRepository.getAllTransactionsFlow().collect { list ->
                _state.update { it.copy(transactions = list.filter { tr -> tr.base.userId == userId }) }
            }
        }
    }
}