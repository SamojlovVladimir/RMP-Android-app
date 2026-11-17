package ru.samoylov.matule.network.retrofit

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.samoylov.matule.network.dto.AddToCartRequest
import ru.samoylov.matule.network.dto.AuthResponse
import ru.samoylov.matule.network.dto.CartItemDto
import ru.samoylov.matule.network.dto.CreateOrderRequest
import ru.samoylov.matule.network.dto.CreateProjectRequest
import ru.samoylov.matule.network.dto.ListResponse
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
 * Описание HTTP-запросов для Retrofit.
 *
 * Это внутренняя деталь реализации: наружу интерфейс не выставляется,
 * приложение работает через MatuleApiService. Пути запросов собраны
 * в одном месте и правятся под спецификацию OpenAPI с площадки.
 */
internal interface RetrofitApi {

    @POST("api/collections/users/auth-with-password")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/collections/users/records")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>

    @GET("api/collections/users/records/{id}")
    suspend fun getProfile(@Path("id") userId: String): Response<UserDto>

    @PATCH("api/collections/users/records/{id}")
    suspend fun updateProfile(
        @Path("id") userId: String,
        @Body request: UpdateProfileRequest
    ): Response<UserDto>

    @GET("api/collections/promos/records")
    suspend fun getPromos(): Response<ListResponse<PromoDto>>

    @GET("api/collections/products/records")
    suspend fun getCatalog(@Query("filter") filter: String?): Response<ListResponse<ProductDto>>

    @GET("api/collections/products/records/{id}")
    suspend fun getProduct(@Path("id") productId: String): Response<ProductDto>

    @GET("api/collections/cart/records")
    suspend fun getCart(): Response<ListResponse<CartItemDto>>

    @POST("api/collections/cart/records")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<CartItemDto>

    @PATCH("api/collections/cart/records/{id}")
    suspend fun updateCartItem(
        @Path("id") cartItemId: String,
        @Body request: UpdateCartRequest
    ): Response<CartItemDto>

    @DELETE("api/collections/cart/records/{id}")
    suspend fun removeFromCart(@Path("id") cartItemId: String): Response<Unit>

    @POST("api/collections/orders/records")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderDto>

    @GET("api/collections/projects/records")
    suspend fun getProjects(): Response<ListResponse<ProjectDto>>

    @POST("api/collections/projects/records")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ProjectDto>
}
