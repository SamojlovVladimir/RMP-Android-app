package ru.samoylov.matule.uikit

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

/**
 * AppPinPad — ввод кода из четырёх цифр: ряд точек и круглая цифровая клавиатура.
 * Используется на экранах «Вход» (по коду) и «Создайте пароль».
 *
 * - точки заполняются синим по мере ввода
 * - клавиша при нажатии становится синей с белой цифрой
 * - справа от «0» — стирание последней цифры
 * - слева от «0» — необязательная клавиша (например, отпечаток пальца)
 *
 * Использование:
 * pinPad.setOnPinEnteredListener { pin -> ... }
 * pinPad.showError()        // красные точки, встряхивание и очистка
 * pinPad.setExtraKey(AppIcons.FINGERPRINT) { ... }
 */
class AppPinPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val dots = mutableListOf<View>()
    private val dotsRow: LinearLayout
    private val extraKey: AppIcon
    private val entered = StringBuilder()
    private var onPinEntered: ((String) -> Unit)? = null
    private val density = resources.displayMetrics.density

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        dotsRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        repeat(PIN_LENGTH) { index ->
            val dot = View(context)
            val params = LayoutParams(dp(16), dp(16))
            if (index > 0) params.marginStart = dp(12)
            dotsRow.addView(dot, params)
            dots.add(dot)
        }
        addView(dotsRow, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))

        // Ряды клавиш: 1-2-3, 4-5-6, 7-8-9, [доп.]-0-[стереть]
        extraKey = createIconKey(AppIcons.FINGERPRINT).apply { visibility = INVISIBLE }
        val backspaceKey = createIconKey(AppIcons.BACKSPACE).apply {
            background = null
            setOnClickListener { removeLastDigit() }
        }
        addRow(listOf(digitKey("1"), digitKey("2"), digitKey("3")), dp(56))
        addRow(listOf(digitKey("4"), digitKey("5"), digitKey("6")), dp(24))
        addRow(listOf(digitKey("7"), digitKey("8"), digitKey("9")), dp(24))
        addRow(listOf(extraKey, digitKey("0"), backspaceKey), dp(24))

        updateDots()
    }

    /**
     * Назначает обработчик ввода всех четырёх цифр.
     * @param listener получает введённый код
     */
    fun setOnPinEnteredListener(listener: (String) -> Unit) {
        onPinEntered = listener
    }

    /** Стирает введённые цифры. */
    fun clear() {
        entered.clear()
        updateDots()
    }

    /** Показывает ошибку: точки краснеют, ряд встряхивается, ввод очищается. */
    fun showError() {
        dots.forEach { it.setBackgroundResource(R.drawable.bg_pin_dot_error) }
        dotsRow.startAnimation(TranslateAnimation(-dp(10).toFloat(), dp(10).toFloat(), 0f, 0f).apply {
            duration = 60
            repeatCount = 5
            repeatMode = Animation.REVERSE
        })
        Handler(Looper.getMainLooper()).postDelayed({ clear() }, 500)
    }

    /**
     * Показывает дополнительную клавишу слева от «0».
     * @param glyph иконка клавиши из AppIcons
     * @param listener вызывается при нажатии
     */
    fun setExtraKey(glyph: String, listener: () -> Unit) {
        extraKey.setIcon(glyph)
        extraKey.visibility = VISIBLE
        extraKey.setOnClickListener { listener() }
    }

    private fun addRow(keys: List<View>, topMargin: Int) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        keys.forEachIndexed { index, key ->
            val params = LayoutParams(dp(KEY_SIZE_DP), dp(KEY_SIZE_DP))
            if (index > 0) params.marginStart = dp(24)
            row.addView(key, params)
        }
        val rowParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        rowParams.topMargin = topMargin
        addView(row, rowParams)
    }

    private fun digitKey(digit: String): View = AppCompatTextView(context).apply {
        text = digit
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        setTextColor(ContextCompat.getColorStateList(context, R.color.pin_key_text))
        setBackgroundResource(R.drawable.bg_pin_key)
        isClickable = true
        isFocusable = true
        setOnClickListener { addDigit(digit) }
    }

    private fun createIconKey(glyph: String): AppIcon = AppIcon(context).apply {
        setIcon(glyph)
        setIconSize(28f)
        setIconColor(ContextCompat.getColor(context, R.color.black))
        isClickable = true
        isFocusable = true
    }

    private fun addDigit(digit: String) {
        if (entered.length >= PIN_LENGTH) return
        entered.append(digit)
        updateDots()
        if (entered.length == PIN_LENGTH) onPinEntered?.invoke(entered.toString())
    }

    private fun removeLastDigit() {
        if (entered.isNotEmpty()) entered.deleteCharAt(entered.length - 1)
        updateDots()
    }

    private fun updateDots() {
        dots.forEachIndexed { index, dot ->
            dot.setBackgroundResource(
                if (index < entered.length) R.drawable.bg_pin_dot_filled
                else R.drawable.bg_pin_dot_empty
            )
        }
    }

    private fun dp(value: Int): Int = (value * density).toInt()

    companion object {
        /** Длина кода быстрого входа */
        const val PIN_LENGTH = 4

        private const val KEY_SIZE_DP = 76
    }
}
