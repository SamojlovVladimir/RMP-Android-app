package ru.samoylov.matule.network

/**
 * Настройки подключения к серверу.
 *
 * @param baseUrl адрес сервера, обязательно со слэшем на конце
 * @param connectTimeoutSeconds таймаут установки соединения
 * @param readTimeoutSeconds таймаут ожидания ответа
 */
data class ApiConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    val connectTimeoutSeconds: Long = 30,
    val readTimeoutSeconds: Long = 30
) {
    companion object {
        /** Адрес Pocketbase по умолчанию. Заменить на адрес с площадки чемпионата. */
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8090/"
    }
}
