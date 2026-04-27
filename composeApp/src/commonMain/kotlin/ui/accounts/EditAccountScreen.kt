package ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import model.account.BankAccount
import model.account.CashAccount
import model.account.DepositAccount
import viewmodel.EditAccountScreenModel

class EditAccountScreen(private val accountId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            EditAccountScreenModel(accountId, AppDependencies.accountRepository, AppDependencies.accountService)
        }
        val state by screenModel.state.collectAsState()

        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(450.dp).padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.padding(64.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@Card
                }

                if (state.account == null) {
                    Text("Account not found.", modifier = Modifier.padding(32.dp))
                    return@Card
                }

                Column(modifier = Modifier.padding(32.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Account", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { if (it.length <= 40) screenModel.onNameChange(it) },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { if (it.length <= 150) screenModel.onNoteChange(it) },
                        label = { Text("Note / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // specific fields matching class type
                    when (state.account) {
                        is BankAccount -> {
                            OutlinedTextField(
                                value = state.bankName,
                                onValueChange = { if (it.length <= 50) screenModel.onBankNameChange(it) },
                                label = { Text("Bank Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                            )
                        }
                        is CashAccount -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = state.cashLocation,
                                    onValueChange = { if (it.length <= 50) screenModel.onCashLocationChange(it) },
                                    label = { Text("Cash Location") },
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                                )
                                OutlinedTextField(
                                    value = state.dailyLimit,
                                    onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 10) screenModel.onDailyLimitChange(it) },
                                    label = { Text("Daily Limit") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                                )
                            }
                        }
                        is DepositAccount -> {
                            OutlinedTextField(
                                value = state.interestRate,
                                onValueChange = { if ((it.isEmpty() || it.toDoubleOrNull() != null || it == ".") && it.length <= 5) screenModel.onInterestRateChange(it) },
                                label = { Text("Interest Rate (%)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                            )
                        }

                        else -> {
                            Text(
                                text = "No additional fields for this account type.",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { screenModel.deleteAccount { navigator.pop() } },
                            modifier = Modifier.weight(1f).height(45.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red.copy(alpha = 0.8f))
                        ) {
                            Text("Delete", color = Color.White)
                        }
                        Button(
                            onClick = { screenModel.updateAccount { navigator.pop() } },
                            modifier = Modifier.weight(2f).height(45.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                        ) {
                            Text("Save changes", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}