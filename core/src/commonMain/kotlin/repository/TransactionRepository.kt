package repository

import kotlinx.coroutines.flow.Flow
import model.transaction.Transaction


interface TransactionRepository : BaseRepository<Transaction, String> {
    fun getAllTransactionsFlow(): Flow<List<Transaction>>
    suspend fun getTransactionsByAccount(accountId: String): List<Transaction>
    fun getTransactionsByAccountFlow(accountId: String): Flow<List<Transaction>>
    suspend fun delete(id: String)
}