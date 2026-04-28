package repository

import kotlinx.coroutines.flow.Flow
import model.account.Account

interface AccountRepository : BaseRepository<Account, String?> {
    fun getAllAccountsFlow(): Flow<List<Account>>
    suspend fun delete(id: String)
}