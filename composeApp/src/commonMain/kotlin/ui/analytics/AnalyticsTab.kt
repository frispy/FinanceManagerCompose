// --- ./composeApp/src/commonMain/kotlin/ui/analytics/AnalyticsTab.kt ---
package ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
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
                transactionRepository = AppDependencies.transactionRepository,
                categoryRepository = AppDependencies.categoryRepository,
                analyticsService = AppDependencies.analyticsService
            )
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("Analytics & Reports", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
            Text("All amounts converted to ${state.reportCurrency.name}", color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {

                    Card(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        backgroundColor = MaterialTheme.colors.surface,
                        elevation = 0.dp,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text("Expenses by Category", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.categoryReport.isEmpty()) {
                                Text("No expenses to analyze.", color = Color.Gray)
                            } else {
                                LazyColumn {
                                    items(state.categoryReport) { item ->
                                        CategoryProgressRow(item)
                                        Spacer(modifier = Modifier.height(16.dp))
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
                            Text("Cashflow over Time (Monthly)", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (state.timeReport.isEmpty()) {
                                Text("No historical data.", color = Color.Gray)
                            } else {
                                LazyColumn {
                                    items(state.timeReport) { item ->
                                        TimeLineRow(item)
                                        Divider(modifier = Modifier.padding(vertical = 12.dp))
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
    fun CategoryProgressRow(item: CategoryAnalyticUiItem) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(IconMapper.getIconByName(item.iconName), contentDescription = null, tint = MaterialTheme.colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.categoryName, fontWeight = FontWeight.SemiBold)
                }
                Text("${item.displayAmount} (${item.displayPercentage})", style = MaterialTheme.typography.body2)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = item.progress,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colors.primary,
                backgroundColor = Color.LightGray
            )
        }
    }

    @Composable
    fun TimeLineRow(item: TimeAnalyticUiItem) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.periodLabel, fontWeight = FontWeight.Bold)
            Column(horizontalAlignment = Alignment.End) {
                Text(item.displayIncome, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Text(item.displayExpense, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}