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
import model.enum.AccountType
import model.enum.CurrencyType
import viewmodel.CreateAccountScreenModel

class CreateAccountScreen(private val userId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            CreateAccountScreenModel(userId, AppDependencies.accountService)
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
                Column(modifier = Modifier.padding(32.dp)) {
                    // header with back button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create New Account",
                            style = MaterialTheme.typography.h5,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primaryVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // account type selector
                    Text("Account Type", color = Color.Gray, style = MaterialTheme.typography.caption)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccountType.values().forEach { type ->
                            Button(
                                onClick = { screenModel.onAccountTypeChange(type) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (state.accountType == type) MaterialTheme.colors.primary else Color.LightGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(type.name, color = if (state.accountType == type) Color.White else Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // common fields
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.balance,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 12) screenModel.onBalanceChange(it) },
                            label = { Text("Initial Balance") },
                            modifier = Modifier.weight(2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                        )

                        // simple currency selector emulation
                        Button(
                            onClick = {
                                // toggle currency logically for demo
                                val next = if (state.currency == CurrencyType.USD) CurrencyType.EUR else CurrencyType.USD
                                screenModel.onCurrencyChange(next)
                            },
                            modifier = Modifier.weight(1f).height(56.dp).align(Alignment.Bottom),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                        ) {
                            Text(state.currency.name)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // specific fields based on account type
                    when (state.accountType) {
                        AccountType.BANK -> {
                            OutlinedTextField(
                                value = state.bankName,
                                onValueChange = { if (it.length <= 50) screenModel.onBankNameChange(it) },
                                label = { Text("Bank Name") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                            )
                        }
                        AccountType.CASH -> {
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
                        AccountType.DEPOSIT -> {
                            OutlinedTextField(
                                value = state.interestRate,
                                onValueChange = { if ((it.isEmpty() || it.toDoubleOrNull() != null || it == ".") && it.length <= 5) screenModel.onInterestRateChange(it) },
                                label = { Text("Interest Rate (%)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.error != null) {
                        Text(state.error!!, color = MaterialTheme.colors.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = {
                            screenModel.saveAccount {
                                navigator.pop() // return to accounts tab on success
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary
                        )
                    ) {
                        Text(if (state.isLoading) "Saving..." else "Create Account")
                    }
                }
            }
        }
    }
}