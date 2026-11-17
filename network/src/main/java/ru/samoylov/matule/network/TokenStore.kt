package ru.samoylov.matule.network

/**
 * Хранилище токена доступа.
 *
 * Сетевой слой умеет только читать и писать токен, но не знает,
 * где именно он лежит: реализацию подставляет приложение
 * (SharedPreferences, база данных, память). Ещё одна точка,
 * которая делает библиотеку заменяемой и тестируемой.
 */
interface TokenStore {

    /** Возвращает сохранённый токен или null, если пользователь не авторизован. */
    fun getToken(): String?

    /**
     * Сохраняет токен после успешной авторизации.
     * @param token токен доступа
     */
    fun saveToken(token: String)

    /** Удаляет токен при выходе из аккаунта. */
    fun clearToken()
}

/**
 * Простая реализация хранилища токена в оперативной памяти.
 * Используется по умолчанию и в тестах; приложение подменяет её
 * на постоянное хранилище.
 */
class InMemoryTokenStore : TokenStore {

    private var token: String? = null

    override fun getToken(): String? = token

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun clearToken() {
        token = null
    }
}
