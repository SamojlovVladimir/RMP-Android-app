package ru.samoylov.matule.domain.repository

import ru.samoylov.matule.domain.model.CartItem
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Order
import ru.samoylov.matule.domain.model.Product
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.domain.model.Promo
import ru.samoylov.matule.domain.model.User

/**
 * Интерфейсы репозиториев — граница доменного слоя.
 *
 * Домен объявляет, ЧТО ему нужно, а слой data решает, КАК это получить:
 * из сети, из кэша или из локального хранилища. Экраны зависят только
 * от этих интерфейсов, поэтому замена источника данных не затрагивает
 * ни один экран (инверсия зависимостей из SOLID).
 */

/** Авторизация и сессия пользователя. */
interface AuthRepository {

    /**
     * Вход по почте и паролю.
     * @param email почта
     * @param password пароль
     */
    suspend fun login(email: String, password: String): DomainResult<User>

    /**
     * Регистрация нового пользователя.
     * @param email почта
     * @param password пароль
     */
    suspend fun register(email: String, password: String): DomainResult<User>

    /** Выход из аккаунта и очистка сохранённой сессии. */
    suspend fun logout(): DomainResult<Unit>

    /** Проверяет, сохранена ли активная сессия после перезапуска приложения. */
    fun isLoggedIn(): Boolean

    /** Возвращает идентификатор текущего пользователя или null. */
    fun getCurrentUserId(): String?

    /**
     * Сохраняет код быстрого входа в приложение.
     * @param pin код из четырёх цифр
     */
    fun savePinCode(pin: String)

    /**
     * Проверяет введённый код быстрого входа.
     * @param pin введённый код
     */
    fun checkPinCode(pin: String): Boolean

    /** Установлен ли код быстрого входа. */
    fun hasPinCode(): Boolean
}

/** Работа с профилем пользователя. */
interface ProfileRepository {

    /** Загружает профиль текущего пользователя. */
    suspend fun getProfile(): DomainResult<User>

    /**
     * Сохраняет изменения профиля на сервере.
     * @param user профиль с новыми значениями
     */
    suspend fun updateProfile(user: User): DomainResult<User>

    /**
     * Сохраняет незаконченное заполнение профиля,
     * чтобы данные не потерялись после перезапуска приложения.
     * @param user черновик профиля
     */
    fun saveDraft(user: User)

    /** Возвращает сохранённый черновик профиля или null. */
    fun getDraft(): User?

    /** Удаляет черновик после успешного сохранения. */
    fun clearDraft()
}

/** Каталог товаров и акции. */
interface CatalogRepository {

    /**
     * Загружает акции для главного экрана.
     * @param forceRefresh true — обязательно идти в сеть, минуя кэш
     */
    suspend fun getPromos(forceRefresh: Boolean = false): DomainResult<List<Promo>>

    /**
     * Загружает каталог товаров.
     * @param category фильтр по категории; пустая строка — все товары
     * @param forceRefresh true — обязательно идти в сеть, минуя кэш
     */
    suspend fun getCatalog(
        category: String = "",
        forceRefresh: Boolean = false
    ): DomainResult<List<Product>>

    /**
     * Ищет товары по названию.
     * @param query поисковый запрос
     */
    suspend fun search(query: String): DomainResult<List<Product>>

    /**
     * Загружает подробное описание товара.
     * @param productId идентификатор товара
     */
    suspend fun getProduct(productId: String): DomainResult<Product>
}

/** Корзина и оформление заказа. */
interface CartRepository {

    /** Возвращает текущий состав корзины. */
    suspend fun getCart(): DomainResult<List<CartItem>>

    /**
     * Добавляет товар в корзину.
     * @param productId идентификатор товара
     */
    suspend fun addToCart(productId: String): DomainResult<CartItem>

    /**
     * Меняет количество позиции.
     * @param cartItemId идентификатор позиции
     * @param quantity новое количество
     */
    suspend fun updateQuantity(cartItemId: String, quantity: Int): DomainResult<CartItem>

    /**
     * Удаляет позицию из корзины.
     * @param cartItemId идентификатор позиции
     */
    suspend fun removeFromCart(cartItemId: String): DomainResult<Unit>

    /**
     * Оформляет заказ из переданных позиций.
     * @param items позиции корзины
     */
    suspend fun createOrder(items: List<CartItem>): DomainResult<Order>
}

/** Проекты пользователя. */
interface ProjectRepository {

    /** Загружает список проектов. */
    suspend fun getProjects(): DomainResult<List<Project>>

    /**
     * Создаёт новый проект.
     * @param project заполненные поля проекта
     */
    suspend fun createProject(project: Project): DomainResult<Project>

    /**
     * Сохраняет черновик экрана «Создание проекта».
     * @param project значения полей формы
     */
    fun saveDraft(project: Project)

    /** Возвращает черновик проекта или null. */
    fun getDraft(): Project?

    /** Удаляет черновик проекта. */
    fun clearDraft()
}
