package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.account.Account
import repository.AccountRepository

data class AccountsState(
    val accounts: List<Account> = emptyList()
)

class AccountsScreenModel(
    private val userId: String,
    private val accountRepository: AccountRepository
) : ScreenModel {

    private val _state = MutableStateFlow(AccountsState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            accountRepository.getAllAccountsFlow().collect { allAccs ->
                _state.update { it.copy(accounts = allAccs.filter { acc -> acc.userId == userId }) }
            }
        }
    }
}