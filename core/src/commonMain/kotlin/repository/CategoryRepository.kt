package repository

import kotlinx.coroutines.flow.Flow
import model.transaction.TransactionCategory

interface CategoryRepository : BaseRepository<TransactionCategory, String> {
    fun getAllCategoriesFlow(): Flow<List<TransactionCategory>>
    suspend fun findByName(name: String): TransactionCategory?
    suspend fun delete(id: String)
}