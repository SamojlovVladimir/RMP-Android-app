package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * Одна вкладка нижней навигации.
 * @param glyph иконка вкладки из AppIcons
 * @param label подпись под иконкой
 */
data class TabBarItem(
    val glyph: String,
    val label: String
)

/**
 * AppTabBar — нижняя навигация UIKit.
 * Сверху тонкая линия-разделитель; активная вкладка окрашивается
 * акцентным цветом, неактивные — серым.
 *
 * Использование:
 * tabBar.setItems(AppTabBar.defaultItems())
 * tabBar.setOnTabSelectedListener { index -> ... }
 * tabBar.setSelectedIndex(0)
 */
class AppTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var items: List<TabBarItem> = emptyList()
    private var selectedIndex = 0
    private var onTabSelected: ((Int) -> Unit)? = null
    private val iconViews = mutableListOf<AppIcon>()
    private val labelViews = mutableListOf<TextView>()
    private val tabsRow: LinearLayout

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.white))
        val density = resources.displayMetrics.density

        addView(View(context).apply {
            setBackgroundColor(ContextCompat.getColor(context, R.color.divider))
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, density.toInt().coerceAtLeast(1))
        })

        tabsRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setPadding(0, (8 * density).toInt(), 0, (8 * density).toInt())
        }
        addView(tabsRow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    /**
     * Задаёт набор вкладок и перестраивает панель.
     * @param newItems список вкладок
     */
    fun setItems(newItems: List<TabBarItem>) {
        items = newItems
        rebuild()
    }

    /**
     * Делает вкладку активной (без вызова обработчика).
     * @param index порядковый номер вкладки, начиная с нуля
     */
    fun setSelectedIndex(index: Int) {
        selectedIndex = index
        updateColors()
    }

    /** Возвращает номер активной вкладки. */
    fun getSelectedIndex(): Int = selectedIndex

    /**
     * Назначает обработчик переключения вкладок.
     * @param listener вызывается с номером выбранной вкладки
     */
    fun setOnTabSelectedListener(listener: (Int) -> Unit) {
        onTabSelected = listener
    }

    private fun rebuild() {
        tabsRow.removeAllViews()
        iconViews.clear()
        labelViews.clear()

        items.forEachIndexed { index, item ->
            tabsRow.addView(createTabView(item, index))
        }
        updateColors()
    }

    private fun createTabView(item: TabBarItem, index: Int): LinearLayout {
        val density = resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            isClickable = true
            isFocusable = true
        }

        val icon = AppIcon(context).apply {
            setIcon(item.glyph)
            setIconSize(26f)
        }

        val label = TextView(context).apply {
            text = item.label
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        container.addView(icon)
        container.addView(label)

        iconViews.add(icon)
        labelViews.add(label)

        container.setOnClickListener {
            selectedIndex = index
            updateColors()
            onTabSelected?.invoke(index)
        }

        return container
    }

    private fun updateColors() {
        val activeColor = ContextCompat.getColor(context, R.color.accent)
        val inactiveColor = ContextCompat.getColor(context, R.color.input_icon)

        iconViews.forEachIndexed { index, icon ->
            val color = if (index == selectedIndex) activeColor else inactiveColor
            icon.setIconColor(color)
            labelViews[index].setTextColor(color)
        }
    }

    companion object {
        /** Четыре вкладки приложения Matule в порядке макета. */
        fun defaultItems(): List<TabBarItem> = listOf(
            TabBarItem(AppIcons.TAB_HOME, "Главная"),
            TabBarItem(AppIcons.TAB_CATALOG, "Каталог"),
            TabBarItem(AppIcons.TAB_PROJECTS, "Проекты"),
            TabBarItem(AppIcons.TAB_PROFILE, "Профиль")
        )
    }
}
