package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.enum.TransactionType
import model.transaction.TransactionCategory
import models.CategoryUiModel
import models.toUiModel
import repository.CategoryRepository
import service.CategoryService

data class CategoriesState(
    val categories: List<CategoryUiModel> = emptyList(),
    val editingCategoryId: String? = null,
    val newCategoryName: String = "",
    val newCategoryType: TransactionType = TransactionType.EXPENSE,
    val newCategoryIcon: String = "Home"
)

class CategoriesScreenModel(
    private val categoryService: CategoryService,
    private val categoryRepository: CategoryRepository
) : ScreenModel {

    private val _state = MutableStateFlow(CategoriesState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collect { list ->
                _state.update { it.copy(categories = list.map { cat -> cat.toUiModel() }) }
            }
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(newCategoryName = name) }
    fun onTypeChange(type: TransactionType) = _state.update { it.copy(newCategoryType = type) }
    fun onIconChange(iconName: String) = _state.update { it.copy(newCategoryIcon = iconName) }

    fun startEditing(category: CategoryUiModel) {
        _state.update {
            it.copy(
                editingCategoryId = category.id,
                newCategoryName = category.name,
                newCategoryType = category.type,
                newCategoryIcon = category.iconName
            )
        }
    }

    fun cancelEditing() {
        _state.update {
            it.copy(
                editingCategoryId = null,
                newCategoryName = "",
                newCategoryType = TransactionType.EXPENSE,
                newCategoryIcon = "Home"
            )
        }
    }

    fun saveCategory() {
        val currentState = _state.value
        if (currentState.newCategoryName.isBlank()) return

        screenModelScope.launch {
            if (currentState.editingCategoryId != null) {
                // update existing
                val updatedCategory = TransactionCategory(
                    id = currentState.editingCategoryId,
                    name = currentState.newCategoryName,
                    type = currentState.newCategoryType,
                    iconName = currentState.newCategoryIcon
                )
                categoryService.updateCategory(updatedCategory)
            } else {
                // create new
                categoryService.createCategory(
                    name = currentState.newCategoryName,
                    type = currentState.newCategoryType,
                    iconName = currentState.newCategoryIcon
                )
            }
            // reset form
            cancelEditing()
        }
    }

    fun deleteCategory(id: String) {
        screenModelScope.launch {
            categoryService.deleteCategory(id)
            if (_state.value.editingCategoryId == id) {
                cancelEditing()
            }
        }
    }
}