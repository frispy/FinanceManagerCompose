package repository

import data.dao.CategoryDao
import data.entity.toDomain
import data.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.transaction.TransactionCategory

class CategoryRepositoryImpl(private val dao: CategoryDao): CategoryRepository {
    override fun getAllCategoriesFlow(): Flow<List<TransactionCategory>> {
        return dao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: String): TransactionCategory? {
        return dao.getById(id)?.toDomain()
    }
    override suspend fun findByName(name: String): TransactionCategory? {
        return dao.findByName(name)?.toDomain()
    }

    override suspend fun add(category: TransactionCategory) {
        dao.insert(category.toEntity())
    }

    override suspend fun update(category: TransactionCategory) {
        dao.update(category.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}