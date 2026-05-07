package ui.accounts

import AppDependencies
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
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
import viewmodel.AccountWithHistory
import viewmodel.AccountsScreenModel

object AccountsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 1u, title = "Accounts")

    @Composable
    override fun Content() {
        val rootNavigator = LocalNavigator.current?.parent
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            AccountsScreenModel(
                user.id,
                AppDependencies.accountService,
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
                Text("Accounts", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)

                Button(
                    onClick = {
                        rootNavigator?.push(CreateAccountScreen(user.id))
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("+ Add Account", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.accounts.isEmpty()) {
                Text("No accounts yet. Click the button above to create one!", color = Color.Gray)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 350.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.accounts) { item ->
                        DetailedAccountCard(
                            item = item,
                            onEditClick = { rootNavigator?.push(EditAccountScreen(item.account.id)) },
                            onHistoryClick = { rootNavigator?.push(AccountDetailsScreen(item.account.id)) }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DetailedAccountCard(item: AccountWithHistory, onEditClick: () -> Unit, onHistoryClick: () -> Unit) {
        val account = item.account

        Card(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            backgroundColor = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            elevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(account.accountTypeLabel, color = Color.Gray, style = MaterialTheme.typography.caption)
                Text(account.displayName, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))
                Text(account.displayBalance, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))
                Text(account.note.ifBlank { "No specific notes provided." }, color = Color.Gray, style = MaterialTheme.typography.body2)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Recent Transactions", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (item.recentTransactions.isEmpty()) {
                        Text("No local history available.", color = Color.Gray)
                    } else {
                        item.recentTransactions.forEach { tx ->
                            MiniTransactionRow(tx)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onHistoryClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Full history", color = Color.White)
                    }
                    OutlinedButton(onClick = onEditClick) {
                        Text("Edit", color = MaterialTheme.colors.primary)
                    }
                }
            }
        }
    }

    @Composable
    fun MiniTransactionRow(tx: TransactionUiModel) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(24.dp).background(Color.White, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(ui.utils.IconMapper.getIconByName(tx.iconName), contentDescription = null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(tx.note, style = MaterialTheme.typography.body2, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = tx.displayAmount,
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Bold,
                color = if (tx.isExpense) Color.Black else Color(0xFF4CAF50)
            )
        }
    }
}