package ru.samoylov.matule.data.repository

import ru.samoylov.matule.data.local.PreferencesStorage
import ru.samoylov.matule.data.mapper.toDomain
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.domain.repository.AuthRepository
import ru.samoylov.matule.network.MatuleApiService
import ru.samoylov.matule.network.NetworkResult

/**
 * Реализация репозитория авторизации.
 *
 * Совмещает два источника: сервер через абстракцию сетевого слоя
 * и локальное хранилище для сессии и кода быстрого входа.
 *
 * @param api абстракция сетевого слоя
 * @param storage постоянное локальное хранилище
 */
class AuthRepositoryImpl(
    private val api: MatuleApiService,
    private val storage: PreferencesStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): DomainResult<User> {
        return when (val result = api.login(email, password)) {
            is NetworkResult.Success -> {
                val user = result.data.record.toDomain()
                storage.saveUserId(user.id)
                DomainResult.Success(user)
            }
            is NetworkResult.Failure -> DomainResult.Error(result.message)
        }
    }

    override suspend fun register(email: String, password: String): DomainResult<User> {
        return when (val result = api.register(email, password)) {
            is NetworkResult.Success -> {
                // Сразу выполняем вход, чтобы получить токен для дальнейших запросов
                login(email, password)
            }
            is NetworkResult.Failure -> DomainResult.Error(result.message)
        }
    }

    override suspend fun logout(): DomainResult<Unit> {
        api.logout()
        storage.clearAll()
        return DomainResult.Success(Unit)
    }

    override fun isLoggedIn(): Boolean = storage.hasSession()

    override fun getCurrentUserId(): String? = storage.getUserId()

    override fun savePinCode(pin: String) = storage.savePin(pin)

    override fun checkPinCode(pin: String): Boolean = storage.checkPin(pin)

    override fun hasPinCode(): Boolean = storage.hasPin()
}
