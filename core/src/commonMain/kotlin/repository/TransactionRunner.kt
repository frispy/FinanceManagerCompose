package repository

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import data.AppDatabase

// generic transaction management
interface TransactionRunner {
    suspend fun <R> execute(block: suspend () -> R): R
}

class RoomTransactionRunner(private val database: AppDatabase) : TransactionRunner {
    override suspend fun <R> execute(block: suspend () -> R): R {
        // NOTE in KMP useWriterConnection instead of Androids withTransaction THATS VERY IMPORTANT FOR FUTURE PROJECTS
        val result = database.useWriterConnection { transactor ->
            transactor.immediateTransaction { // auto rollback
                block()
            }
        }

        // make sure that ui reacts on change in db
        database.invalidationTracker.refreshAsync()

        return result
    }
}