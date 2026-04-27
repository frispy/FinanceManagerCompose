package model.params

import model.enum.CurrencyType

sealed class AccountCreationParams {
    abstract val userId: String
    abstract val name: String
    abstract val initBalance: Long
    abstract val currency: CurrencyType
    abstract val note: String

    data class Bank(
        override val userId: String,
        override val name: String,
        override val initBalance: Long,
        override val currency: CurrencyType,
        override val note: String,
        val bankName: String
    ) : AccountCreationParams()

    data class Cash(
        override val userId: String,
        override val name: String,
        override val initBalance: Long,
        override val currency: CurrencyType,
        override val note: String,
        val cashLocation: String,
        val dailyLimit: Long = 0
    ) : AccountCreationParams()

    data class Deposit(
        override val userId: String,
        override val name: String,
        override val initBalance: Long,
        override val currency: CurrencyType,
        override val note: String,
        val interestRate: Double
    ) : AccountCreationParams()
}