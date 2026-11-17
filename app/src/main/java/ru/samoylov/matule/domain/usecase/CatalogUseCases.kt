package ru.samoylov.matule.domain.usecase

import ru.samoylov.matule.domain.model.CartItem
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Order
import ru.samoylov.matule.domain.model.Product
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.domain.model.Promo
import ru.samoylov.matule.domain.repository.CartRepository
import ru.samoylov.matule.domain.repository.CatalogRepository
import ru.samoylov.matule.domain.repository.ProjectRepository

/**
 * Сценарии работы с каталогом, корзиной и проектами.
 */

/**
 * Загрузка данных главного экрана: акции и каталог одним вызовом.
 * @param repository репозиторий каталога
 */
class GetHomeDataUseCase(private val repository: CatalogRepository) {

    /**
     * Содержимое главного экрана.
     * @param promos список акций
     * @param products список товаров
     */
    data class HomeData(
        val promos: List<Promo>,
        val products: List<Product>
    )

    /**
     * @param category выбранная категория; пустая строка — все товары
     * @param forceRefresh true — обновить данные из сети, минуя кэш
     */
    suspend operator fun invoke(
        category: String = "",
        forceRefresh: Boolean = false
    ): DomainResult<HomeData> {
        val promosResult = repository.getPromos(forceRefresh)
        if (promosResult is DomainResult.Error) return promosResult

        val productsResult = repository.getCatalog(category, forceRefresh)
        if (productsResult is DomainResult.Error) return productsResult

        return DomainResult.Success(
            HomeData(
                promos = (promosResult as DomainResult.Success).data,
                products = (productsResult as DomainResult.Success).data
            )
        )
    }
}

/**
 * Загрузка каталога с фильтром по категории.
 * @param repository репозиторий каталога
 */
class GetCatalogUseCase(private val repository: CatalogRepository) {

    /**
     * @param category выбранная категория; пустая строка — все товары
     * @param forceRefresh true — обновить данные из сети, минуя кэш
     */
    suspend operator fun invoke(
        category: String = "",
        forceRefresh: Boolean = false
    ): DomainResult<List<Product>> = repository.getCatalog(category, forceRefresh)
}

/**
 * Поиск товаров по названию.
 * @param repository репозиторий каталога
 */
class SearchProductsUseCase(private val repository: CatalogRepository) {

    /**
     * @param query поисковый запрос; короткие запросы возвращают пустой список
     */
    suspend operator fun invoke(query: String): DomainResult<List<Product>> {
        if (query.isBlank()) return DomainResult.Success(emptyList())
        return repository.search(query.trim())
    }
}

/**
 * Получение описания товара для карточки.
 * @param repository репозиторий каталога
 */
class GetProductDetailsUseCase(private val repository: CatalogRepository) {

    /** @param productId идентификатор товара */
    suspend operator fun invoke(productId: String): DomainResult<Product> =
        repository.getProduct(productId)
}

/**
 * Управление составом корзины.
 * @param repository репозиторий корзины
 */
class ManageCartUseCase(private val repository: CartRepository) {

    /** Загружает текущее содержимое корзины. */
    suspend fun getCart(): DomainResult<List<CartItem>> = repository.getCart()

    /**
     * Добавляет товар в корзину.
     * @param productId идентификатор товара
     */
    suspend fun add(productId: String): DomainResult<CartItem> =
        repository.addToCart(productId)

    /**
     * Меняет количество позиции; количество меньше единицы удаляет позицию.
     * @param cartItemId идентификатор позиции
     * @param quantity новое количество
     */
    suspend fun changeQuantity(cartItemId: String, quantity: Int): DomainResult<Unit> =
        if (quantity < 1) {
            repository.removeFromCart(cartItemId)
        } else {
            when (val result = repository.updateQuantity(cartItemId, quantity)) {
                is DomainResult.Success -> DomainResult.Success(Unit)
                is DomainResult.Error -> result
            }
        }

    /**
     * Удаляет позицию из корзины.
     * @param cartItemId идентификатор позиции
     */
    suspend fun remove(cartItemId: String): DomainResult<Unit> =
        repository.removeFromCart(cartItemId)

    /**
     * Считает итоговую сумму корзины.
     * @param items позиции корзины
     */
    fun calculateTotal(items: List<CartItem>): Double = items.sumOf { it.totalPrice }
}

/**
 * Оформление заказа.
 * @param repository репозиторий корзины
 */
class CreateOrderUseCase(private val repository: CartRepository) {

    /** @param items позиции, которые нужно оформить */
    suspend operator fun invoke(items: List<CartItem>): DomainResult<Order> {
        if (items.isEmpty()) return DomainResult.Error("Корзина пуста")
        return repository.createOrder(items)
    }
}

/**
 * Список проектов пользователя.
 * @param repository репозиторий проектов
 */
class GetProjectsUseCase(private val repository: ProjectRepository) {

    suspend operator fun invoke(): DomainResult<List<Project>> = repository.getProjects()
}

/**
 * Создание проекта с проверкой обязательных полей.
 * @param repository репозиторий проектов
 */
class CreateProjectUseCase(private val repository: ProjectRepository) {

    /** @param project заполненный проект */
    suspend operator fun invoke(project: Project): DomainResult<Project> {
        RequiredFieldValidator.validate(project.type, "Тип")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(project.title, "Название проекта")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(project.startDate, "Дата начала")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(project.endDate, "Дата окончания")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(project.target, "Кому")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(project.source, "Источник описания")
            ?.let { return DomainResult.Error(it) }
        RequiredFieldValidator.validate(project.category, "Категория")
            ?.let { return DomainResult.Error(it) }
        return repository.createProject(project)
    }
}
