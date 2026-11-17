package ru.samoylov.matule.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.presentation.common.UiState

/**
 * Логика экрана «Профиль»: загрузка данных пользователя и выход.
 */
class ProfileViewModel : ViewModel() {

    private val getProfileUseCase = ServiceLocator.getProfileUseCase
    private val logoutUseCase = ServiceLocator.logoutUseCase

    private val _state = MutableLiveData<UiState<User>>(UiState.Idle)

    /** Состояние загрузки профиля. */
    val state: LiveData<UiState<User>> = _state

    private val _loggedOut = MutableLiveData(false)

    /** true после выхода из аккаунта. */
    val loggedOut: LiveData<Boolean> = _loggedOut

    /** Загружает профиль с сервера. */
    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = getProfileUseCase()) {
                is DomainResult.Success -> UiState.Success(result.data)
                is DomainResult.Error -> UiState.Error(result.message, result.isAuthError)
            }
        }
    }

    /** Выходит из аккаунта и очищает сохранённую сессию. */
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.value = true
        }
    }
}
