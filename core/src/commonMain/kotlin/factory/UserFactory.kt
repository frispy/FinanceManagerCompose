package factory

import model.user.User
import model.params.UserCreationParams
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserFactory : GenericFactory<User, UserCreationParams> {
    @OptIn(ExperimentalUuidApi::class)
    override fun create(params: UserCreationParams): User {
        val userId = Uuid.random().toString()
        val passwordHash = params.pass.toByteArray()
        return User(userId, params.login, passwordHash)
    }
}