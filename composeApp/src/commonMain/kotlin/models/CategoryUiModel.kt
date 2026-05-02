package models

import model.enum.TransactionType

data class CategoryUiModel(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconName: String
)