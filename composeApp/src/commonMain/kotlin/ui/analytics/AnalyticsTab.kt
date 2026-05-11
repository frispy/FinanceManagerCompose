package ui.analytics

import AppDependencies
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import model.enum.CurrencyType
import model.enum.TransactionType
import models.CategoryAnalyticUiItem
import models.TimeAnalyticUiItem
import ui.theme.LocalCurrentUser
import ui.utils.IconMapper
import viewmodel.AnalyticsScreenModel

object AnalyticsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 5u, title = "Analytics")

    @Composable
    override fun Content() {
        val user = LocalCurrentUser.current
        val screenModel = rememberScreenModel {
            AnalyticsScreenModel(
                userId = user.id,
                transactionService = AppDependencies.transactionService,
                categoryService = AppDependencies.categoryService,
                analyticsService = AppDependencies.analyticsService,
                accountService = AppDependencies.accountService,
                currencyExchangeService = AppDependencies.currencyExchangeService
            )
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Analytics Insights", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
                    Text("Track your financial performance and activity volume.", color = Color.Gray)
                }

                // Currency Report Toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyType.values().forEach { cType ->
                        Button(
                            onClick = { screenModel.setCurrency(cType) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (state.reportCurrency == cType) MaterialTheme.colors.primary else Color.LightGray
                            )
                        ) {
                            Text(cType.name, color = if (state.reportCurrency == cType) Color.White else Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val totalIncome = state.timeReport.sumOf { it.rawIncome }
                val totalExpense = state.timeReport.sumOf { it.rawExpense }
                val netCashflow = totalIncome - totalExpense

                // top Summary Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryCard("Total Income", "+ $totalIncome ${state.reportCurrency.name}", Color(0xFF4CAF50), Modifier.weight(1f))
                    SummaryCard("Total Expense", "- $totalExpense ${state.reportCurrency.name}", Color(0xFFE53935), Modifier.weight(1f))
                    SummaryCard("Net Cashflow", "$netCashflow ${state.reportCurrency.name}", if (netCashflow >= 0) Color.Black else Color.Red, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // structured Layout
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                    // left Column
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(24.dp)) {

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colors.surface,
                            elevation = 0.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Monthly Flow", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))

                                if (state.timeReport.isEmpty()) {
                                    Text("No monthly data available.", color = Color.Gray)
                                } else {
                                    state.timeReport.forEach { item ->
                                        MonthlyFlowRow(item, state.reportCurrency.name)
                                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colors.surface,
                            elevation = 0.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Account Activity Volume", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                                Text("Total cash moved across accounts (Income + Expense)", style = MaterialTheme.typography.caption, color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))

                                if (state.accountFlowReport.isEmpty()) {
                                    Text("No account activity.", color = Color.Gray)
                                } else {
                                    state.accountFlowReport.forEachIndexed { index, acc ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("${index + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(acc.accountName, fontWeight = FontWeight.SemiBold)
                                            }
                                            Text(acc.displayFlow, color = Color.Gray, fontWeight = FontWeight.Bold)
                                        }
                                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }

                    // Right Column
                    Column(modifier = Modifier.weight(1f)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colors.surface,
                            elevation = 0.dp,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            var selectedTab by remember { mutableStateOf(TransactionType.EXPENSE) }

                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Category Breakdown", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(TransactionType.EXPENSE, TransactionType.INCOME).forEach { type ->
                                            Button(
                                                onClick = { selectedTab = type },
                                                colors = ButtonDefaults.buttonColors(
                                                    backgroundColor = if (selectedTab == type) MaterialTheme.colors.primary else Color.LightGray
                                                ),
                                            ) {
                                                Text(type.name, color = if (selectedTab == type) Color.White else Color.Black)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                val reportData = if (selectedTab == TransactionType.EXPENSE) state.categoryExpenseReport else state.categoryIncomeReport
                                val barColor = if (selectedTab == TransactionType.EXPENSE) Color(0xFFE53935) else Color(0xFF4CAF50)

                                if (reportData.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                        Text("No data to breakdown.", color = Color.Gray)
                                    }
                                } else {
                                    reportData.forEach { item ->
                                        AnimatedCategoryProgressRow(item, barColor)
                                        Spacer(modifier = Modifier.height(16.dp))
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
    fun SummaryCard(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier,
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, color = Color.Gray, style = MaterialTheme.typography.subtitle2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(value, color = valueColor, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    fun MonthlyFlowRow(item: TimeAnalyticUiItem, currencyName: String) {
        val netRaw = item.rawIncome - item.rawExpense
        val netColor = if (netRaw >= 0) Color(0xFF4CAF50) else Color.Red
        val sign = if (netRaw >= 0) "+" else ""

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.periodLabel, fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("In: ${item.rawIncome}", color = Color(0xFF4CAF50), style = MaterialTheme.typography.caption, fontWeight = FontWeight.SemiBold)
                    Text("Out: ${item.rawExpense}", color = Color(0xFFE53935), style = MaterialTheme.typography.caption, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Net: $sign$netRaw $currencyName", color = netColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.subtitle2)
            }
        }
    }

    @Composable
    fun AnimatedCategoryProgressRow(item: CategoryAnalyticUiItem, progressColor: Color) {
        var isAnimated by remember { mutableStateOf(false) }
        val animatedProgress by animateFloatAsState(
            targetValue = if (isAnimated) item.progress else 0f,
            animationSpec = tween(1000)
        )

        LaunchedEffect(Unit) { isAnimated = true }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = IconMapper.getIconByName(item.iconName),
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.categoryName, fontWeight = FontWeight.SemiBold)
                }
                Text("${item.displayAmount} (${item.displayPercentage})", style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                backgroundColor = Color.LightGray.copy(alpha = 0.3f)
            )
        }
    }
}