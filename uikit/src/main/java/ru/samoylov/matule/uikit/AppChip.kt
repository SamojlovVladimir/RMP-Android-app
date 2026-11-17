package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat

/**
 * AppChip — чипс-фильтр UIKit (категории «Все», «Женщинам», «Мужчинам» и т.п.).
 *
 * Состояния:
 * - выбран    — синяя заливка, белый текст
 * - не выбран — серая заливка, серый текст
 *
 * Атрибуты XML:
 * app:chipText     — надпись на чипсе
 * app:chipSelected — выбран ли чипс изначально
 */
class AppChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var isChipSelected = false

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppChip)
        val chipText = typedArray.getString(R.styleable.AppChip_chipText)
        isChipSelected = typedArray.getBoolean(R.styleable.AppChip_chipSelected, false)
        typedArray.recycle()

        val density = resources.displayMetrics.density
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        minHeight = (48 * density).toInt()
        setPadding((20 * density).toInt(), 0, (20 * density).toInt(), 0)
        if (chipText != null) text = chipText
        isClickable = true
        isFocusable = true

        applyState()
    }

    /**
     * Переключает состояние чипса.
     * @param selected true — активен, false — неактивен
     */
    fun setChipSelected(selected: Boolean) {
        isChipSelected = selected
        applyState()
    }

    /** Возвращает true, если чипс активен. */
    fun isChipSelected(): Boolean = isChipSelected

    private fun applyState() {
        if (isChipSelected) {
            setBackgroundResource(R.drawable.bg_chip_selected)
            setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            setBackgroundResource(R.drawable.bg_chip_default)
            setTextColor(ContextCompat.getColor(context, R.color.description))
        }
    }
}
