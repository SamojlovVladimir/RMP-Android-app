package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import ru.samoylov.matule.uikit.databinding.ViewAppHeaderBinding

/**
 * AppHeader — верхняя панель экрана UIKit. Два вида, как в макете:
 * - обычный (app:headerLarge="false") — «назад», заголовок по центру, иконка справа
 * - крупный (app:headerLarge="true")  — «назад» и иконка в строке,
 *   под ними крупный заголовок слева (экран «Корзина»)
 *
 * Кнопка «назад» и иконка действия показываются только тогда, когда для них
 * назначен обработчик, поэтому один компонент подходит всем экранам.
 *
 * Атрибуты XML:
 * app:headerTitle — заголовок панели
 * app:headerLarge — крупный заголовок слева
 */
class AppHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAppHeaderBinding
    private var isLarge = false

    init {
        orientation = VERTICAL
        binding = ViewAppHeaderBinding.inflate(LayoutInflater.from(context), this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppHeader)
        val title = typedArray.getString(R.styleable.AppHeader_headerTitle)
        isLarge = typedArray.getBoolean(R.styleable.AppHeader_headerLarge, false)
        typedArray.recycle()

        binding.largeTitleText.visibility = if (isLarge) VISIBLE else GONE
        binding.titleText.visibility = if (isLarge) GONE else VISIBLE
        setTitle(title ?: "")
    }

    /**
     * Меняет заголовок панели.
     * @param text новый заголовок
     */
    fun setTitle(text: String) {
        binding.titleText.text = text
        binding.largeTitleText.text = text
    }

    /**
     * Показывает кнопку «назад» и назначает обработчик клика.
     * @param listener вызывается при нажатии на кнопку
     */
    fun setOnBackClickListener(listener: () -> Unit) {
        binding.backIcon.visibility = VISIBLE
        binding.backIcon.setOnClickListener { listener() }
    }

    /**
     * Показывает иконку действия справа и назначает обработчик клика.
     * @param glyph иконка из AppIcons, например AppIcons.TRASH
     * @param listener вызывается при нажатии на иконку
     */
    fun setActionIcon(glyph: String, listener: () -> Unit) {
        binding.actionIcon.setIcon(glyph)
        binding.actionIcon.visibility = VISIBLE
        binding.actionIcon.setOnClickListener { listener() }
    }
}
