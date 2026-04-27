package model.transaction

import kotlinx.serialization.Serializable
import model.Identifiable
import model.enum.TransactionType

@Serializable
data class TransactionCategory(
    override val id: String,
    val name: String,
    val type: TransactionType,
    val iconName: String
) : Identifiable