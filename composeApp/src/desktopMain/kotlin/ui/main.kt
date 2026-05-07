package ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import data.AppDatabase
import AppDependencies
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import app.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import utils.TestDataSeeder
import java.io.File

fun main() {
    // 1. create DB file
    val dbFile = File("FinanceManager.db")

    // 2. initialize database
    val database = Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
        .setDriver(BundledSQLiteDriver()) // KMP Room driver for desktop
        .setQueryCoroutineContext(Dispatchers.IO)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    // 3. pass database to DI container
    AppDependencies.init(database)

    // 4. trigger the data seeder synchronously before ui mounts
    runBlocking {
        TestDataSeeder.seed()
    }

    // 4. launch UI
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "FinanceManagerCompose",
            state = rememberWindowState(placement = WindowPlacement.Maximized),
            resizable = false,
        ) {
            App()
        }
    }
}