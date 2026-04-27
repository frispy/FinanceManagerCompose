package data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import model.user.User
import model.transaction.TransactionCategory

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val login: String,
    val passwordHash: ByteArray
)

fun UserEntity.toDomain() = User(id, login, passwordHash)
fun User.toEntity() = UserEntity(id, login, passwordHash)
