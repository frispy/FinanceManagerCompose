package repository

import data.dao.UserDao
import data.entity.toDomain
import data.entity.toEntity
import model.user.User


class UserRepositoryImpl(private val dao: UserDao): UserRepository {
    override suspend fun findByLogin(login: String): User? {
        return dao.findByLogin(login)?.toDomain()
    }

    override suspend fun add(user: User) {
        dao.insert(user.toEntity())
    }

    override suspend fun update(user: User) {
        dao.update(user.toEntity())
    }
}