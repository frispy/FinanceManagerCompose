package models

import model.account.Account
import model.account.BankAccount
import model.transaction.Transaction
import model.transaction.TransactionCategory
import model.user.User

fun Account.toUiModel(): AccountUiModel {
    val accName = if (this is BankAccount) this.bankName else this.name
    return AccountUiModel(
        id = this.id,
        displayName = accName,
        displayBalance = "${this.currency} ${this.balance}",
        accountTypeLabel = this.accountType.name,
        note = this.note
    )
}

fun TransactionCategory.toUiModel(): CategoryUiModel {
    return CategoryUiModel(
        id = this.id,
        name = this.name,
        type = this.type,
        iconName = this.iconName
    )
}

fun Transaction.toUiModel(categories: List<TransactionCategory>): TransactionUiModel {
    val category = categories.find { it.id == this.base.categoryId }
    val isExpense = this is Transaction.Expense
    val sign = if (isExpense) "-" else "+"
    return TransactionUiModel(
        id = this.id,
        note = this.base.note.ifBlank { "Transaction" },
        displayAmount = "$sign${this.base.amount} ${this.base.currency}",
        displayDate = this.base.date.substringBefore(".").replace("T", " "),
        rawDate = this.base.date,
        typeLabel = this.transactionType.name,
        categoryId = this.base.categoryId,
        iconName = category?.iconName ?: "Category",
        isExpense = isExpense
    )
}

fun User.toUiModel(): UserUiModel {
    return UserUiModel(
        id = this.id,
        login = this.login
    )
}