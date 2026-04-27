package model.transaction

import kotlinx.serialization.Serializable
import model.enum.CurrencyType
import model.Identifiable
import model.enum.TransactionType

@Serializable
data class TransactionBase(
    override val id: String,
    val userId: String,
    val accountId: String?,
    val amount: Long,
    val currency: CurrencyType,
    val date: String,
    val categoryId: String?,
    val note: String
) : Identifiable

@Serializable
sealed class Transaction : Identifiable {
    abstract val base: TransactionBase
    abstract val transactionType: TransactionType

    // delegate these properties so repository/service can see them
    override val id: String get() = base.id
    val accountId: String? get() = base.accountId
    val amount: Long get() = base.amount
    val currency: CurrencyType get() = base.currency

    @Serializable
    data class Expense(override val base: TransactionBase) : Transaction() {
        override val transactionType = TransactionType.EXPENSE
    }

    @Serializable
    data class Income(override val base: TransactionBase) : Transaction() {
        override val transactionType = TransactionType.INCOME
    }

    @Serializable
    data class Transfer(
        override val base: TransactionBase,
        val targetAccountId: String
    ) : Transaction() {
        override val transactionType = TransactionType.TRANSFER
    }
}