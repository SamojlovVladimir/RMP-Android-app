package ru.samoylov.matule.presentation.catalog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.samoylov.matule.R
import ru.samoylov.matule.presentation.cart.CartActivity
import ru.samoylov.matule.presentation.common.ProductViews
import ru.samoylov.matule.presentation.common.showMessage
import ru.samoylov.matule.presentation.main.MainActivity
import ru.samoylov.matule.uikit.AppCartButton
import ru.samoylov.matule.uikit.AppIcon
import ru.samoylov.matule.uikit.AppSearch

/**
 * Экран «Каталог»: поиск, чипсы категорий, добавление и удаление из корзины.
 * Когда в корзине что-то есть, снизу появляется кнопка «В корзину» с суммой.
 * Нажатие на карточку открывает BottomSheet с описанием товара.
 */
class CatalogFragment : Fragment(R.layout.fragment_catalog) {

    private lateinit var viewModel: CatalogViewModel
    private lateinit var productsContainer: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var cartButton: AppCartButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CatalogViewModel::class.java]

        productsContainer = view.findViewById(R.id.productsContainer)
        emptyText = view.findViewById(R.id.emptyText)
        cartButton = view.findViewById(R.id.cartButton)

        view.findViewById<AppSearch>(R.id.searchView).setOnQueryChangedListener { query ->
            viewModel.search(query)
        }
        view.findViewById<AppIcon>(R.id.profileIcon).setOnClickListener {
            (activity as? MainActivity)?.openTab(MainActivity.TAB_PROFILE)
        }
        ProductViews.fillChips(view.findViewById(R.id.chipsContainer), viewModel.category) {
            viewModel.selectCategory(it)
        }
        cartButton.setOnClickListener {
            startActivity(Intent(requireContext(), CartActivity::class.java))
        }

        viewModel.products.observe(viewLifecycleOwner) { showProducts() }
        viewModel.cart.observe(viewLifecycleOwner) { cart ->
            showProducts()
            cartButton.visibility = if (cart.isEmpty()) View.GONE else View.VISIBLE
            cartButton.setTotal(ProductViews.formatPrice(viewModel.cartTotal()))
        }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) showMessage(message) { viewModel.clearError() }
        }

        viewModel.loadCatalog()
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
}
