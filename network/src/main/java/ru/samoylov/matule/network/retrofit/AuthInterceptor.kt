package ru.samoylov.matule.network.retrofit

import okhttp3.Interceptor
import okhttp3.Response
import ru.samoylov.matule.network.TokenStore

/**
 * Подставляет токен авторизации в каждый запрос.
 *
 * @param tokenStore хранилище, откуда берётся действующий токен
 */
internal class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenStore.getToken()
        val request = if (token.isNullOrEmpty()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
        }
        return chain.proceed(request)
    }
}
