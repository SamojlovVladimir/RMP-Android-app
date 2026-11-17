package ru.samoylov.matule.data.repository

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
 */
class ProjectRepositoryImpl(
    private val api: MatuleApiService
) : ProjectRepository {

    override suspend fun getProjects(): DomainResult<List<Project>> =
        api.getProjects().toDomainResult { list -> list.map { it.toDomain() } }

    override suspend fun createProject(project: Project): DomainResult<Project> =
        api.createProject(project.toCreateRequest()).toDomainResult { it.toDomain() }
}
