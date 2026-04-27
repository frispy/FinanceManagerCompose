package data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import model.account.Account
import model.account.BankAccount
import model.account.CashAccount
import model.account.DepositAccount
import model.enum.AccountType
import model.enum.CurrencyType

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val userId: String,
    val balance: Long,
    val currency: CurrencyType,
    val accountType: AccountType,
    val note: String,
    // specific fields
    val bankName: String? = null,
    val cashLocation: String? = null,
    val dailyLimit: Long? = null,
    val interestRate: Double? = null
)

// map to model
fun AccountEntity.toDomain(): Account = when (accountType) {
    AccountType.BANK -> BankAccount(userId, name, balance, currency, note, bankName ?: "", id)
    AccountType.CASH -> CashAccount(userId, name, balance, currency, note, cashLocation ?: "", dailyLimit ?: 0L, id)
    AccountType.DEPOSIT -> DepositAccount(userId, name, balance, currency, note, interestRate ?: 0.0, id)
}

// map to entity
fun Account.toEntity(): AccountEntity = when (this) {
    is BankAccount -> AccountEntity(id, name, userId, balance, currency, accountType, note, bankName = bankName)
    is CashAccount -> AccountEntity(id, name, userId, balance, currency, accountType, note, cashLocation = cashLocation, dailyLimit = dailyLimit)
    is DepositAccount -> AccountEntity(id, name, userId, balance, currency, accountType, note, interestRate = interestRate)
}