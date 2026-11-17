package ru.samoylov.matule.data.repository

import ru.samoylov.matule.data.local.PreferencesStorage
import ru.samoylov.matule.data.mapper.toCreateRequest
import ru.samoylov.matule.data.mapper.toDomain
import ru.samoylov.matule.data.mapper.toDomainResult
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.domain.repository.ProjectRepository
import ru.samoylov.matule.network.MatuleApiService

/**
 * Реализация репозитория проектов.
 *
 * @param api абстракция сетевого слоя
 * @param storage локальное хранилище для черновика
 */
class ProjectRepositoryImpl(
    private val api: MatuleApiService,
    private val storage: PreferencesStorage
) : ProjectRepository {

    override suspend fun getProjects(): DomainResult<List<Project>> =
        api.getProjects().toDomainResult { list -> list.map { it.toDomain() } }

    override suspend fun createProject(project: Project): DomainResult<Project> =
        api.createProject(project.toCreateRequest()).toDomainResult { it.toDomain() }

    override fun saveDraft(project: Project) = storage.saveProjectDraft(project)

    override fun getDraft(): Project? = storage.getProjectDraft()

    override fun clearDraft() = storage.clearProjectDraft()
}
