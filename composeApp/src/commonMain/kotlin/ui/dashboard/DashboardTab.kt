package ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import model.account.Account
import model.account.BankAccount
import model.transaction.Transaction
import ui.theme.LocalCurrentUser
import viewmodel.DashboardScreenModel

object DashboardTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 0u, title = "Dashboard")

    @Composable
    override fun Content() {
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            DashboardScreenModel(
                userId = user.id,
                accountRepository = AppDependencies.accountRepository, // Assuming these exist in AppDependencies
                transactionRepository = AppDependencies.transactionRepository,
                categoryRepository = AppDependencies.categoryRepository
            )
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text(
                text = "Welcome, ${user.login}",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("My Accounts", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // horizontal row for accounts
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.topAccounts.isEmpty()) {
                    Text("No accounts yet. Create one!", color = Color.Gray)
                } else {
                    state.topAccounts.forEach { account ->
                        AccountCard(account, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // recent transactions block
                Card(
                    modifier = Modifier.weight(1.5f).fillMaxHeight(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recent Transactions", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                            Button(onClick = {}, colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)) {
                                Text("View all", color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (state.recentTransactions.isEmpty()) {
                            Text("No transactions yet.", color = Color.Gray)
                        } else {
                            LazyColumn {
                                items(state.recentTransactions) { tx ->
                                    TransactionRow(tx)
                                }
                            }
                        }
                    }
                }

                // custom categories block
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Custom Categories", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                        Text("Create custom tags to track your spending habits.", color = Color.Gray, style = MaterialTheme.typography.body2)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.activeCategories.isEmpty()) {
                            Text("No categories yet.", color = Color.Gray)
                        } else {
                            // flow layout is tricky in pure basic compose without accompanist,
                            // we'll just use a column of simple text items for simplicity here
                            LazyColumn {
                                items(state.activeCategories) { cat ->
                                    // extract pure name if we encoded type/icon inside
                                    val pureName = cat.name.split("|").first()
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        backgroundColor = Color.LightGray.copy(alpha = 0.5f),
                                        elevation = 0.dp
                                    ) {
                                        Text(pureName, modifier = Modifier.padding(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AccountCard(account: Account, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier.height(160.dp),
            backgroundColor = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            elevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(account.accountType.name, color = Color.Gray, style = MaterialTheme.typography.caption)
                    val accName = if (account is BankAccount) account.bankName else "Account"
                    Text(accName, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                }
                Text("${account.currency} ${account.balance}", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Account ->", color = Color.White)
                }
            }
        }
    }

    @Composable
    fun TransactionRow(tx: Transaction) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Box(modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(tx.base.note.ifBlank { "Transaction" }, fontWeight = FontWeight.Bold)
                    Text(tx.transactionType.name, color = Color.Gray, style = MaterialTheme.typography.caption)
                }
            }
            Text(
                text = "${if (tx is Transaction.Expense) "-" else "+"}${tx.base.amount} ${tx.base.currency}",
                fontWeight = FontWeight.Bold,
                color = if (tx is Transaction.Expense) Color.Black else Color(0xFF4CAF50)
            )
        }
    }
}