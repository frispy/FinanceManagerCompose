package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.params.UserCreationParams
import service.UserService

data class RegistrationState(
    val login: String = "",
    val pass: String = "",
    val confirmPass: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class RegistrationScreenModel(
    private val userService: UserService
) : ScreenModel {

    private val _state = MutableStateFlow(RegistrationState())
    val state = _state.asStateFlow()

    fun onLoginChanged(login: String) = _state.update { it.copy(login = login) }
    fun onPasswordChanged(pass: String) = _state.update { it.copy(pass = pass) }
    fun onConfirmPasswordChanged(pass: String) = _state.update { it.copy(confirmPass = pass) }

    fun register(onSuccess: () -> Unit) {
        val currentState = _state.value

        if (currentState.login.isBlank() || currentState.pass.isBlank()) {
            _state.update { it.copy(error = "Please fill all fields") }
            return
        }

        if (currentState.pass != currentState.confirmPass) {
            _state.update { it.copy(error = "Passwords don't match") }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val success = userService.register(
                UserCreationParams(
                    login = currentState.login,
                    pass = currentState.pass
                )
            )

            if (success) {
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } else {
                _state.update {
                    it.copy(isLoading = false, error = "User with this login already exists")
                }
            }
        }
    }
}