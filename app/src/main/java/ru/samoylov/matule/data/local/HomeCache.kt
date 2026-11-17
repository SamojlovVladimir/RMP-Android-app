package ru.samoylov.matule.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.samoylov.matule.domain.model.Product
import ru.samoylov.matule.domain.model.Promo

/**
 * Кэш главной страницы.
 *
 * Позволяет показать данные сразу при открытии экрана, не дожидаясь сети,
 * и не оставляет пользователя с пустым экраном при потере соединения.
 *
 * @param storage постоянное хранилище
 */
class HomeCache(private val storage: PreferencesStorage) {

    private val gson = Gson()

    /**
     * Сохраняет акции в кэш.
     * @param promos список акций
     */
    fun savePromos(promos: List<Promo>) = storage.saveCache(KEY_PROMOS, promos)

    /** Читает акции из кэша; пустой список, если кэша нет. */
    fun getPromos(): List<Promo> {
        val json = storage.getCacheJson(KEY_PROMOS) ?: return emptyList()
        val type = object : TypeToken<List<Promo>>() {}.type
        return runCatching { gson.fromJson<List<Promo>>(json, type) }.getOrNull().orEmpty()
    }

    /**
     * Сохраняет товары в кэш.
     * @param category категория, для которой сохраняются товары
     * @param products список товаров
     */
    fun saveProducts(category: String, products: List<Product>) =
        storage.saveCache(KEY_PRODUCTS + category, products)

    /**
     * Читает товары из кэша.
     * @param category категория, для которой нужны товары
     */
    fun getProducts(category: String): List<Product> {
        val json = storage.getCacheJson(KEY_PRODUCTS + category) ?: return emptyList()
        val type = object : TypeToken<List<Product>>() {}.type
        return runCatching { gson.fromJson<List<Product>>(json, type) }.getOrNull().orEmpty()
    }

    private companion object {
        const val KEY_PROMOS = "home_promos"
        const val KEY_PRODUCTS = "home_products_"
    }
}
