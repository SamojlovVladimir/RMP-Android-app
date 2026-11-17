package ru.samoylov.matule.uikit

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * AppCartButton — синяя кнопка «В корзину» с иконкой тележки слева
 * и суммой корзины справа (компонент «Cart» из раздела кнопок макета).
 * Фон общий с AppButton (bg_button_primary).
 *
 * Использование:
 * cartButton.setTotal("500 ₽")
 * cartButton.setOnClickListener { ... }
 */
class AppCartButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val totalText: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.bg_button_primary)
        isClickable = true
        isFocusable = true

        val density = resources.displayMetrics.density
        minimumHeight = (56 * density).toInt()
        setPadding((20 * density).toInt(), 0, (20 * density).toInt(), 0)
        val white = ContextCompat.getColor(context, R.color.white)

        addView(AppIcon(context).apply {
            setIcon(AppIcons.CART)
            setIconColor(white)
        })

        addView(createText("В корзину", white).apply {
            setPadding((16 * density).toInt(), 0, 0, 0)
        }, LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))

        totalText = createText("", white)
        addView(totalText)
    }

    /**
     * Меняет сумму справа.
     * @param text сумма, например «500 ₽»
     */
    fun setTotal(text: String) {
        totalText.text = text
    }

    private fun createText(value: String, color: Int): TextView = TextView(context).apply {
        text = value
        setTextColor(color)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        typeface = Typeface.DEFAULT_BOLD
    }
}
