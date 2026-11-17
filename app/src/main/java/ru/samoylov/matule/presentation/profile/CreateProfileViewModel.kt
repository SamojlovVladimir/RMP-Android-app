package ru.samoylov.matule.presentation.profile

import androidx.lifecycle.ViewModel
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.domain.usecase.EmailValidator

/**
 * Логика экрана «Создание профиля».
 *
 * Каждое изменение формы сохраняется как черновик, поэтому введённые данные
 * переживают перезапуск приложения. Черновик забирает экран «Создание пароля»
 * и отправляет его на сервер вместе с паролем.
 */
class CreateProfileViewModel : ViewModel() {

    private val draftUseCase = ServiceLocator.profileDraftUseCase

    /** Возвращает сохранённый черновик или null. */
    fun restoreDraft(): User? = draftUseCase.restore()

    /**
     * Сохраняет текущее состояние формы.
     * @param user значения полей формы
     */
    fun saveDraft(user: User) = draftUseCase.save(user)

    /**
     * Все ли поля заполнены (тогда кнопка «Далее» активна).
     * @param user значения полей формы
     */
    fun isComplete(user: User): Boolean =
        user.name.isNotBlank() && user.patronymic.isNotBlank() && user.surname.isNotBlank() &&
            user.birthDate.length == DATE_LENGTH && user.gender.isNotBlank() &&
            user.email.isNotBlank()

    /**
     * Проверяет почту перед переходом дальше.
     * @param email введённая почта
     * @return текст ошибки или null
     */
    fun validateEmail(email: String): String? = EmailValidator.validate(email)

    private companion object {
        /** Длина даты в формате ДД.ММ.ГГГГ */
        const val DATE_LENGTH = 10
    }
}
