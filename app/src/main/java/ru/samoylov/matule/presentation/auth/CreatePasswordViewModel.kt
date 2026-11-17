package ru.samoylov.matule.presentation.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.domain.usecase.PasswordValidator
import ru.samoylov.matule.presentation.common.UiState

/**
 * Логика экрана «Создание пароля» профиля.
 * Проверяет надёжность пароля и завершает регистрацию на сервере.
 */
class CreatePasswordViewModel : ViewModel() {

    private val completeRegistrationUseCase = ServiceLocator.completeRegistrationUseCase

    private val _state = MutableLiveData<UiState<User>>(UiState.Idle)

    /** Состояние экрана. */
    val state: LiveData<UiState<User>> = _state

    private val _passwordError = MutableLiveData<String?>()

    /** Ошибка под полем нового пароля. */
    val passwordError: LiveData<String?> = _passwordError

    private val _confirmError = MutableLiveData<String?>()

    /** Ошибка под полем повтора пароля. */
    val confirmError: LiveData<String?> = _confirmError

    /**
     * Проверяет пароли и создаёт аккаунт.
     * @param password новый пароль
     * @param confirmation повтор пароля
     */
    fun save(password: String, confirmation: String) {
        val passwordError = PasswordValidator.validate(password)
        val confirmError = PasswordValidator.validateConfirmation(password, confirmation)
        _passwordError.value = passwordError
        _confirmError.value = if (passwordError == null) confirmError else null
        if (passwordError != null || confirmError != null) return

        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = completeRegistrationUseCase(password, confirmation)) {
                is DomainResult.Success -> UiState.Success(result.data)
                is DomainResult.Error -> UiState.Error(result.message, result.isAuthError)
            }
        }
    }

    /** Сбрасывает состояние после показа ошибки. */
    fun consumeState() {
        _state.value = UiState.Idle
    }
}
