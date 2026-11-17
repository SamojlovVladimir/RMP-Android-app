package ru.samoylov.matule.network

import ru.samoylov.matule.network.dto.AddToCartRequest
import ru.samoylov.matule.network.dto.AuthResponse
import ru.samoylov.matule.network.dto.CartItemDto
import ru.samoylov.matule.network.dto.CreateOrderRequest
import ru.samoylov.matule.network.dto.CreateProjectRequest
import ru.samoylov.matule.network.dto.OrderDto
import ru.samoylov.matule.network.dto.ProductDto
import ru.samoylov.matule.network.dto.ProjectDto
import ru.samoylov.matule.network.dto.PromoDto
import ru.samoylov.matule.network.dto.UpdateProfileRequest
import ru.samoylov.matule.network.dto.UserDto

/**
 * УРОВЕНЬ АБСТРАКЦИИ сетевого слоя.
 *
 * Приложение вызывает только методы этого интерфейса и ничего не знает
 * о том, какой библиотекой сделан запрос. Замена Retrofit на любую другую
 * реализацию сводится к написанию нового класса, реализующего этот интерфейс,
 * без единой правки в остальных слоях приложения (принцип инверсии
 * зависимостей из SOLID).
 *
 * Все методы возвращают NetworkResult, поэтому ошибки не бросаются
 * исключениями, а приходят как обычное значение.
 */
interface MatuleApiService {

    // ===================== АВТОРИЗАЦИЯ =====================

    /**
     * Авторизация пользователя.
     * @param email почта пользователя
     * @param password пароль
     * @return токен доступа и профиль пользователя
     */
    suspend fun login(email: String, password: String): NetworkResult<AuthResponse>

    /**
     * Создание нового пользователя.
     * @param email почта пользователя
     * @param password пароль
     * @return созданный профиль
     */
    suspend fun register(email: String, password: String): NetworkResult<UserDto>

    /**
     * Выход из аккаунта: сброс токена на стороне клиента.
     */
    suspend fun logout(): NetworkResult<Unit>

    // ===================== ПРОФИЛЬ =====================

    /**
     * Получение информации о профиле.
     * @param userId идентификатор пользователя
     */
    suspend fun getProfile(userId: String): NetworkResult<UserDto>

    /**
     * Изменение профиля.
     * @param userId идентификатор пользователя
     * @param request поля, которые нужно изменить
     */
    suspend fun updateProfile(
        userId: String,
        request: UpdateProfileRequest
    ): NetworkResult<UserDto>

    // ===================== КАТАЛОГ =====================

    /**
     * Получение списка акций для главного экрана.
     */
    suspend fun getPromos(): NetworkResult<List<PromoDto>>

    /**
     * Получение каталога товаров.
     * @param category фильтр по категории; null — все категории
     */
    suspend fun getCatalog(category: String? = null): NetworkResult<List<ProductDto>>

    /**
     * Поиск товаров по названию.
     * @param query поисковый запрос
     */
    suspend fun search(query: String): NetworkResult<List<ProductDto>>

    /**
     * Получение подробного описания товара.
     * @param productId идентификатор товара
     */
    suspend fun getProduct(productId: String): NetworkResult<ProductDto>

    // ===================== КОРЗИНА =====================

    /**
     * Получение текущего состава корзины.
     */
    suspend fun getCart(): NetworkResult<List<CartItemDto>>

    /**
     * Добавление товара в корзину.
     * @param request идентификатор товара и количество
     */
    suspend fun addToCart(request: AddToCartRequest): NetworkResult<CartItemDto>

    /**
     * Изменение количества позиции в корзине.
     * @param cartItemId идентификатор позиции корзины
     * @param quantity новое количество
     */
    suspend fun updateCartItem(cartItemId: String, quantity: Int): NetworkResult<CartItemDto>

    /**
     * Удаление позиции из корзины.
     * @param cartItemId идентификатор позиции корзины
     */
    suspend fun removeFromCart(cartItemId: String): NetworkResult<Unit>

    /**
     * Оформление заказа.
     * @param request состав заказа и итоговая сумма
     */
    suspend fun createOrder(request: CreateOrderRequest): NetworkResult<OrderDto>

    // ===================== ПРОЕКТЫ =====================

    /**
     * Получение списка проектов пользователя.
     */
    suspend fun getProjects(): NetworkResult<List<ProjectDto>>

    /**
     * Создание проекта.
     * @param request поля нового проекта
     */
    suspend fun createProject(request: CreateProjectRequest): NetworkResult<ProjectDto>
}
