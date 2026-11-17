package ru.samoylov.matule.network.dto

import com.google.gson.annotations.SerializedName

/**
 * Модели, приходящие от сервера и уходящие на сервер.
 * Живут только внутри сетевого слоя: наружу репозитории отдают доменные модели.
 */

/** Запрос авторизации. @param identity email пользователя. @param password пароль */
data class LoginRequest(
    val identity: String,
    val password: String
)

/** Ответ авторизации. @param token токен доступа. @param record данные пользователя */
data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("record") val record: UserDto
)

/** Запрос регистрации нового пользователя. */
data class RegisterRequest(
    val email: String,
    val password: String,
    val passwordConfirm: String,
    val name: String = ""
)

/** Запрос изменения профиля. Поля необязательные: отправляются только изменённые. */
data class UpdateProfileRequest(
    val name: String? = null,
    val surname: String? = null,
    val patronymic: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val email: String? = null
)

/** Пользователь. */
data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("surname") val surname: String? = null,
    @SerializedName("patronymic") val patronymic: String? = null,
    @SerializedName("birthDate") val birthDate: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("avatar") val avatar: String? = null
)

/** Обёртка постраничного ответа сервера. */
data class ListResponse<T>(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("perPage") val perPage: Int = 30,
    @SerializedName("totalItems") val totalItems: Int = 0,
    @SerializedName("items") val items: List<T> = emptyList()
)

/** Акция на главном экране. */
data class PromoDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("image") val image: String? = null
)

/** Товар каталога. */
data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("consumption") val consumption: String? = null,
    @SerializedName("image") val image: String? = null
)

/** Позиция корзины. */
data class CartItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("product") val product: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("price") val price: Double = 0.0,
    @SerializedName("quantity") val quantity: Int = 1
)

/** Запрос добавления товара в корзину. */
data class AddToCartRequest(
    val product: String,
    val quantity: Int = 1
)

/** Запрос изменения количества позиции в корзине. */
data class UpdateCartRequest(
    val quantity: Int
)

/** Запрос оформления заказа. */
data class CreateOrderRequest(
    val items: List<String>,
    val total: Double
)

/** Оформленный заказ. */
data class OrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("created") val created: String? = null
)

/** Проект пользователя. */
data class ProjectDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("target") val target: String? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("photo") val photo: String? = null,
    @SerializedName("updated") val updated: String? = null
)

/** Запрос создания проекта. */
data class CreateProjectRequest(
    val title: String,
    val type: String,
    val startDate: String,
    val endDate: String,
    val target: String,
    val source: String,
    val category: String
)

/** Тело ошибки, которое возвращает сервер. */
data class ErrorDto(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("message") val message: String? = null
)
