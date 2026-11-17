package ru.samoylov.matule.data.repository

import ru.samoylov.matule.data.mapper.toDomain
import ru.samoylov.matule.data.mapper.toDomainResult
import ru.samoylov.matule.domain.model.CartItem
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Order
import ru.samoylov.matule.domain.repository.CartRepository
import ru.samoylov.matule.network.MatuleApiService
import ru.samoylov.matule.network.dto.AddToCartRequest
import ru.samoylov.matule.network.dto.CreateOrderRequest

/**
 * Реализация репозитория корзины.
 *
 * @param api абстракция сетевого слоя
 */
class CartRepositoryImpl(
    private val api: MatuleApiService
) : CartRepository {

    override suspend fun getCart(): DomainResult<List<CartItem>> =
        api.getCart().toDomainResult { list -> list.map { it.toDomain() } }

    override suspend fun addToCart(productId: String): DomainResult<CartItem> =
        api.addToCart(AddToCartRequest(productId)).toDomainResult { it.toDomain() }

    override suspend fun updateQuantity(
        cartItemId: String,
        quantity: Int
    ): DomainResult<CartItem> =
        api.updateCartItem(cartItemId, quantity).toDomainResult { it.toDomain() }

    override suspend fun removeFromCart(cartItemId: String): DomainResult<Unit> =
        api.removeFromCart(cartItemId).toDomainResult { }

    override suspend fun createOrder(items: List<CartItem>): DomainResult<Order> {
        val request = CreateOrderRequest(
            items = items.map { it.id },
            total = items.sumOf { it.totalPrice }
        )
        return api.createOrder(request).toDomainResult { Order(it.id, it.total) }
    }
}
