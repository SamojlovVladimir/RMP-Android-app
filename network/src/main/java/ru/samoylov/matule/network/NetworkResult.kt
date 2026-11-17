package ru.samoylov.matule.network

/**
 * Результат сетевого вызова.
 * Единый тип ответа для всего сетевого слоя: вызывающая сторона не знает,
 * какой библиотекой сделан запрос, и работает только с этим классом.
 */
sealed class NetworkResult<out T> {

    /**
     * Успешный ответ сервера.
     * @param data разобранные данные ответа
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * Ошибка запроса.
     * @param error тип ошибки
     * @param message текст для показа пользователю
     */
    data class Failure(val error: NetworkError, val message: String) : NetworkResult<Nothing>()
}
