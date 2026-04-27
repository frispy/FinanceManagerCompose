package service

import kotlinx.coroutines.flow.Flow
import model.transaction.TransactionCategory
import repository.CategoryRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun createCategory(name: String): TransactionCategory? {
        // check for duplicates
        val existing = categoryRepository.findByName(name)
        if (existing != null) {
            return null
        }

        // generate new id and object
        val newCategory = TransactionCategory(
            id = Uuid.random().toString(),
            name = name
        )

        // save to file
        categoryRepository.add(newCategory)
        return newCategory
    }

    // fetch all stored categories
    suspend fun getAllCategories(): Flow<List<TransactionCategory>> {
        return categoryRepository.getAllCategoriesFlow()
    }

    // delete specific category
    suspend fun deleteCategory(categoryId: String) {
        categoryRepository.delete(categoryId)
    }
}