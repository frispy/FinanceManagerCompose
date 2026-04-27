package data

import androidx.room.Database
import androidx.room.RoomDatabase
import data.dao.*
import data.entity.*

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        TransactionEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}