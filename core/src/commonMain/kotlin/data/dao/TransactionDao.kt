package data.dao
import androidx.room.*
import data.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE accountId = :accountId")
    suspend fun getByAccountId(accountId: String): List<TransactionEntity>

    // flow variant just in case
    @Query("SELECT * FROM transactions WHERE accountId = :accountId")
    fun getByAccountIdFlow(accountId: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: String)

    @Transaction
    suspend fun executeTransaction(
        transaction: TransactionEntity,
        accountDao: AccountDao,
        isIncome: Boolean,
        amount: Long
    ) {
        // 1. Insert the history record
        insert(transaction)

        // 2. Update account balance
        val account = accountDao.getById(transaction.accountId) ?: return
        val newBalance = if (isIncome) account.balance + amount else account.balance - amount
        accountDao.update(account.copy(balance = newBalance))
    }
}