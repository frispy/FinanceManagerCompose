package ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import model.enum.TransactionType
import model.transaction.TransactionCategory
import ui.utils.IconMapper
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
                // create or edit category form
                Card(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 0.dp,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = if (state.editingCategoryId != null) "Edit Category" else "Create New Category",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.newCategoryName,
                            onValueChange = { screenModel.onNameChange(it) },
                            placeholder = { Text("Category name...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(backgroundColor = Color.White)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Type", color = Color.Gray, style = MaterialTheme.typography.caption)

                        // simple buttons to emulate selection
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { screenModel.onTypeChange(TransactionType.EXPENSE) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (state.newCategoryType == TransactionType.EXPENSE) MaterialTheme.colors.primary else Color.LightGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) { Text("Expense", color = if (state.newCategoryType == TransactionType.EXPENSE) Color.White else Color.Black) }

                            Button(
                                onClick = { screenModel.onTypeChange(TransactionType.INCOME) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (state.newCategoryType == TransactionType.INCOME) MaterialTheme.colors.primary else Color.LightGray
                                ),
                                modifier = Modifier.weight(1f)
                            ) { Text("Income", color = if (state.newCategoryType == TransactionType.INCOME) Color.White else Color.Black) }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Icon", color = Color.Gray, style = MaterialTheme.typography.caption)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(IconMapper.availableIcons) { iconName ->
                                val isSelected = state.newCategoryIcon == iconName
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .background(
                                            if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.2f) else Color.White,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { screenModel.onIconChange(iconName) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconMapper.getIconByName(iconName),
                                        contentDescription = iconName,
                                        tint = if (isSelected) MaterialTheme.colors.primary else Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.editingCategoryId != null) {
                            OutlinedButton(
                                onClick = { screenModel.cancelEditing() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { screenModel.saveCategory() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                        ) {
                            Text(if (state.editingCategoryId != null) "Update category" else "Save category", color = Color.White)
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
                                CategoryCard(
                                    category = cat,
                                    onEdit = { screenModel.startEditing(cat) },
                                    onDelete = { screenModel.deleteCategory(cat.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CategoryCard(category: TransactionCategory, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                    Box(
                        modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(IconMapper.getIconByName(category.iconName), contentDescription = null, tint = MaterialTheme.colors.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(category.name, fontWeight = FontWeight.Bold)
                        Text(category.type.name, color = Color.Gray, style = MaterialTheme.typography.caption)
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}