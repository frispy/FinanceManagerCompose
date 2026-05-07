package ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import model.enum.CurrencyType
import model.enum.TransactionType
import viewmodel.CreateTransactionScreenModel

class CreateTransactionScreen(private val userId: String) : Screen {

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            CreateTransactionScreenModel(
                userId,
                AppDependencies.accountService,
                AppDependencies.categoryService,
                AppDependencies.transactionService
            )
        }
        val state by screenModel.state.collectAsState()

        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(500.dp).padding(16.dp).heightIn(max = 800.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(32.dp).verticalScroll(rememberScrollState())) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Transaction", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionType.values().forEach { type ->
                            Button(
                                onClick = { screenModel.onTypeChange(type) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (state.selectedType == type) MaterialTheme.colors.primary else Color.LightGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) { Text(type.name, color = if (state.selectedType == type) Color.White else Color.Black) }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.amount,
                            onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 12) screenModel.onAmountChange(it) },
                            label = { Text("Amount") },
                            modifier = Modifier.weight(2f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                        )

                        Button(
                            onClick = {
                                val allCurrencies = CurrencyType.values()
                                val nextIndex = (state.selectedCurrency.ordinal + 1) % allCurrencies.size
                                screenModel.onCurrencyChange(allCurrencies[nextIndex])
                            },
                            modifier = Modifier.weight(1f).height(56.dp).align(Alignment.Bottom),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                        ) {
                            Text(state.selectedCurrency.name)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { if (it.length <= 150) screenModel.onNoteChange(it) },
                        label = { Text("Note / Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Source Account", style = MaterialTheme.typography.caption)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.availableAccounts.forEach { acc ->
                            Button(
                                onClick = { screenModel.onAccountChange(acc.id) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (state.selectedAccountId == acc.id) MaterialTheme.colors.primary else Color.LightGray
                                )
                            ) { Text(acc.displayName, color = if (state.selectedAccountId == acc.id) Color.White else Color.Black) }
                        }
                    }

                    if (state.selectedType == TransactionType.TRANSFER) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Target Account", style = MaterialTheme.typography.caption)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableAccounts.forEach { acc ->
                                Button(
                                    onClick = { screenModel.onTargetAccountChange(acc.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (state.selectedTargetAccountId == acc.id) MaterialTheme.colors.primary else Color.LightGray
                                    )
                                ) { Text(acc.displayName, color = if (state.selectedTargetAccountId == acc.id) Color.White else Color.Black) }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Category", style = MaterialTheme.typography.caption)
                        Spacer(modifier = Modifier.height(8.dp))
                        val filteredCats = state.availableCategories.filter { it.type == state.selectedType }
                        if (filteredCats.isEmpty()) {
                            Text("No categories for this type.", color = Color.Gray)
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredCats.forEach { cat ->
                                    Button(
                                        onClick = { screenModel.onCategoryChange(cat.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = if (state.selectedCategoryId == cat.id) MaterialTheme.colors.primary else Color.LightGray
                                        )
                                    ) { Text(cat.name, color = if (state.selectedCategoryId == cat.id) Color.White else Color.Black) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (state.error != null) {
                        Text(state.error!!, color = MaterialTheme.colors.error)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(
                        onClick = { screenModel.submitTransaction { navigator.pop() } },
                        modifier = Modifier.fillMaxWidth().height(45.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                    ) {
                        Text(if (state.isLoading) "Processing..." else "Submit", color = Color.White)
                    }
                }
            }
        }
    }
}