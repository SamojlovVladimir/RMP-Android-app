package ru.samoylov.matule.presentation.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.CartItem
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Order
import ru.samoylov.matule.presentation.common.UiState

/**
 * Логика экрана «Корзина»: изменение количества, удаление позиций,
 * очистка корзины и оформление заказа.
 */
class CartViewModel : ViewModel() {

    private val manageCartUseCase = ServiceLocator.manageCartUseCase
    private val createOrderUseCase = ServiceLocator.createOrderUseCase

    private val _items = MutableLiveData<List<CartItem>>()

    /** Позиции корзины (null — ещё не загружены). */
    val items: LiveData<List<CartItem>> = _items

    private val _orderState = MutableLiveData<UiState<Order>>(UiState.Idle)

    /** Состояние оформления заказа. */
    val orderState: LiveData<UiState<Order>> = _orderState

    private val _error = MutableLiveData<String?>()

    /** Текст ошибки для уведомления или null. */
    val error: LiveData<String?> = _error

    /** Загружает состав корзины. */
    fun load() {
        viewModelScope.launch {
            when (val result = manageCartUseCase.getCart()) {
                is DomainResult.Success -> _items.value = result.data
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

    /**
     * Меняет количество позиции.
     * @param item позиция
     * @param quantity новое количество
     */
    fun changeQuantity(item: CartItem, quantity: Int) {
        // Сразу пересчитываем сумму на экране, не дожидаясь сервера
        _items.value = _items.value.orEmpty().map {
            if (it.id == item.id) it.copy(quantity = quantity) else it
        }
        viewModelScope.launch {
            val result = manageCartUseCase.changeQuantity(item.id, quantity)
            if (result is DomainResult.Error) {
                _error.value = result.message
                load()
            }
        }
    }

    /**
     * Удаляет позицию из корзины.
     * @param item позиция
     */
    fun remove(item: CartItem) {
        viewModelScope.launch {
            val result = manageCartUseCase.remove(item.id)
            if (result is DomainResult.Error) _error.value = result.message
            load()
        }
    }

    /** Удаляет все позиции (иконка корзины в хэдере). */
    fun clear() {
        viewModelScope.launch {
            _items.value.orEmpty().forEach { manageCartUseCase.remove(it.id) }
            load()
        }
    }

    /** Сумма корзины. */
    fun total(): Double = manageCartUseCase.calculateTotal(_items.value.orEmpty())

    /** Оформляет заказ из всех позиций корзины. */
    fun createOrder() {
        _orderState.value = UiState.Loading
        viewModelScope.launch {
            _orderState.value = when (val result = createOrderUseCase(_items.value.orEmpty())) {
                is DomainResult.Success -> UiState.Success(result.data)
                is DomainResult.Error -> UiState.Error(result.message, result.isAuthError)
            }
        }
    }

    /** Сбрасывает ошибку после показа уведомления. */
    fun clearError() {
        _error.value = null
        if (_orderState.value is UiState.Error) _orderState.value = UiState.Idle
    }
}
