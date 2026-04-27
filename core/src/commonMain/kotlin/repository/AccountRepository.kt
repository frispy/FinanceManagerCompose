package repository

import kotlinx.coroutines.flow.Flow
import model.account.Account

interface AccountRepository {
    fun getAllAccountsFlow(): Flow<List<Account>>
    suspend fun getById(id: String?): Account?
    suspend fun add(account: Account)
    suspend fun update(account: Account)
    suspend fun delete(id: String)
}

