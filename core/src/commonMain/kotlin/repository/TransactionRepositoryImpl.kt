package repository


import data.dao.TransactionDao
import data.entity.toDomain
import data.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.transaction.Transaction

class TransactionRepositoryImpl(private val dao: TransactionDao): TransactionRepository {

    override fun getAllTransactionsFlow(): Flow<List<Transaction>> {
        return dao.getAllFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: String): Transaction? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun getTransactionsByAccount(accountId: String): List<Transaction> {
        return dao.getByAccountId(accountId).map { it.toDomain() }
    }

    // variant for flow
    override fun getTransactionsByAccountFlow(accountId: String): Flow<List<Transaction>> {
        return dao.getByAccountIdFlow(accountId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun add(transaction: Transaction) {
        dao.insert(transaction.toEntity())
    }

    override suspend fun update(transaction: Transaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}