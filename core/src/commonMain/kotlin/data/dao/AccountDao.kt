package data.dao

import androidx.room.Dao
import androidx.room.Query
import data.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao : BaseDao<AccountEntity> {
    @Query("SELECT * FROM accounts")
    fun getAllFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String?): AccountEntity?

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteById(id: String)
}