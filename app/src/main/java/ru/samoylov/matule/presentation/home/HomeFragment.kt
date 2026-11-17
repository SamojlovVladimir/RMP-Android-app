package ru.samoylov.matule.presentation.home

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.samoylov.matule.R
import ru.samoylov.matule.domain.model.Promo
import ru.samoylov.matule.presentation.catalog.CatalogViewModel
import ru.samoylov.matule.presentation.common.ProductViews
import ru.samoylov.matule.presentation.common.dp
import ru.samoylov.matule.presentation.common.showMessage
import ru.samoylov.matule.uikit.AppSearch

/**
 * Экран «Главная»: поиск, акции и новости, чипсы категорий и каталог описаний.
 * При выборе категории список товаров фильтруется.
 * Данные главной кэшируются (HomeCache), поэтому при повторном открытии
 * и без сети экран сразу заполнен.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var viewModel: CatalogViewModel
    private lateinit var promoContainer: LinearLayout
    private lateinit var chipsContainer: LinearLayout
    private lateinit var productsContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CatalogViewModel::class.java]

        promoContainer = view.findViewById(R.id.promoContainer)
        chipsContainer = view.findViewById(R.id.chipsContainer)
        productsContainer = view.findViewById(R.id.productsContainer)
        emptyText = view.findViewById(R.id.emptyText)

        view.findViewById<AppSearch>(R.id.searchView).setOnQueryChangedListener { query ->
            viewModel.search(query)
        }
        ProductViews.fillChips(chipsContainer, viewModel.category) { viewModel.selectCategory(it) }

        viewModel.promos.observe(viewLifecycleOwner) { showPromos(it) }
        viewModel.products.observe(viewLifecycleOwner) { showProducts() }
        viewModel.cart.observe(viewLifecycleOwner) { showProducts() }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) showMessage(message) { viewModel.clearError() }
        }

        viewModel.loadHome()
    }

    override fun onResume() {
        super.onResume()
        // После возврата из корзины её состав мог измениться
        viewModel.loadCart()
    }

    private fun showProducts() {
        val products = viewModel.products.value.orEmpty()
        ProductViews.fillProducts(
            productsContainer, products,
            isInCart = { viewModel.isInCart(it) },
            onCartClick = { viewModel.toggleCart(it) },
            onCardClick = { product ->
                ProductViews.showDetails(requireContext(), product, viewModel.isInCart(product)) {
                    viewModel.toggleCart(product)
                }
            }
        )
        val loadedEmpty = viewModel.products.value?.isEmpty() == true
        emptyText.visibility = if (loadedEmpty) View.VISIBLE else View.GONE
    }

    /** Карточки акций: цветной фон, название сверху, цена снизу. */
    private fun showPromos(promos: List<Promo>) {
        promoContainer.removeAllViews()
        promos.forEachIndexed { index, promo ->
            val card = FrameLayout(requireContext()).apply {
                background = GradientDrawable().apply {
                    cornerRadius = dp(12).toFloat()
                    setColor(Color.parseColor(PROMO_COLORS[index % PROMO_COLORS.size]))
                }
                setPadding(dp(16), dp(16), dp(16), dp(16))
            }
            card.addView(createPromoText(promo.title, 20f), FrameLayout.LayoutParams(
                dp(160), FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.START
            ))
            card.addView(createPromoText(ProductViews.formatPrice(promo.price), 20f),
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM or Gravity.START
                )
            )
            val params = LinearLayout.LayoutParams(dp(270), dp(152))
            if (index > 0) params.marginStart = dp(16)
            promoContainer.addView(card, params)
        }
    }

    private fun createPromoText(value: String, size: Float): TextView =
        TextView(requireContext()).apply {
            text = value
            textSize = size
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

    private companion object {
        /** Цвета карточек акций из макета: бирюзовый и голубой */
        val PROMO_COLORS = listOf("#97D9E9", "#76B3FF")
    }
}
