package ru.samoylov.matule.network.retrofit

import ru.samoylov.matule.network.MatuleApiService
import ru.samoylov.matule.network.NetworkResult
import ru.samoylov.matule.network.TokenStore
import ru.samoylov.matule.network.dto.AddToCartRequest
import ru.samoylov.matule.network.dto.AuthResponse
import ru.samoylov.matule.network.dto.CartItemDto
import ru.samoylov.matule.network.dto.CreateOrderRequest
import ru.samoylov.matule.network.dto.CreateProjectRequest
import ru.samoylov.matule.network.dto.LoginRequest
import ru.samoylov.matule.network.dto.OrderDto
import ru.samoylov.matule.network.dto.ProductDto
import ru.samoylov.matule.network.dto.ProjectDto
import ru.samoylov.matule.network.dto.PromoDto
import ru.samoylov.matule.network.dto.RegisterRequest
import ru.samoylov.matule.network.dto.UpdateCartRequest
import ru.samoylov.matule.network.dto.UpdateProfileRequest
import ru.samoylov.matule.network.dto.UserDto

/**
 * Реализация сетевого слоя на Retrofit.
 *
 * Класс закрыт от внешнего мира: приложение получает его как MatuleApiService
 * через NetworkProvider и не знает про Retrofit. Чтобы заменить библиотеку,
 * достаточно написать другой класс с этим же интерфейсом.
 *
 * @param api описание HTTP-запросов
 * @param tokenStore хранилище токена, очищается при выходе
 */
internal class RetrofitApiService(
    private val api: RetrofitApi,
    private val tokenStore: TokenStore
) : MatuleApiService {

    override suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        val result = ErrorHandler.safeCall { api.login(LoginRequest(email, password)) }
        if (result is NetworkResult.Success) {
            tokenStore.saveToken(result.data.token)
        }
        return result
    }

    override suspend fun register(email: String, password: String): NetworkResult<UserDto> =
        ErrorHandler.safeCall {
            api.register(RegisterRequest(email, password, password))
        }

    override suspend fun logout(): NetworkResult<Unit> {
        tokenStore.clearToken()
        return NetworkResult.Success(Unit)
    }

    override suspend fun getProfile(userId: String): NetworkResult<UserDto> =
        ErrorHandler.safeCall { api.getProfile(userId) }

    override suspend fun updateProfile(
        userId: String,
        request: UpdateProfileRequest
    ): NetworkResult<UserDto> =
        ErrorHandler.safeCall { api.updateProfile(userId, request) }

    override suspend fun getPromos(): NetworkResult<List<PromoDto>> =
        unwrapList(ErrorHandler.safeCall { api.getPromos() })

    override suspend fun getCatalog(category: String?): NetworkResult<List<ProductDto>> {
        val filter = category?.takeIf { it.isNotBlank() }?.let { "category=\"$it\"" }
        return unwrapList(ErrorHandler.safeCall { api.getCatalog(filter) })
    }

    override suspend fun search(query: String): NetworkResult<List<ProductDto>> {
        val filter = "title~\"$query\""
        return unwrapList(ErrorHandler.safeCall { api.getCatalog(filter) })
    }

    override suspend fun getProduct(productId: String): NetworkResult<ProductDto> =
        ErrorHandler.safeCall { api.getProduct(productId) }

    override suspend fun getCart(): NetworkResult<List<CartItemDto>> =
        unwrapList(ErrorHandler.safeCall { api.getCart() })

    override suspend fun addToCart(request: AddToCartRequest): NetworkResult<CartItemDto> =
        ErrorHandler.safeCall { api.addToCart(request) }

    override suspend fun updateCartItem(
        cartItemId: String,
        quantity: Int
    ): NetworkResult<CartItemDto> =
        ErrorHandler.safeCall { api.updateCartItem(cartItemId, UpdateCartRequest(quantity)) }

    override suspend fun removeFromCart(cartItemId: String): NetworkResult<Unit> =
        ErrorHandler.safeCall { api.removeFromCart(cartItemId) }

    override suspend fun createOrder(request: CreateOrderRequest): NetworkResult<OrderDto> =
        ErrorHandler.safeCall { api.createOrder(request) }

    override suspend fun getProjects(): NetworkResult<List<ProjectDto>> =
        unwrapList(ErrorHandler.safeCall { api.getProjects() })

    override suspend fun createProject(request: CreateProjectRequest): NetworkResult<ProjectDto> =
        ErrorHandler.safeCall { api.createProject(request) }

    /**
     * Достаёт список из постраничной обёртки сервера.
     * Вынесено в отдельный метод, чтобы не повторять один и тот же
     * разбор в каждом запросе со списком (DRY).
     */
    private fun <T> unwrapList(
        result: NetworkResult<ru.samoylov.matule.network.dto.ListResponse<T>>
    ): NetworkResult<List<T>> = when (result) {
        is NetworkResult.Success -> NetworkResult.Success(result.data.items)
        is NetworkResult.Failure -> result
    }
}
