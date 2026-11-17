package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat

/**
 * AppIcon — иконка из шрифта иконок UIKit (res/font/uikit_icons.ttf).
 *
 * Иконка — это один символ шрифта, поэтому её цвет задаётся как цвет текста
 * (android:textColor или setIconColor), а размер — атрибутом app:iconSize.
 * Все иконки макета собраны в одном шрифте, коды символов лежат в AppIcons.
 *
 * Атрибуты XML:
 * app:iconName — имя иконки: back, search, plus, close, trash, cart, eye и т.д.
 * app:iconSize — размер иконки (по умолчанию 24dp)
 */
class AppIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        typeface = ResourcesCompat.getFont(context, R.font.uikit_icons)
        includeFontPadding = false
        gravity = Gravity.CENTER

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppIcon)
        val name = typedArray.getString(R.styleable.AppIcon_iconName)
        val sizePx = typedArray.getDimension(
            R.styleable.AppIcon_iconSize,
            24 * resources.displayMetrics.density
        )
        typedArray.recycle()

        // Размер в пикселях: иконка не должна раздуваться от системного масштаба шрифта
        setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx)
        if (name != null) setIcon(AppIcons.byName[name] ?: "")
    }

    /**
     * Меняет иконку.
     * @param glyph код символа из AppIcons, например AppIcons.SEARCH
     */
    fun setIcon(glyph: String) {
        text = glyph
    }

    /**
     * Меняет цвет иконки.
     * @param color цвет в формате ARGB (например ContextCompat.getColor(...))
     */
    fun setIconColor(color: Int) {
        setTextColor(color)
    }

    /**
     * Меняет размер иконки.
     * @param sizeDp размер в dp
     */
    fun setIconSize(sizeDp: Float) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeDp * resources.displayMetrics.density)
    }
}
