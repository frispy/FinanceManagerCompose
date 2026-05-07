package service

import factory.GenericFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.account.Account
import model.enum.CurrencyType
import model.params.AccountCreationParams
import repository.AccountRepository

class AccountService (
    private val accountRepository: AccountRepository,
    private val accountFactory: GenericFactory<Account, AccountCreationParams>,
) {
    suspend fun createAccount(params: AccountCreationParams): Boolean {
        val account = accountFactory.create(params)
        accountRepository.add(account)
        return true
    }
    suspend fun updateAccount(account: Account): Boolean {
        accountRepository.update(account)
        return true
    }

    // fetch raw domain object from repo natively to assist calculations
    suspend fun getAccountById(accountId: String?): Account? {
        return accountRepository.getById(accountId)
    }

    suspend fun getAccountCurrency(accountId: String?): CurrencyType? {
        return accountRepository.getById(accountId ?: "")?.currency
    }

    // remove money
    suspend fun withdraw(accountId: String?, amount: Long): Boolean {
        if (accountId == null) return false
        val account = accountRepository.getById(accountId) ?: return false

        if (amount <= 0) {
            return false
        }

        val updatedAccount = account.updateBalance(account.balance - amount)

        accountRepository.update(updatedAccount)
        return true
    }

    // add money
    suspend fun deposit(accountId: String?, amount: Long): Boolean {
        if (accountId == null) return false
        val account = accountRepository.getById(accountId) ?: return false

        if (amount <= 0) {
            return false
        }

        val updatedAccount = account.updateBalance(account.balance + amount)

        accountRepository.update(updatedAccount)
        return true
    }

    fun getUserAccountsFlow(userId: String): Flow<List<Account>> {
        return accountRepository.getAllAccountsFlow().map { list ->
            list.filter { it.userId == userId }
        }
    }
    suspend fun deleteAccount(accountId: String) {
        accountRepository.delete(accountId)
    }

    fun getTotalBalanceFlow(userId: String): Flow<Long> {
        return accountRepository.getAllAccountsFlow().map { accountsList ->
            accountsList.filter { it.userId == userId }.sumOf { it.balance }
        }
    }

    suspend fun getConcreteBalance(accountId: String): String {
        return accountRepository.getById(accountId)?.balance?.toString() ?: "0"
    }
}