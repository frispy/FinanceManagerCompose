package models

data class TransactionUiModel(
    val id: String,
    val note: String,
    val displayAmount: String,
    val displayDate: String,
    val rawDate: String,
    val typeLabel: String,
    val categoryId: String?,
    val iconName: String,
    val isExpense: Boolean
)