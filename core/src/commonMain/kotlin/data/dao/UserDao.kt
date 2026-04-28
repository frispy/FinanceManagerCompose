package data.dao

import androidx.room.*
import data.entity.UserEntity

@Dao
interface UserDao : BaseDao<UserEntity> {
    @Query("SELECT * FROM users WHERE login = :login LIMIT 1")
    suspend fun findByLogin(login: String): UserEntity?

}