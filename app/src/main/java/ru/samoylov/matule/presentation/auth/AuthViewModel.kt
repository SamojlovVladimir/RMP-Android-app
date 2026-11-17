package ru.samoylov.matule.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.usecase.EmailValidator
import ru.samoylov.matule.presentation.common.UiState

/**
 * Логика экрана «Вход и регистрация».
 *
 * Хранит состояние экрана и вызывает сценарии доменного слоя.
 * Про сеть и хранилище ничего не знает, поэтому замена источника данных
 * этот класс не затрагивает.
 */
class AuthViewModel : ViewModel() {

    private val loginUseCase = ServiceLocator.loginUseCase
    private val pinCodeUseCase = ServiceLocator.pinCodeUseCase

    /** Куда перейти после успешного входа. */
    enum class Next { CREATE_PIN, MAIN }

    private val _state = MutableLiveData<UiState<Next>>(UiState.Idle)

    /** Состояние экрана для наблюдения из Activity. */
    val state: LiveData<UiState<Next>> = _state

    private val _emailError = MutableLiveData<String?>()

    /** Текст ошибки под полем почты или null. */
    val emailError: LiveData<String?> = _emailError

    /**
     * Проверяет почту по шаблону name@domenname.ru.
     * @param email введённая почта
     * @return true, если почта корректна
     */
    fun validateEmail(email: String): Boolean {
        val error = if (email.isBlank()) null else EmailValidator.validate(email)
        _emailError.value = error
        return error == null
    }

    /**
     * Выполняет вход.
     * @param email почта
     * @param password пароль
     */
    fun login(email: String, password: String) {
        if (!validateEmail(email)) return
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = loginUseCase(email, password)) {
                is DomainResult.Success ->
                    UiState.Success(if (pinCodeUseCase.hasPin()) Next.MAIN else Next.CREATE_PIN)
                is DomainResult.Error -> UiState.Error(result.message, result.isAuthError)
            }
        }
    }

    /** Сбрасывает состояние после показа ошибки. */
    fun consumeState() {
        _state.value = UiState.Idle
    }
}
