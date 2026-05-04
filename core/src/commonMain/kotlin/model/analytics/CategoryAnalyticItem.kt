package model.analytics

data class CategoryAnalyticItem(
    val categoryId: String?,
    val categoryName: String,
    val iconName: String,
    val totalAmount: Long,
    val percentage: Float // from 0.0 to 1.0 for graphs
)