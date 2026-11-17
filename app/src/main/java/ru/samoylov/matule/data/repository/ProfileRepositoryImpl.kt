package ru.samoylov.matule.data.repository

import ru.samoylov.matule.data.local.PreferencesStorage
import ru.samoylov.matule.data.mapper.toDomain
import ru.samoylov.matule.data.mapper.toDomainResult
import ru.samoylov.matule.data.mapper.toUpdateRequest
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.domain.repository.ProfileRepository
import ru.samoylov.matule.network.MatuleApiService

/**
 * Реализация репозитория профиля.
 *
 * @param api абстракция сетевого слоя
 * @param storage постоянное локальное хранилище
 */
class ProfileRepositoryImpl(
    private val api: MatuleApiService,
    private val storage: PreferencesStorage
) : ProfileRepository {

    override suspend fun getProfile(): DomainResult<User> {
        val userId = storage.getUserId()
            ?: return DomainResult.Error("Сессия не найдена, войдите заново", isAuthError = true)

        return api.getProfile(userId).toDomainResult { it.toDomain() }
    }

    override suspend fun updateProfile(user: User): DomainResult<User> {
        val userId = storage.getUserId()
            ?: return DomainResult.Error("Сессия не найдена, войдите заново", isAuthError = true)

        return api.updateProfile(userId, user.toUpdateRequest()).toDomainResult { it.toDomain() }
    }

    override fun saveDraft(user: User) = storage.saveProfileDraft(user)

    override fun getDraft(): User? = storage.getProfileDraft()

    override fun clearDraft() = storage.clearProfileDraft()
}
