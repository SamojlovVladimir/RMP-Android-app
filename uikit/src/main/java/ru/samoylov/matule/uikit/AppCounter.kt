package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import ru.samoylov.matule.uikit.databinding.ViewAppCounterBinding

/**
 * AppCounter — счётчик количества: подпись «1 штук» и кнопки «−» и «+».
 * Используется в карточке корзины. Когда значение минимальное,
 * «−» становится бледным и не уменьшает счётчик (удаление — крестиком карточки).
 *
 * Использование:
 * counter.setCount(2)
 * counter.setOnCountChangedListener { newCount -> ... }
 */
class AppCounter @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAppCounterBinding
    private var count = 1
    private var minCount = 1
    private var onCountChanged: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        binding = ViewAppCounterBinding.inflate(LayoutInflater.from(context), this)

        binding.decreaseButton.setOnClickListener {
            if (count > minCount) {
                count--
                updateText()
                onCountChanged?.invoke(count)
            }
        }
        binding.increaseButton.setOnClickListener {
            count++
            updateText()
            onCountChanged?.invoke(count)
        }

        updateText()
    }

    /**
     * Устанавливает текущее значение счётчика.
     * @param value новое значение
     */
    fun setCount(value: Int) {
        count = value
        updateText()
    }

    /** Возвращает текущее значение счётчика. */
    fun getCount(): Int = count

    /**
     * Задаёт минимально допустимое значение.
     * @param value минимум, ниже которого счётчик не опускается (по умолчанию 1)
     */
    fun setMinCount(value: Int) {
        minCount = value
    }

    /**
     * Назначает обработчик изменения значения.
     * @param listener вызывается с новым значением счётчика
     */
    fun setOnCountChangedListener(listener: (Int) -> Unit) {
        onCountChanged = listener
    }

    private fun updateText() {
        binding.countText.text = "$count ${context.getString(R.string.uikit_count_unit)}"
        val minusColor = if (count > minCount) R.color.description else R.color.input_icon
        binding.decreaseButton.setIconColor(ContextCompat.getColor(context, minusColor))
    }
}
