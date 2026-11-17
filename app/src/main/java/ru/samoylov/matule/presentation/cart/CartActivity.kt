package ru.samoylov.matule.presentation.cart

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import ru.samoylov.matule.R
import ru.samoylov.matule.domain.model.CartItem
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.presentation.common.ProductViews
import ru.samoylov.matule.presentation.common.UiState
import ru.samoylov.matule.presentation.main.MainActivity
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppCard
import ru.samoylov.matule.uikit.AppCounter
import ru.samoylov.matule.uikit.AppHeader
import ru.samoylov.matule.uikit.AppIcons

/**
 * Экран «Корзина». Позиции можно удалить крестиком и изменить счётчиком.
 * По «Перейти к оформлению заказа» показывается уведомление об успешном
 * заказе на 5 секунд, после чего открывается «Главная».
 */
class CartActivity : BaseActivity() {

    override val screenName = "cart"

    private val viewModel: CartViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var cartContainer: LinearLayout
    private lateinit var emptyText: TextView
    private lateinit var totalRow: View
    private lateinit var totalText: TextView
    private lateinit var orderButton: AppButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartContainer = findViewById(R.id.cartContainer)
        emptyText = findViewById(R.id.emptyText)
        totalRow = findViewById(R.id.totalRow)
        totalText = findViewById(R.id.totalText)
        orderButton = findViewById(R.id.orderButton)

        val header = findViewById<AppHeader>(R.id.header)
        header.setOnBackClickListener { finish() }
        header.setActionIcon(AppIcons.TRASH) { viewModel.clear() }

        orderButton.setOnClickListener { viewModel.createOrder() }

        viewModel.items.observe(this) { items -> showItems(items) }
        viewModel.error.observe(this) { message ->
            if (message != null) showMessage(message) { viewModel.clearError() }
        }
        viewModel.orderState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> orderButton.isEnabled = false
                is UiState.Success -> onOrderCreated()
                is UiState.Error -> {
                    orderButton.isEnabled = true
                    showMessage(state.message) { viewModel.clearError() }
                }
                is UiState.Idle -> Unit
            }
        }

        viewModel.load()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun showItems(items: List<CartItem>) {
        cartContainer.removeAllViews()
        val margin = (16 * resources.displayMetrics.density).toInt()
        items.forEach { item ->
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = margin
            cartContainer.addView(createCard(item), params)
        }
        updateTotal()
        emptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        totalRow.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        orderButton.isEnabled = items.isNotEmpty()
    }

    private fun createCard(item: CartItem): AppCard {
        val card = AppCard(this)
        card.setTitle(item.title)
        card.setPrice(ProductViews.formatPrice(item.price))
        card.setOnCloseListener { viewModel.remove(item) }
        val counter = AppCounter(this)
        counter.setCount(item.quantity)
        counter.setOnCountChangedListener { count ->
            viewModel.changeQuantity(item, count)
        }
        card.setActionView(counter)
        return card
    }

    private fun updateTotal() {
        totalText.text = ProductViews.formatPrice(viewModel.total())
    }

    /** Уведомление на 5 секунд, затем «Главная». */
    private fun onOrderCreated() {
        showMessage("Заказ успешно оформлен") { openHome() }
        handler.postDelayed({ openHome() }, ORDER_MESSAGE_MS)
    }

    private fun openHome() {
        if (isFinishing) return
        handler.removeCallbacksAndMessages(null)
        startActivity(MainActivity.createIntent(this, MainActivity.TAB_HOME))
        finish()
    }

    private companion object {
        const val ORDER_MESSAGE_MS = 5000L
    }
}
