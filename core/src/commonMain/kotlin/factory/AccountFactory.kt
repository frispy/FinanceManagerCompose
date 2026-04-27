package factory

import model.account.Account
import model.account.BankAccount
import model.account.CashAccount
import model.account.DepositAccount
import model.params.AccountCreationParams
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AccountFactory : GenericFactory<Account, AccountCreationParams> {
    override fun create(params: AccountCreationParams): Account {
        val accountId = Uuid.random().toString()

        return when (params) {
            is AccountCreationParams.Bank -> {
                BankAccount(params.userId, params.name, params.initBalance, params.currency, params.note, params.bankName, accountId)
            }
            is AccountCreationParams.Cash -> {
                CashAccount(params.userId, params.name, params.initBalance, params.currency, params.note, params.cashLocation, params.dailyLimit, accountId)
            }
            is AccountCreationParams.Deposit -> {
                DepositAccount(params.userId, params.name, params.initBalance, params.currency, params.note, params.interestRate, accountId)
            }
        }
    }
}