package models

data class AccountUiModel(
    val id: String,
    val displayName: String,
    val displayBalance: String,
    val accountTypeLabel: String,
    val note: String
)