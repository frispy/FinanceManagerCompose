package ui.accounts

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
import model.account.Account
import model.account.BankAccount
import ui.theme.LocalCurrentUser
import viewmodel.AccountsScreenModel

object AccountsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 1u, title = "Accounts")

    @Composable
    override fun Content() {
        // we use parent navigator to push full screen over the tabs
        val rootNavigator = LocalNavigator.current?.parent
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            AccountsScreenModel(user.id, AppDependencies.accountRepository)
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            // header row with title and add button
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
                    items(state.accounts) { account ->
                        DetailedAccountCard(account)
                    }
                }
            }
        }
    }

    @Composable
    fun DetailedAccountCard(account: Account) {
        Card(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            backgroundColor = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(16.dp),
            elevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(account.accountType.name, color = Color.Gray, style = MaterialTheme.typography.caption)
                // in your current design note / name might be stored differently,
                // but we display bank name or generic name as title
                val accName = if (account is BankAccount) account.bankName else "Account"
                Text(accName, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))
                Text("${account.currency} ${account.balance}", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(16.dp))
                // fallback text for now since note might be added later to the base account model
                Text("Your custom description or note will be here...", color = Color.Gray, style = MaterialTheme.typography.body2)

                Spacer(modifier = Modifier.height(24.dp))
                Text("Transactions", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("No local history available yet.", color = Color.Gray)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text("Full history", color = Color.White)
                    }
                    OutlinedButton(onClick = {}) {
                        Text("Edit", color = MaterialTheme.colors.primary)
                    }
                }
            }
        }
    }
}