package ru.samoylov.matule.domain.model

/**
 * Результат операции доменного слоя.
 *
 * Отдельный тип нужен для того, чтобы presentation-слой не импортировал
 * NetworkResult и вообще ничего не знал о существовании сетевой библиотеки.
 * Репозитории переводят ответ сети в этот тип.
 */
sealed class DomainResult<out T> {

    /**
     * Операция выполнена успешно.
     * @param data полученные данные
     */
    data class Success<T>(val data: T) : DomainResult<T>()

    /**
     * Операция завершилась ошибкой.
     * @param message текст, который можно показать пользователю
     * @param isAuthError true, если требуется повторная авторизация
     */
    data class Error(
        val message: String,
        val isAuthError: Boolean = false
    ) : DomainResult<Nothing>()
}
