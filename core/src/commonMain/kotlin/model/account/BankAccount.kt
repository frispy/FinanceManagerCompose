package model.account

import model.enum.AccountType
import model.enum.CurrencyType
import kotlinx.serialization.Serializable

@Serializable
data class BankAccount(
    override val userId: String,
    override val name: String,
    override val balance: Long = 0,
    override val currency: CurrencyType,
    override val note: String,
    val bankName: String,
    override val id: String
) : Account() {
    override val accountType: AccountType = AccountType.BANK

    override fun updateBalance(newBalance: Long): Account {
        return this.copy(balance = newBalance)
    }
}