package ui.accounts

import AppDependencies
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import models.TransactionUiModel
import viewmodel.AccountDetailsScreenModel

class AccountDetailsScreen(private val accountId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            AccountDetailsScreenModel(
                accountId = accountId,
                accountService = AppDependencies.accountService,
                transactionService = AppDependencies.transactionService,
                categoryService = AppDependencies.categoryService
            )
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background).padding(32.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Account Summary", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.account == null) {
                Text("Account not found.", color = Color.Gray)
            } else {
                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    backgroundColor = MaterialTheme.colors.surface,
                    shape = RoundedCornerShape(16.dp),
                    elevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(32.dp)) {
                        Text(state.account!!.accountTypeLabel, color = Color.Gray, style = MaterialTheme.typography.caption)
                        Text(state.account!!.displayName, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.account!!.displayBalance, style = MaterialTheme.typography.h3, fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.account!!.note.ifBlank { "No specific notes provided." }, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ull History
                Text("Full Local History", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (state.transactions.isEmpty()) {
                    Text("No transactions linked to this account.", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(state.transactions) { tx ->
                            AccountTransactionRow(tx)
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AccountTransactionRow(tx: TransactionUiModel) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(8.dp)),
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
            Text(
                text = tx.displayAmount,
                fontWeight = FontWeight.Bold,
                color = if (tx.isExpense) Color.Black else Color(0xFF4CAF50)
            )
        }
    }
}