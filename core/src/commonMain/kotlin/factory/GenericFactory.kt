package factory

interface GenericFactory<T, P> {
    fun create(params: P): T
}