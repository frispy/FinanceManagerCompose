package service

import factory.GenericFactory
import model.params.UserCreationParams
import model.user.User
import repository.UserRepository

class UserService(
    private val userRepository: UserRepository,
    private val userFactory: GenericFactory<User, UserCreationParams>,
){
    suspend fun register(params: UserCreationParams): Boolean {
        if (userRepository.findByLogin(params.login) != null) {
            return false
        }
        val newUser = userFactory.create(params)
        userRepository.add(newUser)
        return true
    }

    suspend fun login(login: String, pass: String): User? {
        val user = userRepository.findByLogin(login) ?: return null
        if (pass.toByteArray().contentEquals(user.passwordHash)) {
            return user
        }
        return null
    }

    suspend fun changePassword(login: String, oldPassword: String, newPassword: String): Boolean {
        val foundUser = userRepository.findByLogin(login) ?: return false

        // verify the old password matches what's in the DB
        if (!oldPassword.toByteArray().contentEquals(foundUser.passwordHash)) {
            return false
        }

        // check if the new password is the same as the old one
        if (newPassword.toByteArray().contentEquals(foundUser.passwordHash)) {
            return false
        }

        // update the user with the new hash
        val updatedUser = foundUser.copy(passwordHash = newPassword.toByteArray())
        userRepository.update(updatedUser)

        return true
    }
}