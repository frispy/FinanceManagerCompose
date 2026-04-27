package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import repository.AccountRepository
import repository.CategoryRepository
import repository.TransactionRepository

class SettingsScreenModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ScreenModel {

    // iterates and deletes all user-related data (since UserDao lacks delete function)
    fun clearUserData(userId: String) {
        screenModelScope.launch {
            // we grab the flows temporarily just to get current state and delete
            accountRepository.getAllAccountsFlow().collect { accounts ->
                accounts.filter { it.userId == userId }.forEach { accountRepository.delete(it.id) }
            }
            transactionRepository.getAllTransactionsFlow().collect { transactions ->
                transactions.filter { it.base.userId == userId }.forEach { transactionRepository.delete(it.id) }
            }
            // keeping categories intact as they might be global, or delete them similarly
        }
    }
}