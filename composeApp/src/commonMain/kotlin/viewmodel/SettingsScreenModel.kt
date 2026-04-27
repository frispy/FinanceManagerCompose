package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import model.user.User
import service.UserService

class SettingsScreenModel(
    private val userService: UserService
) : ScreenModel {

    // deletes the specific user utilizing repo method
    fun deleteUser(user: User, onComplete: () -> Unit) {
        screenModelScope.launch {
            userService.deleteUser(user)
            onComplete()
        }
    }
}