package repository

import data.dao.AccountDao
import data.entity.toDomain
import data.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.account.Account

class AccountRepositoryImpl(private val dao: AccountDao): AccountRepository {
    override fun getAllAccountsFlow(): Flow<List<Account>> {
        return dao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: String?): Account? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun add(account: Account) {
        dao.insert(account.toEntity())
    }

    override suspend fun update(account: Account) {
        dao.update(account.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}