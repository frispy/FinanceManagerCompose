package repository

// basic crud operations
interface BaseRepository<T, ID> {
    suspend fun getById(id: ID): T?
    suspend fun add(item: T)
    suspend fun update(item: T)
}