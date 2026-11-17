package ru.samoylov.matule.uikit

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton

/**
 * AppButton — кнопка UIKit.
 *
 * Варианты оформления (app:buttonVariant):
 * - primary — синяя заливка, белый текст (основное действие)
 * - outline — белый фон, синяя рамка и текст («Убрать»)
 * - neutral — серая заливка, чёрный текст
 *
 * Размеры (app:buttonSize):
 * - big   — во всю ширину, высота 56dp («Далее», «Подтвердить»)
 * - small — компактная, высота 40dp («Добавить», «Открыть» в карточках)
 *
 * Неактивное состояние задаётся стандартным android:enabled="false":
 * у primary заливка становится бледно-синей.
 */
class AppButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppButton)
        val variant = typedArray.getInt(R.styleable.AppButton_buttonVariant, 0)
        val size = typedArray.getInt(R.styleable.AppButton_buttonSize, 0)
        typedArray.recycle()

        setVariant(variant)
        applySize(size)

        isAllCaps = false
        gravity = Gravity.CENTER
        typeface = Typeface.DEFAULT_BOLD
        stateListAnimator = null // без тени и «подпрыгивания» Material
    }

    /**
     * Меняет вариант оформления, например «Добавить» → «Убрать» в карточке.
     * @param variant 0 — primary, 1 — outline, 2 — neutral
     */
    fun setVariant(variant: Int) {
        when (variant) {
            VARIANT_OUTLINE -> {
                setBackgroundResource(R.drawable.bg_button_outline)
                setTextColor(context.getColorStateList(R.color.button_text_outline))
            }
            VARIANT_NEUTRAL -> {
                setBackgroundResource(R.drawable.bg_button_neutral)
                setTextColor(context.getColorStateList(R.color.button_text_neutral))
            }
            else -> {
                setBackgroundResource(R.drawable.bg_button_primary)
                setTextColor(context.getColorStateList(R.color.button_text_primary))
            }
        }
    }

    /**
     * Задаёт высоту, размер шрифта и отступы.
     * @param size 0 — big, 1 — small
     */
    private fun applySize(size: Int) {
        val density = resources.displayMetrics.density
        if (size == SIZE_SMALL) {
            minHeight = (40 * density).toInt()
            minimumHeight = (40 * density).toInt()
            minWidth = (96 * density).toInt()
            minimumWidth = (96 * density).toInt()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setPadding((16 * density).toInt(), 0, (16 * density).toInt(), 0)
        } else {
            minHeight = (56 * density).toInt()
            minimumHeight = (56 * density).toInt()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setPadding((16 * density).toInt(), 0, (16 * density).toInt(), 0)
        }
    }

    companion object {
        /** Синяя кнопка */
        const val VARIANT_PRIMARY = 0

        /** Белая кнопка с синей рамкой */
        const val VARIANT_OUTLINE = 1

        /** Серая кнопка */
        const val VARIANT_NEUTRAL = 2

        /** Большая кнопка */
        const val SIZE_BIG = 0

        /** Маленькая кнопка */
        const val SIZE_SMALL = 1

        /**
         * Создаёт маленькую кнопку из кода (для карточек).
         * @param context контекст
         * @param text надпись
         * @param variant вариант оформления
         */
        fun small(context: Context, text: String, variant: Int = VARIANT_PRIMARY): AppButton =
            AppButton(context).apply {
                applySize(SIZE_SMALL)
                setVariant(variant)
                this.text = text
            }
    }
}
