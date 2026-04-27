package model.account

import model.enum.AccountType
import model.enum.CurrencyType
import kotlinx.serialization.Serializable
import model.Identifiable

@Serializable
sealed class Account : Identifiable {
    abstract val userId: String
    abstract val name: String
    abstract val balance: Long
    abstract val currency: CurrencyType
    abstract val accountType: AccountType
    abstract val note: String
    abstract override val id: String

    abstract fun updateBalance(newBalance: Long): Account
}