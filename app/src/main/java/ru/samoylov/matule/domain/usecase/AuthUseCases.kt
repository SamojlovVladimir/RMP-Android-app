package ru.samoylov.matule.domain.usecase

import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.domain.repository.AuthRepository
import ru.samoylov.matule.domain.repository.ProfileRepository

/**
 * Сценарии взаимодействия, связанные с авторизацией.
 *
 * Каждый сценарий — отдельный класс с одним методом: экран вызывает
 * ровно то действие, которое ему нужно, и не получает доступ к остальным
 * возможностям репозитория (принцип разделения интерфейсов из SOLID).
 */

/**
 * Вход в приложение.
 * @param repository репозиторий авторизации
 */
class LoginUseCase(private val repository: AuthRepository) {

    /**
     * Проверяет введённые данные и выполняет вход.
     * @param email почта
     * @param password пароль
     */
    suspend operator fun invoke(email: String, password: String): DomainResult<User> {
        EmailValidator.validate(email)?.let { return DomainResult.Error(it) }
        if (password.isBlank()) return DomainResult.Error("Введите пароль")
        return repository.login(email, password)
    }
}

/**
 * Регистрация нового пользователя.
 * @param repository репозиторий авторизации
 */
class RegisterUseCase(private val repository: AuthRepository) {

    /**
     * Проверяет данные и создаёт пользователя.
     * @param email почта
     * @param password пароль
     * @param confirmation повтор пароля
     */
    suspend operator fun invoke(
        email: String,
        password: String,
        confirmation: String
    ): DomainResult<User> {
        EmailValidator.validate(email)?.let { return DomainResult.Error(it) }
        PasswordValidator.validate(password)?.let { return DomainResult.Error(it) }
        PasswordValidator.validateConfirmation(password, confirmation)
            ?.let { return DomainResult.Error(it) }
        return repository.register(email, password)
    }
}

/**
 * Выход из аккаунта.
 * @param repository репозиторий авторизации
 */
class LogoutUseCase(private val repository: AuthRepository) {

    suspend operator fun invoke(): DomainResult<Unit> = repository.logout()
}

/**
 * Определение стартового экрана при запуске приложения.
 * @param repository репозиторий авторизации
 */
class GetStartDestinationUseCase(private val repository: AuthRepository) {

    /** Возможные варианты первого экрана. */
    enum class Destination {
        /** Пользователь авторизован и установил код — просим код или биометрию */
        PIN_LOGIN,

        /** Пользователь авторизован, но код ещё не создан — экран создания кода */
        PIN_CREATE,

        /** Пользователь не авторизован — экран входа и регистрации */
        AUTH
    }

    /** @return экран, который нужно показать после заставки */
    operator fun invoke(): Destination = when {
        repository.isLoggedIn() && repository.hasPinCode() -> Destination.PIN_LOGIN
        repository.isLoggedIn() -> Destination.PIN_CREATE
        else -> Destination.AUTH
    }
}

/**
 * Код быстрого входа в приложение (четыре цифры).
 * @param repository репозиторий авторизации
 */
class PinCodeUseCase(private val repository: AuthRepository) {

    /**
     * Сохраняет новый код.
     * @param pin код из четырёх цифр
     * @return текст ошибки или null, если код сохранён
     */
    fun save(pin: String): String? {
        if (pin.length != 4 || pin.any { !it.isDigit() }) return "Код должен состоять из 4 цифр"
        repository.savePinCode(pin)
        return null
    }

    /**
     * Проверяет введённый код.
     * @param pin введённый код
     */
    fun check(pin: String): Boolean = repository.checkPinCode(pin)

    /** Создан ли уже код на этом устройстве. */
    fun hasPin(): Boolean = repository.hasPinCode()
}

/**
 * Завершение регистрации: создание аккаунта с паролем и сохранение
 * профиля, заполненного на экране «Создание профиля».
 * @param authRepository репозиторий авторизации
 * @param profileRepository репозиторий профиля
 */
class CompleteRegistrationUseCase(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) {

    /**
     * @param password новый пароль
     * @param confirmation повтор пароля
     */
    suspend operator fun invoke(password: String, confirmation: String): DomainResult<User> {
        PasswordValidator.validate(password)?.let { return DomainResult.Error(it) }
        PasswordValidator.validateConfirmation(password, confirmation)
            ?.let { return DomainResult.Error(it) }

        val draft = profileRepository.getDraft()
            ?: return DomainResult.Error("Сначала заполните профиль")

        val registerResult = authRepository.register(draft.email, password)
        if (registerResult is DomainResult.Error) return registerResult

        // id появился только после регистрации, поэтому подставляем его в черновик
        val userId = (registerResult as DomainResult.Success).data.id
        val result = profileRepository.updateProfile(draft.copy(id = userId))
        if (result is DomainResult.Success) profileRepository.clearDraft()
        return result
    }
}
