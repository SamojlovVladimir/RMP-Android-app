package ru.samoylov.matule.presentation.common

/**
 * Состояние экрана.
 *
 * Presentation-слой работает только с этим типом: он не знает
 * ни про сеть, ни про базу данных, а лишь отражает, что показывать.
 */
sealed class UiState<out T> {

    /** Идёт загрузка, показываем индикатор. */
    object Loading : UiState<Nothing>()

    /**
     * Данные получены.
     * @param data содержимое экрана
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Произошла ошибка.
     * @param message текст для уведомления
     * @param requiresLogin нужно ли отправить пользователя на экран входа
     */
    data class Error(
        val message: String,
        val requiresLogin: Boolean = false
    ) : UiState<Nothing>()

    /** Начальное состояние до первого действия пользователя. */
    object Idle : UiState<Nothing>()
}
