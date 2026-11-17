package ru.samoylov.matule.domain.model

/**
 * Доменные модели приложения.
 *
 * Самый внутренний слой архитектуры: не зависит ни от Android,
 * ни от сетевой библиотеки, ни от базы данных. Меняется только тогда,
 * когда меняется суть предметной области.
 */

/**
 * Пользователь приложения.
 * @param id идентификатор на сервере
 * @param email почта, она же логин
 * @param name имя
 * @param surname фамилия
 * @param patronymic отчество
 * @param birthDate дата рождения в виде текста
 * @param gender пол
 */
data class User(
    val id: String,
    val email: String,
    val name: String = "",
    val surname: String = "",
    val patronymic: String = "",
    val birthDate: String = "",
    val gender: String = ""
) {
    /** Отображаемое имя: имя или почта, если имя ещё не заполнено. */
    val displayName: String
        get() = if (name.isNotBlank()) name else email
}

/**
 * Акция для главного экрана.
 * @param id идентификатор
 * @param title заголовок
 * @param subtitle подзаголовок
 * @param price цена
 * @param imageUrl адрес картинки
 */
data class Promo(
    val id: String,
    val title: String,
    val subtitle: String,
    val price: Double,
    val imageUrl: String
)

/**
 * Товар каталога.
 * @param id идентификатор
 * @param title название
 * @param category категория для фильтрации
 * @param description подробное описание
 * @param price цена
 * @param consumption примерный расход материала
 * @param imageUrl адрес картинки
 */
data class Product(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val price: Double,
    val consumption: String,
    val imageUrl: String
)

/**
 * Позиция корзины.
 * @param id идентификатор позиции
 * @param productId идентификатор товара
 * @param title название товара
 * @param price цена за единицу
 * @param quantity количество
 */
data class CartItem(
    val id: String,
    val productId: String,
    val title: String,
    val price: Double,
    val quantity: Int
) {
    /** Стоимость позиции с учётом количества. */
    val totalPrice: Double
        get() = price * quantity
}

/**
 * Проект пользователя.
 * @param id идентификатор
 * @param title название
 * @param type тип проекта
 * @param startDate дата начала
 * @param endDate дата окончания
 * @param target для кого проект
 * @param source источник описания
 * @param category категория
 * @param photoUrl адрес фотографии
 * @param updatedAt дата последнего изменения
 */
data class Project(
    val id: String,
    val title: String,
    val type: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val target: String = "",
    val source: String = "",
    val category: String = "",
    val photoUrl: String = "",
    val updatedAt: String = ""
)

/**
 * Оформленный заказ.
 * @param id идентификатор
 * @param total итоговая сумма
 */
data class Order(
    val id: String,
    val total: Double
)

/**
 * Категория каталога для фильтрации на главном экране.
 * @param serverValue значение категории на сервере
 * @param title надпись на чипсе
 * @param caption подпись категории в карточке товара
 */
enum class Category(val serverValue: String, val title: String, val caption: String) {
    ALL("", "Все", ""),
    WOMEN("women", "Женщинам", "Женская одежда"),
    MEN("men", "Мужчинам", "Мужская одежда"),
    KIDS("kids", "Детям", "Детская одежда"),
    ACCESSORIES("accessories", "Аксессуары", "Аксессуары");

    companion object {
        /**
         * Находит категорию по значению с сервера.
         * @param value значение категории в ответе сервера
         */
        fun fromServer(value: String): Category =
            values().firstOrNull { it.serverValue == value } ?: ALL
    }
}
