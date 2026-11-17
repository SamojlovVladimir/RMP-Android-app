package ru.samoylov.matule.presentation.projects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.presentation.common.UiState

/**
 * Логика экрана «Список проектов».
 */
class ProjectsViewModel : ViewModel() {

    private val getProjectsUseCase = ServiceLocator.getProjectsUseCase

    private val _projects = MutableLiveData<List<Project>>()

    /** Проекты пользователя (null — ещё не загружены). */
    val projects: LiveData<List<Project>> = _projects

    private val _error = MutableLiveData<String?>()

    /** Текст ошибки для уведомления или null. */
    val error: LiveData<String?> = _error

    /** Загружает список проектов. */
    fun load() {
        viewModelScope.launch {
            when (val result = getProjectsUseCase()) {
                is DomainResult.Success -> _projects.value = result.data
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

    /** Сбрасывает ошибку после показа уведомления. */
    fun clearError() {
        _error.value = null
    }
}

/**
 * Логика экрана «Создание проекта». Все поля обязательны:
 * кнопка «Подтвердить» активна, только когда всё заполнено и выбрано фото.
 * Форма хранится черновиком и переживает перезапуск приложения.
 */
class CreateProjectViewModel : ViewModel() {

    private val createProjectUseCase = ServiceLocator.createProjectUseCase
    private val draftUseCase = ServiceLocator.projectDraftUseCase

    private val _state = MutableLiveData<UiState<Project>>(UiState.Idle)

    /** Состояние отправки проекта. */
    val state: LiveData<UiState<Project>> = _state

    /** Путь к файлу выбранного фото, пустая строка — фото нет. */
    var photoPath = ""

    /** Возвращает сохранённый черновик или null. */
    fun restoreDraft(): Project? = draftUseCase.restore()?.also { photoPath = it.photoUrl }

    /**
     * Сохраняет текущее состояние формы.
     * @param project значения полей формы
     */
    fun saveDraft(project: Project) = draftUseCase.save(project)

    /**
     * Все ли поля заполнены.
     * @param project значения полей формы
     */
    fun isComplete(project: Project): Boolean =
        project.type.isNotBlank() && project.title.isNotBlank() &&
            project.startDate.length == DATE_LENGTH && project.endDate.length == DATE_LENGTH &&
            project.target.isNotBlank() && project.source.isNotBlank() &&
            project.category.isNotBlank() && project.photoUrl.isNotBlank()

    /**
     * Отправляет проект на сервер.
     * @param project значения полей формы
     */
    fun create(project: Project) {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = when (val result = createProjectUseCase(project)) {
                is DomainResult.Success -> {
                    draftUseCase.clear()
                    UiState.Success(result.data)
                }
                is DomainResult.Error -> UiState.Error(result.message, result.isAuthError)
            }
        }
    }

    /** Сбрасывает состояние после показа ошибки. */
    fun consumeState() {
        _state.value = UiState.Idle
    }

    private companion object {
        /** Длина даты в формате ДД.ММ.ГГГГ */
        const val DATE_LENGTH = 10
    }
}
