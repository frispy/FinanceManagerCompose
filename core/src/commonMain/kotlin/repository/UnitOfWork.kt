package repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import data.AppDatabase

// generic transaction management
interface UnitOfWork {
    suspend fun <R> execute(block: suspend () -> R): R
}
class RoomUnitOfWork(private val database: AppDatabase) : UnitOfWork {
    override suspend fun <R> execute(block: suspend () -> R): R {
        // in KMP useWriterConnection instead of Android's withTransaction
        return database.useWriterConnection { transactor ->
            transactor.immediateTransaction { // rollbacks automatically
                block()
            }
        }
    }
}