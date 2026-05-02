package viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import models.UserUiModel
import service.UserService

class SettingsScreenModel(
    private val userService: UserService
) : ScreenModel {

    fun deleteUser(user: UserUiModel, onComplete: () -> Unit) {
        screenModelScope.launch {
            val domainUser = model.user.User(user.id, user.login, ByteArray(0))
            userService.deleteUser(domainUser)
            onComplete()
        }
    }
}