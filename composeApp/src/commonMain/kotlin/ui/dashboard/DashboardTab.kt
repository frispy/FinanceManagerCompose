package ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import model.account.Account
import model.account.BankAccount
import model.transaction.Transaction
import model.transaction.TransactionCategory
import ui.accounts.AccountsTab
import ui.transactions.TransactionsTab
import ui.theme.LocalCurrentUser
import viewmodel.DashboardScreenModel

object DashboardTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 0u, title = "Dashboard")

    @Composable
    override fun Content() {
        val tabNavigator = LocalTabNavigator.current
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            DashboardScreenModel(
                userId = user.id,
                accountRepository = AppDependencies.accountRepository,
                transactionRepository = AppDependencies.transactionRepository,
                categoryRepository = AppDependencies.categoryRepository
            )
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Welcome, ${user.login}",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.currentDateTime,
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("My Accounts", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.topAccounts.isEmpty()) {
                    Text("No accounts yet. Create one!", color = Color.Gray)
                } else {
                    state.topAccounts.forEach { account ->
                        AccountCard(
                            account = account,
                            modifier = Modifier.weight(1f),
                            onClick = { tabNavigator.current = AccountsTab } // navigation
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.weight(1.5f).fillMaxHeight(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Recent Transactions", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { tabNavigator.current = TransactionsTab }, // navigation
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                            ) {
                                Text("View all", color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (state.recentTransactions.isEmpty()) {
                            Text("No transactions yet.", color = Color.Gray)
                        } else {
                            LazyColumn {
                                items(state.recentTransactions) { tx ->
                                    TransactionRow(tx, state.activeCategories) // pass categories for icons
                                }
                            }
                        }
                    }
                }

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
                            LazyColumn {
                                items(state.activeCategories) { cat ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        backgroundColor = Color.LightGray.copy(alpha = 0.5f),
                                        elevation = 0.dp
                                    ) {
                                        Text(cat.name, modifier = Modifier.padding(12.dp))
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
    fun AccountCard(account: Account, modifier: Modifier = Modifier, onClick: () -> Unit) {
        Card(
            modifier = modifier.height(160.dp),
            backgroundColor = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            elevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(account.accountType.name, color = Color.Gray, style = MaterialTheme.typography.caption)
                    val accName = if (account is BankAccount) account.bankName else account.name
                    Text(accName, style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                }
                Text("${account.currency} ${account.balance}", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("View Account", color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }

    @Composable
    fun TransactionRow(tx: Transaction, categories: List<TransactionCategory>) {
        val category = categories.find { it.id == tx.base.categoryId }
        val iconName = category?.iconName ?: "Category"

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(ui.utils.IconMapper.getIconByName(iconName), contentDescription = null, tint = MaterialTheme.colors.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(tx.base.note.ifBlank { "Transaction" }, fontWeight = FontWeight.Bold)
                    // integrated the date visually under the transaction label
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(tx.transactionType.name, color = Color.Gray, style = MaterialTheme.typography.caption)
                        Text("•", color = Color.LightGray, style = MaterialTheme.typography.caption)
                        Text(tx.base.date.substringBefore(".").replace("T", " "), color = Color.Gray, style = MaterialTheme.typography.caption)
                    }
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