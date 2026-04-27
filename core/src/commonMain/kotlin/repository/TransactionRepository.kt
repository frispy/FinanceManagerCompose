package repository

import kotlinx.coroutines.flow.Flow
import model.transaction.Transaction


interface TransactionRepository {
    fun getAllTransactionsFlow(): Flow<List<Transaction>>
    suspend fun getById(id: String): Transaction?
    suspend fun getTransactionsByAccount(accountId: String): List<Transaction>
    fun getTransactionsByAccountFlow(accountId: String): Flow<List<Transaction>>
    suspend fun add(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(id: String)
}
