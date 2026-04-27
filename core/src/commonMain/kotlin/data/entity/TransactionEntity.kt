package data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import model.enum.CurrencyType
import model.enum.TransactionType
import model.transaction.Transaction
import model.transaction.TransactionBase

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetAccountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    // indices improve performance for foreign keys
    indices = [
        Index("userId"),
        Index("accountId"),
        Index("targetAccountId"),
        Index("categoryId")
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val accountId: String,
    val amount: Long,
    val currency: CurrencyType,
    val date: String,
    val categoryId: String?,
    val note: String,
    val transactionType: TransactionType,
    // specific fields
    val targetAccountId: String? = null
)

// map to model
fun TransactionEntity.toDomain(): Transaction {
    val base = TransactionBase(
        id = id,
        userId = userId,
        accountId = accountId,
        amount = amount,
        currency = currency,
        date = date,
        categoryId = categoryId,
        note = note
    )

    return when (transactionType) {
        TransactionType.EXPENSE -> Transaction.Expense(base)
        TransactionType.INCOME -> Transaction.Income(base)
        TransactionType.TRANSFER -> Transaction.Transfer(
            base = base,
            targetAccountId = targetAccountId ?: ""
        )
    }
}

// map to entity
fun Transaction.toEntity(): TransactionEntity = when (this) {
    is Transaction.Expense -> TransactionEntity(
        id = base.id,
        userId = base.userId,
        accountId = base.accountId,
        amount = base.amount,
        currency = base.currency,
        date = base.date,
        categoryId = base.categoryId,
        note = base.note,
        transactionType = transactionType
    )
    is Transaction.Income -> TransactionEntity(
        id = base.id,
        userId = base.userId,
        accountId = base.accountId,
        amount = base.amount,
        currency = base.currency,
        date = base.date,
        categoryId = base.categoryId,
        note = base.note,
        transactionType = transactionType
    )
    is Transaction.Transfer -> TransactionEntity(
        id = base.id,
        userId = base.userId,
        accountId = base.accountId,
        amount = base.amount,
        currency = base.currency,
        date = base.date,
        categoryId = base.categoryId,
        note = base.note,
        transactionType = transactionType,
        targetAccountId = targetAccountId
    )
}