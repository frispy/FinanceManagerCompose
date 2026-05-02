package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.enum.AccountType
import model.enum.CurrencyType
import model.params.AccountCreationParams
import service.AccountService


data class CreateAccountState(
    val name: String = "",
    val note: String = "",
    val balance: String = "",
    val currency: CurrencyType = CurrencyType.USD,
    val accountType: AccountType = AccountType.BANK,
    // specific fields
    val bankName: String = "",
    val cashLocation: String = "",
    val dailyLimit: String = "",
    val interestRate: String = "",

    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateAccountScreenModel(
    private val userId: String,
    private val accountService: AccountService
) : ScreenModel {

    private val _state = MutableStateFlow(CreateAccountState())
    val state = _state.asStateFlow()

    // basic fields updates
    fun onNameChange(name: String) = _state.update { it.copy(name = name) }
    fun onNoteChange(note: String) = _state.update { it.copy(note = note) }
    fun onBalanceChange(balance: String) = _state.update { it.copy(balance = balance) }
    fun onCurrencyChange(currency: CurrencyType) = _state.update { it.copy(currency = currency) }
    fun onAccountTypeChange(type: AccountType) = _state.update { it.copy(accountType = type) }

    // specific fields updates
    fun onBankNameChange(bankName: String) = _state.update { it.copy(bankName = bankName) }
    fun onCashLocationChange(location: String) = _state.update { it.copy(cashLocation = location) }
    fun onDailyLimitChange(limit: String) = _state.update { it.copy(dailyLimit = limit) }
    fun onInterestRateChange(rate: String) = _state.update { it.copy(interestRate = rate) }

    fun saveAccount(onSuccess: () -> Unit) {
        val currentState = _state.value

        // simple validation
        if (currentState.name.isBlank()) {
            _state.update { it.copy(error = "account name cannot be empty") }
            return
        }

        val initialBalance = currentState.balance.toLongOrNull() ?: 0L

        val params = when (currentState.accountType) {
            AccountType.BANK -> AccountCreationParams.Bank(
                userId = userId,
                name = currentState.name,
                note = currentState.note,
                initBalance = initialBalance,
                currency = currentState.currency,
                bankName = currentState.bankName.ifBlank { "My Bank" }
            )
            AccountType.CASH -> AccountCreationParams.Cash(
                userId = userId,
                name = currentState.name,
                note = currentState.note,
                initBalance = initialBalance,
                currency = currentState.currency,
                cashLocation = currentState.cashLocation.ifBlank { "Wallet" },
                dailyLimit = currentState.dailyLimit.toLongOrNull() ?: 0L
            )
            AccountType.DEPOSIT -> AccountCreationParams.Deposit(
                userId = userId,
                name = currentState.name,
                note = currentState.note,
                initBalance = initialBalance,
                currency = currentState.currency,
                interestRate = currentState.interestRate.toDoubleOrNull() ?: 0.0
            )
        }

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val success = accountService.createAccount(params)

            if (success) {
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _state.update { it.copy(isLoading = false, error = "failed to create account") }
            }
        }
    }
}