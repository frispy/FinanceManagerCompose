package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.transaction.TransactionCategory
import repository.CategoryRepository
import service.CategoryService

data class CategoriesState(
    val categories: List<TransactionCategory> = emptyList(),
    val newCategoryName: String = "",
    val newCategoryType: String = "Expense"
)

class CategoriesScreenModel(
    private val categoryService: CategoryService,
    private val categoryRepository: CategoryRepository // using repo to listen to flow
) : ScreenModel {

    private val _state = MutableStateFlow(CategoriesState())
    val state = _state.asStateFlow()

    init {
        screenModelScope.launch {
            categoryRepository.getAllCategoriesFlow().collect { list ->
                _state.update { it.copy(categories = list) }
            }
        }
    }

    fun onNameChange(name: String) = _state.update { it.copy(newCategoryName = name) }
    fun onTypeChange(type: String) = _state.update { it.copy(newCategoryType = type) }

    fun saveCategory() {
        val name = _state.value.newCategoryName
        if (name.isBlank()) return

        // hack: combine name and type into single string because CategoryEntity lacks 'type' field
        val combinedName = "$name|${_state.value.newCategoryType}"

        screenModelScope.launch {
            categoryService.createCategory(combinedName)
            _state.update { it.copy(newCategoryName = "") } // reset field
        }
    }
}