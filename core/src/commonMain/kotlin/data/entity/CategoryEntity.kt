package data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import model.transaction.TransactionCategory

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String
)

fun CategoryEntity.toDomain() = TransactionCategory(id, name)
fun TransactionCategory.toEntity() = CategoryEntity(id, name)