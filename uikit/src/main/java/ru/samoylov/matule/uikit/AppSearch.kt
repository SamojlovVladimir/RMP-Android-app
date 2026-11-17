package ru.samoylov.matule.uikit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import ru.samoylov.matule.uikit.databinding.ViewAppSearchBinding

/**
 * AppSearch — поле поиска UIKit.
 *
 * Состояния:
 * - пустое      — слева лупа и подсказка «Искать описания»
 * - заполненное — справа появляется крестик очистки
 * Фон общий с AppInput (bg_input), поэтому в фокусе рамка тоже синяя.
 *
 * Использование:
 * search.setOnQueryChangedListener { query -> ... }
 */
class AppSearch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAppSearchBinding
    private var onQueryChanged: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.bg_input)

        val density = resources.displayMetrics.density
        setPadding((14 * density).toInt(), 0, (14 * density).toInt(), 0)
        minimumHeight = (48 * density).toInt()

        binding = ViewAppSearchBinding.inflate(LayoutInflater.from(context), this)
        binding.searchField.hint = context.getString(R.string.uikit_search_hint)

        // Фокус EditText передаётся всему полю, поэтому синяя рамка рисуется по краю
        setAddStatesFromChildren(true)

        binding.searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString().orEmpty()
                binding.clearIcon.visibility = if (query.isEmpty()) GONE else VISIBLE
                onQueryChanged?.invoke(query.trim())
            }
        })

        binding.clearIcon.setOnClickListener { binding.searchField.setText("") }
    }

    /**
     * Назначает обработчик изменения поискового запроса.
     * @param listener вызывается на каждое изменение текста
     */
    fun setOnQueryChangedListener(listener: (String) -> Unit) {
        onQueryChanged = listener
    }

    /** Возвращает текущий поисковый запрос. */
    fun getQuery(): String = binding.searchField.text.toString().trim()

    /**
     * Меняет текст-подсказку.
     * @param hint новая подсказка
     */
    fun setHint(hint: String) {
        binding.searchField.hint = hint
    }
}
