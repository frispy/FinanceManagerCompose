package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.account.Account
import model.account.BankAccount
import model.account.CashAccount
import model.account.DepositAccount
import repository.AccountRepository
import service.AccountService

data class EditAccountState(
    val account: Account? = null,
    val name: String = "",
    val note: String = "",

    // specific fields mapped
    val bankName: String = "",
    val cashLocation: String = "",
    val dailyLimit: String = "",
    val interestRate: String = "",

    val isLoading: Boolean = true,
    val error: String? = null
)

class EditAccountScreenModel(
    private val accountId: String,
    private val accountRepository: AccountRepository,
    private val accountService: AccountService
) : ScreenModel {

    private val _state = MutableStateFlow(EditAccountState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            val acc = accountRepository.getById(accountId)
            if (acc != null) {
                _state.update {
                    it.copy(
                        account = acc,
                        name = acc.name,
                        note = acc.note,
                        bankName = if (acc is BankAccount) acc.bankName else "",
                        cashLocation = if (acc is CashAccount) acc.cashLocation else "",
                        dailyLimit = if (acc is CashAccount) acc.dailyLimit.toString() else "",
                        interestRate = if (acc is DepositAccount) acc.interestRate.toString() else "",
                        isLoading = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, error = "account not found") }
            }
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(name = name) }
    fun onNoteChange(note: String) = _state.update { it.copy(note = note) }
    fun onBankNameChange(bankName: String) = _state.update { it.copy(bankName = bankName) }
    fun onCashLocationChange(location: String) = _state.update { it.copy(cashLocation = location) }
    fun onDailyLimitChange(limit: String) = _state.update { it.copy(dailyLimit = limit) }
    fun onInterestRateChange(rate: String) = _state.update { it.copy(interestRate = rate) }

    fun updateAccount(onSuccess: () -> Unit) {
        val st = _state.value
        val acc = st.account ?: return

        // apply fields based on class instance
        val updatedAcc = when (acc) {
            is BankAccount -> acc.copy(name = st.name, note = st.note, bankName = st.bankName)
            is CashAccount -> acc.copy(name = st.name, note = st.note, cashLocation = st.cashLocation, dailyLimit = st.dailyLimit.toLongOrNull() ?: 0L)
            is DepositAccount -> acc.copy(name = st.name, note = st.note, interestRate = st.interestRate.toDoubleOrNull() ?: 0.0)
        }

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            accountService.updateAccount(updatedAcc)
            _state.update { it.copy(isLoading = false) }
            onSuccess()
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            accountService.deleteAccount(accountId)
            onSuccess()
        }
    }
}