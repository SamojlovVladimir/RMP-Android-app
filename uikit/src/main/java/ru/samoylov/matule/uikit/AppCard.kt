package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import ru.samoylov.matule.uikit.databinding.ViewAppCardBinding

/**
 * AppCard — универсальная карточка UIKit.
 *
 * Один компонент покрывает все виды карточек макета, потому что зона
 * действия справа внизу принимает произвольный View (DRY):
 * - Primary (товар): заголовок, подзаголовок, цена и кнопка «Добавить»/«Убрать»
 * - Cart (корзина):  заголовок, крестик, цена и счётчик AppCounter
 * - Project:         заголовок, серая подпись «Прошло 2 дня» и кнопка «Открыть»
 *
 * Использование:
 * card.setTitle("Рубашка Воскресенье")
 * card.setSubtitle("Мужская одежда")
 * card.setPrice("300 ₽")
 * card.setActionView(AppButton.small(context, "Добавить"))
 * card.setOnCloseListener { ... }
 */
class AppCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAppCardBinding

    init {
        orientation = VERTICAL
        val density = resources.displayMetrics.density
        val padding = (16 * density).toInt()
        setPadding(padding, padding, padding, padding)
        setBackgroundResource(R.drawable.bg_card)
        elevation = 2 * density

        binding = ViewAppCardBinding.inflate(LayoutInflater.from(context), this)
    }

    /**
     * Задаёт заголовок карточки.
     * @param text текст заголовка (до двух строк)
     */
    fun setTitle(text: String) {
        binding.titleText.text = text
    }

    /**
     * Задаёт серый подзаголовок под названием («Мужская одежда»).
     * @param text текст подзаголовка; null скрывает строку
     */
    fun setSubtitle(text: String?) {
        if (text.isNullOrEmpty()) {
            binding.subtitleText.visibility = GONE
        } else {
            binding.subtitleText.text = text
            binding.subtitleText.visibility = VISIBLE
        }
    }

    /**
     * Задаёт цену в левом нижнем углу (чёрный жирный текст).
     * @param text текст цены
     */
    fun setPrice(text: String) {
        binding.priceText.text = text
        binding.priceText.setTextAppearance(R.style.Text_Title3_Semibold)
    }

    /**
     * Задаёт серую подпись в левом нижнем углу вместо цены («Прошло 2 дня»).
     * @param text текст подписи
     */
    fun setCaption(text: String) {
        binding.priceText.text = text
        binding.priceText.setTextAppearance(R.style.Text_Caption_Semibold)
        binding.priceText.setTextColor(ContextCompat.getColor(context, R.color.placeholder))
        // У карточки проекта между заголовком и нижней строкой большой отступ
        val params = binding.bottomRow.layoutParams as LayoutParams
        params.topMargin = (32 * resources.displayMetrics.density).toInt()
        binding.bottomRow.layoutParams = params
    }

    /**
     * Подставляет произвольный View в зону действия карточки.
     * @param view кнопка, счётчик или любой другой элемент управления
     */
    fun setActionView(view: View) {
        binding.actionContainer.removeAllViews()
        binding.actionContainer.addView(view)
    }

    /**
     * Показывает крестик в углу карточки и назначает обработчик клика.
     * @param listener вызывается при нажатии на крестик
     */
    fun setOnCloseListener(listener: () -> Unit) {
        binding.closeIcon.visibility = VISIBLE
        binding.closeIcon.setOnClickListener { listener() }
    }
}
