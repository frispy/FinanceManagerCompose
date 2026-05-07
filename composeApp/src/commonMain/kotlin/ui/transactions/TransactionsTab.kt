package ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import models.TransactionUiModel
import ui.theme.LocalCurrentUser
import viewmodel.TransactionsScreenModel

object TransactionsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 3u, title = "Transactions")

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.current?.parent
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            TransactionsScreenModel(
                user.id,
                AppDependencies.transactionService,
                AppDependencies.categoryService
            )
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("All Transactions", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { rootNavigator?.push(CreateTransactionScreen(user.id)) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("+ Add Transaction", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.transactions.isEmpty()) {
                Text("No transactions found.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(state.transactions.sortedByDescending { it.rawDate }) { tx ->
                        RemovableTransactionRow(
                            tx = tx,
                            onDelete = { screenModel.deleteTransaction(tx.id) }
                        )
                        Divider(color = Color.LightGray)
                    }
                }
            }
        }
    }

    @Composable
    fun RemovableTransactionRow(tx: TransactionUiModel, onDelete: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(ui.utils.IconMapper.getIconByName(tx.iconName), contentDescription = null, tint = MaterialTheme.colors.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(tx.note, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(tx.typeLabel, color = Color.Gray, style = MaterialTheme.typography.caption)
                        Text("•", color = Color.LightGray, style = MaterialTheme.typography.caption)
                        Text(tx.displayDate, color = Color.Gray, style = MaterialTheme.typography.caption)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tx.displayAmount,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.isExpense) Color.Black else Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
    }
}