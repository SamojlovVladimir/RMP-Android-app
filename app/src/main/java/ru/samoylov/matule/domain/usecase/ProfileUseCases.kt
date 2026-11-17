package ru.samoylov.matule.domain.usecase

import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.domain.repository.ProfileRepository

/**
 * Сценарии работы с профилем пользователя.
 */

/**
 * Загрузка профиля для экрана «Профиль».
 * @param repository репозиторий профиля
 */
class GetProfileUseCase(private val repository: ProfileRepository) {

    suspend operator fun invoke(): DomainResult<User> = repository.getProfile()
}

/**
 * Сохранение профиля с проверкой обязательных полей.
 * @param repository репозиторий профиля
 */
class SaveProfileUseCase(private val repository: ProfileRepository) {

    /** @param user заполненный профиль */
    suspend operator fun invoke(user: User): DomainResult<User> {
        RequiredFieldValidator.validate(user.name, "Имя")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(user.surname, "Фамилия")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(user.patronymic, "Отчество")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(user.birthDate, "Дата рождения")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(user.gender, "Пол")
            ?.let { return DomainResult.Error(it) }
        EmailValidator.validate(user.email)?.let { return DomainResult.Error(it) }

        val result = repository.updateProfile(user)
        if (result is DomainResult.Success) repository.clearDraft()
        return result
    }
}

/**
 * Черновик профиля: сохранение незаконченной формы между запусками.
 * @param repository репозиторий профиля
 */
class ProfileDraftUseCase(private val repository: ProfileRepository) {

    /**
     * Сохраняет текущее состояние формы.
     * @param user черновик профиля
     */
    fun save(user: User) = repository.saveDraft(user)

    /** Возвращает сохранённый черновик или null. */
    fun restore(): User? = repository.getDraft()

    /** Удаляет черновик. */
    fun clear() = repository.clearDraft()
}
