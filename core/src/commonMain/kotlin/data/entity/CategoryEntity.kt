package data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import model.enum.TransactionType
import model.transaction.TransactionCategory

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: TransactionType,
    val iconName: String
)

fun CategoryEntity.toDomain() = TransactionCategory(id, name, type, iconName)
fun TransactionCategory.toEntity() = CategoryEntity(id, name, type, iconName)