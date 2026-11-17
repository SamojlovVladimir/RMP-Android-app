package ru.samoylov.matule.network.retrofit

import com.google.gson.Gson
import retrofit2.Response
import ru.samoylov.matule.network.NetworkError
import ru.samoylov.matule.network.NetworkResult
import ru.samoylov.matule.network.dto.ErrorDto
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Обработка ошибок сетевого слоя.
 *
 * Единственное место, где исключения библиотеки и коды HTTP превращаются
 * в NetworkResult.Failure — принцип единственной ответственности из SOLID.
 * Благодаря этому ни один запрос не дублирует логику разбора ошибок (DRY).
 */
internal object ErrorHandler {

    private val gson = Gson()

    /**
     * Выполняет запрос и оборачивает результат или ошибку.
     * @param call сам вызов Retrofit
     * @return успешный результат или разобранная ошибка
     */
    suspend fun <T> safeCall(call: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    @Suppress("UNCHECKED_CAST")
                    NetworkResult.Success(Unit as T)
                }
            } else {
                mapHttpError(response.code(), response.errorBody()?.string())
            }
        } catch (e: UnknownHostException) {
            NetworkResult.Failure(
                NetworkError.NO_CONNECTION,
                "Нет соединения с интернетом"
            )
        } catch (e: SocketTimeoutException) {
            NetworkResult.Failure(
                NetworkError.TIMEOUT,
                "Сервер не отвечает, попробуйте позже"
            )
        } catch (e: IOException) {
            NetworkResult.Failure(
                NetworkError.NO_CONNECTION,
                "Проблема с сетью, проверьте подключение"
            )
        } catch (e: Exception) {
            NetworkResult.Failure(
                NetworkError.UNKNOWN,
                e.message ?: "Произошла неизвестная ошибка"
            )
        }
    }

    /**
     * Переводит код HTTP и тело ответа в понятную ошибку.
     * @param code код состояния HTTP
     * @param rawBody тело ответа с ошибкой
     */
    private fun mapHttpError(code: Int, rawBody: String?): NetworkResult.Failure {
        val serverMessage = parseServerMessage(rawBody)
        return when (code) {
            400, 422 -> NetworkResult.Failure(
                NetworkError.VALIDATION,
                serverMessage ?: "Проверьте правильность введённых данных"
            )
            401, 403 -> NetworkResult.Failure(
                NetworkError.UNAUTHORIZED,
                serverMessage ?: "Неверный логин или пароль"
            )
            404 -> NetworkResult.Failure(
                NetworkError.NOT_FOUND,
                serverMessage ?: "Запрошенные данные не найдены"
            )
            in 500..599 -> NetworkResult.Failure(
                NetworkError.SERVER,
                serverMessage ?: "Ошибка на сервере, попробуйте позже"
            )
            else -> NetworkResult.Failure(
                NetworkError.UNKNOWN,
                serverMessage ?: "Произошла ошибка, код $code"
            )
        }
    }

    /** Достаёт текст ошибки из тела ответа, если сервер его прислал. */
    private fun parseServerMessage(rawBody: String?): String? {
        if (rawBody.isNullOrBlank()) return null
        return try {
            gson.fromJson(rawBody, ErrorDto::class.java)?.message
        } catch (e: Exception) {
            null
        }
    }
}
