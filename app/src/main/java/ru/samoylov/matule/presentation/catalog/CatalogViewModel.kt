package ru.samoylov.matule.presentation.catalog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.CartItem
import ru.samoylov.matule.domain.model.Category
import ru.samoylov.matule.domain.model.DomainResult
import ru.samoylov.matule.domain.model.Product
import ru.samoylov.matule.domain.model.Promo

/**
 * Логика экранов «Главная» и «Каталог»: акции, товары с фильтром
 * по категории, поиск и добавление в корзину.
 *
 * Оба экрана показывают один и тот же список товаров, поэтому логика
 * общая (DRY), а у каждого экрана свой экземпляр ViewModel.
 */
class CatalogViewModel : ViewModel() {

    private val getHomeDataUseCase = ServiceLocator.getHomeDataUseCase
    private val getCatalogUseCase = ServiceLocator.getCatalogUseCase
    private val searchProductsUseCase = ServiceLocator.searchProductsUseCase
    private val manageCartUseCase = ServiceLocator.manageCartUseCase

    private val _promos = MutableLiveData<List<Promo>>(emptyList())

    /** Акции и новости (только для главной). */
    val promos: LiveData<List<Promo>> = _promos

    private val _products = MutableLiveData<List<Product>>()

    /** Товары с учётом категории и поиска (null — ещё не загружены). */
    val products: LiveData<List<Product>> = _products

    private val _cart = MutableLiveData<List<CartItem>>(emptyList())

    /** Текущий состав корзины. */
    val cart: LiveData<List<CartItem>> = _cart

    private val _error = MutableLiveData<String?>()

    /** Текст ошибки для уведомления или null. */
    val error: LiveData<String?> = _error

    /** Выбранная категория. */
    var category: Category = Category.ALL
        private set

    private var query = ""
    private var searchJob: Job? = null

    /** Загружает главную: акции и товары выбранной категории. */
    fun loadHome() {
        viewModelScope.launch {
            when (val result = getHomeDataUseCase(category.serverValue)) {
                is DomainResult.Success -> {
                    _promos.value = result.data.promos
                    if (query.isEmpty()) _products.value = result.data.products
                }
                is DomainResult.Error -> _error.value = result.message
            }
        }
        loadCart()
    }

    /** Загружает каталог товаров выбранной категории. */
    fun loadCatalog() {
        loadProducts()
        loadCart()
    }

    /**
     * Меняет категорию и перезагружает список.
     * @param newCategory выбранная категория
     */
    fun selectCategory(newCategory: Category) {
        category = newCategory
        loadProducts()
    }

    /**
     * Ищет товары по названию. Запрос отправляется с небольшой паузой,
     * чтобы не дёргать сервер на каждую букву.
     * @param newQuery поисковый запрос
     */
    fun search(newQuery: String) {
        query = newQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MS)
            loadProducts()
        }
    }

    /**
     * Добавляет товар в корзину или убирает его, если он уже там.
     * @param product товар
     */
    fun toggleCart(product: Product) {
        viewModelScope.launch {
            val item = _cart.value.orEmpty().firstOrNull { it.productId == product.id }
            val result: DomainResult<Unit> = if (item != null) {
                manageCartUseCase.remove(item.id)
            } else {
                when (val added = manageCartUseCase.add(product.id)) {
                    is DomainResult.Success -> DomainResult.Success(Unit)
                    is DomainResult.Error -> added
                }
            }
            if (result is DomainResult.Error) _error.value = result.message
            loadCart()
        }
    }

    /** Обновляет состав корзины. */
    fun loadCart() {
        viewModelScope.launch {
            when (val result = manageCartUseCase.getCart()) {
                is DomainResult.Success -> _cart.value = result.data
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

    /**
     * Лежит ли товар в корзине.
     * @param product товар
     */
    fun isInCart(product: Product): Boolean =
        _cart.value.orEmpty().any { it.productId == product.id }

    /** Сумма корзины. */
    fun cartTotal(): Double = manageCartUseCase.calculateTotal(_cart.value.orEmpty())

    /** Сбрасывает ошибку после показа уведомления. */
    fun clearError() {
        _error.value = null
    }

    private fun loadProducts() {
        viewModelScope.launch {
            val result = if (query.isEmpty()) {
                getCatalogUseCase(category.serverValue)
            } else {
                searchProductsUseCase(query)
            }
            when (result) {
                is DomainResult.Success -> _products.value = filterByCategory(result.data)
                is DomainResult.Error -> _error.value = result.message
            }
        }
    }

    /** Поиск идёт по всему каталогу, поэтому категорию применяем ещё раз. */
    private fun filterByCategory(list: List<Product>): List<Product> =
        if (category == Category.ALL) list else list.filter { it.category == category.serverValue }

    private companion object {
        const val SEARCH_DELAY_MS = 300L
    }
}
