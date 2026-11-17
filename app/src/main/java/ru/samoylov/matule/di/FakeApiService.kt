package ru.samoylov.matule.di

import kotlinx.coroutines.delay
import ru.samoylov.matule.network.MatuleApiService
import ru.samoylov.matule.network.NetworkError
import ru.samoylov.matule.network.NetworkResult
import ru.samoylov.matule.network.TokenStore
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
 * Тестовая реализация MatuleApiService: вместо сервера данные хранятся в памяти.
 *
 * Реализует тот же интерфейс, что и RetrofitApiService, поэтому включается
 * одним флагом ServiceLocator.USE_FAKE_API и больше нигде ничего не меняется —
 * в этом и смысл абстракции сетевого слоя.
 *
 * Для проверки уведомления об ошибке: войдите с почтой, которая начинается
 * на «error» (например error@mail.ru) — вернётся ошибка «нет сети».
 *
 * @param tokenStore хранилище токена, как у настоящей реализации
 */
class FakeApiService(private val tokenStore: TokenStore) : MatuleApiService {

    private val products = listOf(
        ProductDto(
            "prod1", "Рубашка Воскресенье для машинного вязания", "men",
            DESCRIPTION, 300.0, "80-90 г", ""
        ),
        ProductDto(
            "prod2", "Шорты Вторник для машинного вязания", "men",
            DESCRIPTION, 300.0, "70-90 г", ""
        ),
        ProductDto(
            "prod3", "Платье Пятница для ручного вязания", "women",
            DESCRIPTION, 690.0, "300-350 г", ""
        ),
        ProductDto(
            "prod4", "Кардиган Суббота для машинного вязания", "women",
            DESCRIPTION, 1200.0, "400-450 г", ""
        ),
        ProductDto(
            "prod5", "Шапка Понедельник для детей", "kids",
            DESCRIPTION, 250.0, "50-60 г", ""
        ),
        ProductDto(
            "prod6", "Шарф Среда с резинкой 1x1", "accessories",
            DESCRIPTION, 450.0, "120-150 г", ""
        )
    )

    private val cart = mutableListOf<CartItemDto>()
    private var nextId = 1

    private val projects = mutableListOf(
        ProjectDto(
            id = "proj_0", title = "Мой первый проект", type = "Личный",
            startDate = "01.10.2026", endDate = "30.10.2026", target = "Себе",
            source = "example.com", category = "Одежда", photo = "", updated = "Прошло 2 дня"
        )
    )

    private var profile = UserDto(
        id = "fake_user_1",
        email = "test@mail.ru",
        name = "Владимир",
        surname = "Тишкин",
        patronymic = "Олегович",
        birthDate = "28.02.1991",
        gender = "Мужской"
    )

    override suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        delay(400)
        if (email.startsWith("error")) {
            return NetworkResult.Failure(
                NetworkError.NO_CONNECTION,
                "Нет соединения с интернетом. Проверьте подключение и повторите попытку"
            )
        }
        profile = profile.copy(email = email)
        tokenStore.saveToken("fake_token")
        return NetworkResult.Success(AuthResponse(token = "fake_token", record = profile))
    }

    override suspend fun register(email: String, password: String): NetworkResult<UserDto> {
        delay(400)
        profile = UserDto(id = "fake_user_1", email = email)
        return NetworkResult.Success(profile)
    }

    override suspend fun logout(): NetworkResult<Unit> {
        delay(100)
        tokenStore.clearToken()
        return NetworkResult.Success(Unit)
    }

    override suspend fun getProfile(userId: String): NetworkResult<UserDto> {
        delay(300)
        return NetworkResult.Success(profile)
    }

    override suspend fun updateProfile(
        userId: String,
        request: UpdateProfileRequest
    ): NetworkResult<UserDto> {
        delay(300)
        profile = profile.copy(
            name = request.name ?: profile.name,
            surname = request.surname ?: profile.surname,
            patronymic = request.patronymic ?: profile.patronymic,
            birthDate = request.birthDate ?: profile.birthDate,
            gender = request.gender ?: profile.gender,
            email = request.email ?: profile.email
        )
        return NetworkResult.Success(profile)
    }

    override suspend fun getPromos(): NetworkResult<List<PromoDto>> {
        delay(400)
        return NetworkResult.Success(
            listOf(
                PromoDto("p1", "Шорты Вторник", "", 4000.0, ""),
                PromoDto("p2", "Рубашка Воскресенье", "", 8000.0, "")
            )
        )
    }

    override suspend fun getCatalog(category: String?): NetworkResult<List<ProductDto>> {
        delay(400)
        return NetworkResult.Success(
            if (category.isNullOrBlank()) products else products.filter { it.category == category }
        )
    }

    override suspend fun search(query: String): NetworkResult<List<ProductDto>> {
        delay(300)
        return NetworkResult.Success(products.filter { it.title.orEmpty().contains(query, true) })
    }

    override suspend fun getProduct(productId: String): NetworkResult<ProductDto> {
        delay(200)
        val product = products.firstOrNull { it.id == productId }
            ?: return NetworkResult.Failure(NetworkError.NOT_FOUND, "Товар не найден")
        return NetworkResult.Success(product)
    }

    override suspend fun getCart(): NetworkResult<List<CartItemDto>> {
        delay(200)
        return NetworkResult.Success(cart.toList())
    }

    override suspend fun addToCart(request: AddToCartRequest): NetworkResult<CartItemDto> {
        delay(200)
        val existing = cart.firstOrNull { it.product == request.product }
        if (existing != null) return NetworkResult.Success(existing)

        val product = products.firstOrNull { it.id == request.product }
            ?: return NetworkResult.Failure(NetworkError.NOT_FOUND, "Товар не найден")
        val item = CartItemDto(
            id = "cart_${nextId++}",
            product = product.id,
            title = product.title,
            price = product.price,
            quantity = request.quantity
        )
        cart.add(item)
        return NetworkResult.Success(item)
    }

    override suspend fun updateCartItem(cartItemId: String, quantity: Int): NetworkResult<CartItemDto> {
        delay(150)
        val index = cart.indexOfFirst { it.id == cartItemId }
        if (index == -1) return NetworkResult.Failure(NetworkError.NOT_FOUND, "Позиция не найдена")
        val updated = cart[index].copy(quantity = quantity)
        cart[index] = updated
        return NetworkResult.Success(updated)
    }

    override suspend fun removeFromCart(cartItemId: String): NetworkResult<Unit> {
        delay(150)
        cart.removeAll { it.id == cartItemId }
        return NetworkResult.Success(Unit)
    }

    override suspend fun createOrder(request: CreateOrderRequest): NetworkResult<OrderDto> {
        delay(500)
        cart.clear()
        return NetworkResult.Success(
            OrderDto(id = "order_${nextId++}", total = request.total, created = "now")
        )
    }

    override suspend fun getProjects(): NetworkResult<List<ProjectDto>> {
        delay(300)
        return NetworkResult.Success(projects.toList())
    }

    override suspend fun createProject(request: CreateProjectRequest): NetworkResult<ProjectDto> {
        delay(400)
        val project = ProjectDto(
            id = "proj_${nextId++}",
            title = request.title,
            type = request.type,
            startDate = request.startDate,
            endDate = request.endDate,
            target = request.target,
            source = request.source,
            category = request.category,
            photo = "",
            updated = "Только что"
        )
        projects.add(0, project)
        return NetworkResult.Success(project)
    }

    private companion object {
        const val DESCRIPTION =
            "Мой выбор для этих шапок – кардные составы, которые раскрываются деликатным " +
                "пушком. Кашемиры, меринос, смесовки с ними отлично подойдут на шапку.\n" +
                "Кардные составы берите в большое количество сложений, вязать будем " +
                "резинку 1x1, плотненько.\n" +
                "Пряжу 1400-1500м в 100г в 4 сложения, пряжу 700м в 2 сложения. " +
                "Ориентир для конечной толщины – 300-350м в 100г.\n" +
                "Артикулы, из которых мы вязали эту модель: Zermatt Zegna Baruffa, " +
                "Cashfive, Baby Cashmere Loro Piana, Soft Donegal и другие.\n" +
                "Примерный расход на шапку с подгибом 70-90г."
    }
}
