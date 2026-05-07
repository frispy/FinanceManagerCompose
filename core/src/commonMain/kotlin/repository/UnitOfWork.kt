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
        // NOTE in KMP useWriterConnection instead of Androids withTransaction THATS VERY IMPORTANT FOR FUTURE PROJECTS
        val result = database.useWriterConnection { transactor ->
            transactor.immediateTransaction { // auto rollback
                block()
            }
        }

        // Manually trigger Room's invalidation tracker so Flows emit the new UI state
        database.invalidationTracker.refreshAsync()

        return result
    }
}