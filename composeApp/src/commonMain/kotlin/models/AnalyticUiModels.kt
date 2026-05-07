package models

data class CategoryAnalyticUiItem(
    val categoryId: String?,
    val categoryName: String,
    val iconName: String,
    val displayAmount: String,
    val displayPercentage: String,
    val progress: Float
)

data class TimeAnalyticUiItem(
    val periodLabel: String,
    val displayIncome: String,
    val displayExpense: String,
    val rawIncome: Long,
    val rawExpense: Long     
)