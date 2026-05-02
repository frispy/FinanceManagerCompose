package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import models.UserUiModel
import models.toUiModel
import service.UserService

data class LoginState(
    val login: String = "",
    val pass: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginScreenModel(
    private val userService: UserService
) : ScreenModel {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onLoginChanged(login: String) = _state.update { it.copy(login = login) }
    fun onPasswordChanged(pass: String) = _state.update { it.copy(pass = pass) }

    fun login(onSuccess: (UserUiModel) -> Unit) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val user = userService.login(_state.value.login, _state.value.pass)

            if (user != null) {
                _state.update { it.copy(isLoading = false) }
                onSuccess(user.toUiModel())
            } else {
                _state.update { it.copy(isLoading = false, error = "Invalid login or password") }
            }
        }
    }
}