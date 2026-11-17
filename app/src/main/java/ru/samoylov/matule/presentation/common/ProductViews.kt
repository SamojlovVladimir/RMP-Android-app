package ru.samoylov.matule.presentation.common

import android.content.Context
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import ru.samoylov.matule.R
import ru.samoylov.matule.domain.model.Category
import ru.samoylov.matule.domain.model.Product
import ru.samoylov.matule.uikit.AppBottomSheet
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppCard
import ru.samoylov.matule.uikit.AppChip
import ru.samoylov.matule.uikit.AppIcon

/**
 * Общие элементы экранов «Главная» и «Каталог»: карточка товара
 * и BottomSheet с описанием. Вынесены сюда, чтобы не дублировать код (DRY).
 */
object ProductViews {

    /**
     * Форматирует цену: 2490.0 → «2490 ₽».
     * @param price цена
     */
    fun formatPrice(price: Double): String = "${price.toLong()} ₽"

    /**
     * Заполняет ряд чипсов категориями; выбранная подсвечивается.
     * @param container горизонтальный контейнер для чипсов
     * @param selected выбранная категория
     * @param onSelect вызывается при выборе другой категории
     */
    fun fillChips(container: LinearLayout, selected: Category, onSelect: (Category) -> Unit) {
        container.removeAllViews()
        val margin = (12 * container.resources.displayMetrics.density).toInt()
        Category.values().forEachIndexed { index, category ->
            val chip = AppChip(container.context)
            chip.text = category.title
            chip.setChipSelected(category == selected)
            chip.setOnClickListener {
                fillChips(container, category, onSelect)
                onSelect(category)
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            if (index > 0) params.marginStart = margin
            container.addView(chip, params)
        }
    }

    /**
     * Перерисовывает список товаров.
     * @param container вертикальный контейнер
     * @param products товары
     * @param isInCart проверка, лежит ли товар в корзине
     * @param onCartClick нажатие «Добавить»/«Убрать»
     * @param onCardClick нажатие на карточку
     */
    fun fillProducts(
        container: LinearLayout,
        products: List<Product>,
        isInCart: (Product) -> Boolean,
        onCartClick: (Product) -> Unit,
        onCardClick: (Product) -> Unit
    ) {
        container.removeAllViews()
        val margin = (16 * container.resources.displayMetrics.density).toInt()
        products.forEach { product ->
            val card = createCard(
                container.context, product, isInCart(product),
                onCartClick = { onCartClick(product) },
                onCardClick = { onCardClick(product) }
            )
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = margin
            container.addView(card, params)
        }
    }

    /**
     * Создаёт карточку товара с кнопкой «Добавить» или «Убрать».
     * @param context контекст
     * @param product товар
     * @param inCart лежит ли товар в корзине
     * @param onCartClick нажатие на кнопку карточки
     * @param onCardClick нажатие на саму карточку (открыть описание)
     */
    fun createCard(
        context: Context,
        product: Product,
        inCart: Boolean,
        onCartClick: () -> Unit,
        onCardClick: () -> Unit
    ): AppCard {
        val card = AppCard(context)
        card.setTitle(product.title)
        card.setSubtitle(Category.fromServer(product.category).caption)
        card.setPrice(formatPrice(product.price))
        val button = if (inCart) {
            AppButton.small(context, "Убрать", AppButton.VARIANT_OUTLINE)
        } else {
            AppButton.small(context, "Добавить")
        }
        button.setOnClickListener { onCartClick() }
        card.setActionView(button)
        card.setOnClickListener { onCardClick() }
        return card
    }

    /**
     * Показывает BottomSheet с описанием товара.
     * @param context контекст
     * @param product товар
     * @param inCart лежит ли товар в корзине
     * @param onCartClick нажатие на кнопку «Добавить за …» / «Убрать из корзины»
     */
    fun showDetails(
        context: Context,
        product: Product,
        inCart: Boolean,
        onCartClick: () -> Unit
    ) {
        val sheet = AppBottomSheet(context)
        val view = LayoutInflater.from(context).inflate(R.layout.sheet_product, null)

        view.findViewById<TextView>(R.id.productTitle).text = product.title
        view.findViewById<TextView>(R.id.productDescription).text = product.description
        view.findViewById<TextView>(R.id.productConsumption).text = product.consumption
        view.findViewById<AppIcon>(R.id.closeIcon).setOnClickListener { sheet.dismiss() }

        val button = view.findViewById<AppButton>(R.id.productButton)
        if (inCart) {
            button.text = "Убрать из корзины"
            button.setVariant(AppButton.VARIANT_OUTLINE)
        } else {
            button.text = "Добавить за ${formatPrice(product.price)}"
        }
        button.setOnClickListener {
            onCartClick()
            sheet.dismiss()
        }

        sheet.setContent(view)
        sheet.show()
    }
}
