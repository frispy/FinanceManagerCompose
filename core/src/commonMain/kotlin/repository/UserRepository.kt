package repository

import model.user.User

interface UserRepository {
    suspend fun findByLogin(login: String): User?
    suspend fun add(user: User)
    suspend fun update(user: User)
}