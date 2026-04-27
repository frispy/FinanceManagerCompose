package model.params

import model.enum.CurrencyType

sealed class TransactionCreationParams {
    // shared data structure
    data class Common(
        val userId: String,
        val accountId: String,
        val amount: Long,
        val currency: CurrencyType,
        val date: String,
        val categoryId: String?,
        val note: String
    )

    data class Expense(val common: Common) : TransactionCreationParams()

    data class Income(val common: Common) : TransactionCreationParams()

    data class Transfer(
        val common: Common,
        val targetAccountId: String
    ) : TransactionCreationParams()
}