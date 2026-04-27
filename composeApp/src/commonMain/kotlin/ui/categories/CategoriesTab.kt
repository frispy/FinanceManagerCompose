package ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import model.transaction.TransactionCategory
import viewmodel.CategoriesScreenModel

object CategoriesTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 2u, title = "Categories")

    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel {
            CategoriesScreenModel(AppDependencies.categoryService, AppDependencies.categoryRepository)
        }
        val state by screenModel.state.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("Category Management", style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // create new category form
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Create New Category", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.newCategoryName,
                            onValueChange = { screenModel.onNameChange(it) },
                            placeholder = { Text("New category name...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Type", color = Color.Gray, style = MaterialTheme.typography.caption)

                        // simple buttons to emulate dropdown/selection
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { screenModel.onTypeChange("Expense") },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (state.newCategoryType == "Expense") MaterialTheme.colors.primary else Color.LightGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) { Text("Expense", color = if (state.newCategoryType == "Expense") Color.White else Color.Black) }

                            Button(
                                onClick = { screenModel.onTypeChange("Income") },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (state.newCategoryType == "Income") MaterialTheme.colors.primary else Color.LightGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) { Text("Income", color = if (state.newCategoryType == "Income") Color.White else Color.Black) }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { screenModel.saveCategory() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                        ) {
                            Text("Save category", color = Color.White)
                        }
                    }
                }

                // active categories list
                Column(modifier = Modifier.weight(2f)) {
                    Text("Active Categories", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.categories.isEmpty()) {
                        Text("No active categories.", color = Color.Gray)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.categories) { cat ->
                                CategoryCard(cat)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CategoryCard(category: TransactionCategory) {
        // extract encoded info
        val parts = category.name.split("|")
        val pureName = parts.getOrNull(0) ?: category.name
        val type = parts.getOrNull(1) ?: "UNKNOWN"

        Card(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            backgroundColor = MaterialTheme.colors.surface,
            elevation = 0.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(8.dp)))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(pureName, fontWeight = FontWeight.Bold)
                        Text(type, color = Color.Gray, style = MaterialTheme.typography.caption)
                    }
                }
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
            }
        }
    }
}