package data.dao

import androidx.room.*
import data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao : BaseDao<TransactionEntity> {
    @Query("SELECT * FROM transactions")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE accountId = :accountId")
    suspend fun getByAccountId(accountId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId")
    fun getByAccountIdFlow(accountId: String): Flow<List<TransactionEntity>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}