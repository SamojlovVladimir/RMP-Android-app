package ru.samoylov.matule.data.repository

import ru.samoylov.matule.data.local.HomeCache
import ru.samoylov.matule.data.mapper.toDomain
import ru.samoylov.matule.data.mapper.toDomainResult
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Product
import ru.samoylov.matule.domain.model.Promo
import ru.samoylov.matule.domain.repository.CatalogRepository
import ru.samoylov.matule.network.MatuleApiService

/**
 * Реализация репозитория каталога.
 *
 * Работает по схеме «сеть с откатом на кэш»: при успешном ответе данные
 * обновляются и сохраняются, при ошибке сети возвращается последнее
 * сохранённое состояние, чтобы экран не оставался пустым.
 *
 * @param api абстракция сетевого слоя
 * @param cache кэш главной страницы
 */
class CatalogRepositoryImpl(
    private val api: MatuleApiService,
    private val cache: HomeCache
) : CatalogRepository {

    override suspend fun getPromos(forceRefresh: Boolean): DomainResult<List<Promo>> {
        if (!forceRefresh) {
            val cached = cache.getPromos()
            if (cached.isNotEmpty()) return DomainResult.Success(cached)
        }

        val result = api.getPromos().toDomainResult { list -> list.map { it.toDomain() } }
        if (result is DomainResult.Success) {
            cache.savePromos(result.data)
            return result
        }

        val fallback = cache.getPromos()
        return if (fallback.isNotEmpty()) DomainResult.Success(fallback) else result
    }

    override suspend fun getCatalog(
        category: String,
        forceRefresh: Boolean
    ): DomainResult<List<Product>> {
        if (!forceRefresh) {
            val cached = cache.getProducts(category)
            if (cached.isNotEmpty()) return DomainResult.Success(cached)
        }

        val result = api.getCatalog(category.takeIf { it.isNotBlank() })
            .toDomainResult { list -> list.map { it.toDomain() } }

        if (result is DomainResult.Success) {
            cache.saveProducts(category, result.data)
            return result
        }

        val fallback = cache.getProducts(category)
        return if (fallback.isNotEmpty()) DomainResult.Success(fallback) else result
    }

    override suspend fun search(query: String): DomainResult<List<Product>> =
        api.search(query).toDomainResult { list -> list.map { it.toDomain() } }

    override suspend fun getProduct(productId: String): DomainResult<Product> =
        api.getProduct(productId).toDomainResult { it.toDomain() }
}
