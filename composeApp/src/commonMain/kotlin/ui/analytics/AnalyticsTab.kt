package ui.analytics

import AppDependencies
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
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
                analyticsService = AppDependencies.analyticsService
            )
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text(
                text = "Analytics Insights",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track your financial performance dynamically across ${state.reportCurrency.name}.",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val totalIncome = state.timeReport.sumOf { it.rawIncome }
                val totalExpense = state.timeReport.sumOf { it.rawExpense }
                val netCashflow = totalIncome - totalExpense

                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    // TOP ROW: Summary & Graph
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Summary Card
                        Card(
                            modifier = Modifier.weight(0.4f).fillMaxHeight(),
                            backgroundColor = MaterialTheme.colors.primary,
                            elevation = 0.dp,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
                                Text("Net Cashflow", color = Color.White.copy(alpha = 0.8f))
                                AnimatedCounterText(value = netCashflow, currency = state.reportCurrency.name, color = Color.White, style = MaterialTheme.typography.h3)

                                Spacer(modifier = Modifier.height(32.dp))

                                Text("Total Income", color = Color.White.copy(alpha = 0.8f))
                                AnimatedCounterText(value = totalIncome, currency = state.reportCurrency.name, color = Color(0xFF81C784), style = MaterialTheme.typography.h5)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Total Expense", color = Color.White.copy(alpha = 0.8f))
                                AnimatedCounterText(value = totalExpense, currency = state.reportCurrency.name, color = Color(0xFFE57373), style = MaterialTheme.typography.h5)
                            }
                        }

                        // Graph Card
                        Card(
                            modifier = Modifier.weight(0.6f).fillMaxHeight(),
                            backgroundColor = MaterialTheme.colors.surface,
                            elevation = 0.dp,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                                Text("Cashflow Over Time (Monthly)", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))

                                if (state.timeReport.isEmpty()) {
                                    Text("No historical data to graph.", color = Color.Gray)
                                } else {
                                    CashflowBarChart(state.timeReport)
                                }
                            }
                        }
                    }

                    // BOTTOM ROW: Categories
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        backgroundColor = MaterialTheme.colors.surface,
                        elevation = 0.dp,
                        shape = RoundedCornerShape(24.dp)
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
                                            Text(type.name + "S", color = if (selectedTab == type) Color.White else Color.Black)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            val reportData = if (selectedTab == TransactionType.EXPENSE) state.categoryExpenseReport else state.categoryIncomeReport
                            val barColor = if (selectedTab == TransactionType.EXPENSE) Color(0xFFE53935) else Color(0xFF4CAF50)

                            if (reportData.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No data to breakdown.", color = Color.Gray)
                                }
                            } else {
                                LazyColumn {
                                    items(reportData) { item ->
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
    fun AnimatedCounterText(value: Long, currency: String, color: Color, style: androidx.compose.ui.text.TextStyle) {
        var animationTriggered by remember { mutableStateOf(false) }
        val animatedValue by animateFloatAsState(
            targetValue = if (animationTriggered) value.toFloat() else 0f,
            animationSpec = tween(durationMillis = 1500)
        )

        LaunchedEffect(Unit) { animationTriggered = true }

        Text("${animatedValue.toLong()} $currency", color = color, style = style, fontWeight = FontWeight.Bold)
    }

    @Composable
    fun CashflowBarChart(items: List<TimeAnalyticUiItem>) {
        val maxAmount = items.maxOf { maxOf(it.rawIncome, it.rawExpense) }.toFloat().coerceAtLeast(1f)
        var animationTriggered by remember { mutableStateOf(false) }
        val heightProgress by animateFloatAsState(targetValue = if (animationTriggered) 1f else 0f, animationSpec = tween(1200))

        LaunchedEffect(Unit) { animationTriggered = true }

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..4) {
                val y = size.height * (i / 4f)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            val availableWidth = size.width
            val itemWidth = availableWidth / items.size.coerceAtLeast(1)
            val barWidth = itemWidth * 0.3f
            var xOffset = itemWidth * 0.2f

            items.forEach { item ->
                val incomeHeight = (item.rawIncome / maxAmount) * size.height * heightProgress
                val expenseHeight = (item.rawExpense / maxAmount) * size.height * heightProgress

                drawRoundRect(
                    color = Color(0xFF4CAF50),
                    topLeft = Offset(xOffset, size.height - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawRoundRect(
                    color = Color(0xFFE53935),
                    topLeft = Offset(xOffset + barWidth + 2.dp.toPx(), size.height - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                xOffset += itemWidth
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