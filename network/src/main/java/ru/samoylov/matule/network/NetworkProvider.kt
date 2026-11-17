package ru.samoylov.matule.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.samoylov.matule.network.retrofit.AuthInterceptor
import ru.samoylov.matule.network.retrofit.RetrofitApi
import ru.samoylov.matule.network.retrofit.RetrofitApiService
import java.util.concurrent.TimeUnit

/**
 * Единственная точка входа в библиотеку сетевого слоя.
 *
 * Приложение вызывает create и получает объект типа MatuleApiService —
 * то есть абстракцию, а не конкретную реализацию на Retrofit.
 */
object NetworkProvider {

    /**
     * Собирает готовый к работе сетевой слой.
     *
     * @param tokenStore хранилище токена; приложение передаёт своё,
     *        чтобы токен переживал перезапуск
     * @param config настройки подключения к серверу
     * @param enableLogging писать ли запросы и ответы в Logcat
     * @return реализация абстракции сетевого слоя
     */
    fun create(
        tokenStore: TokenStore = InMemoryTokenStore(),
        config: ApiConfig = ApiConfig(),
        enableLogging: Boolean = true
    ): MatuleApiService {

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenStore))

        if (enableLogging) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setLenient().create()
                )
            )
            .build()

        return RetrofitApiService(retrofit.create(RetrofitApi::class.java), tokenStore)
    }
}
