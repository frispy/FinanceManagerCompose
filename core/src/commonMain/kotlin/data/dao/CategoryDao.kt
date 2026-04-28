package data.dao

import androidx.room.*
import data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<CategoryEntity> {
    @Query("SELECT * FROM categories")
    fun getAllFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): CategoryEntity?

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)
}