package repository

import kotlinx.coroutines.flow.Flow
import model.transaction.TransactionCategory

interface CategoryRepository {
    fun getAllCategoriesFlow(): Flow<List<TransactionCategory>>
    suspend fun getById(id: String): TransactionCategory?
    suspend fun findByName(name: String): TransactionCategory?
    suspend fun add(category: TransactionCategory)
    suspend fun update(category: TransactionCategory)
    suspend fun delete(id: String)
}

