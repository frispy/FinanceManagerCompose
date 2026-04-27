package ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import ui.dashboard.DashboardTab.TransactionRow
import ui.theme.LocalCurrentUser
import viewmodel.TransactionsScreenModel

object TransactionsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 3u, title = "Transactions")

    @Composable
    override fun Content() {
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            TransactionsScreenModel(user.id, AppDependencies.transactionRepository)
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("All Transactions", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            if (state.transactions.isEmpty()) {
                Text("No transactions found.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(state.transactions) { tx ->
                        TransactionRow(tx)
                        Divider(color = Color.LightGray)
                    }
                }
            }
        }
    }
}