package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.samoylov.matule.uikit.databinding.ViewAppSelectBinding

/**
 * Опция для AppSelect.
 * @param title подпись опции
 * @param emoji необязательный смайлик слева от подписи
 */
data class AppSelectOption(
    val title: String,
    val emoji: String = ""
)

/**
 * AppSelect — поле выбора значения из списка.
 *
 * Внешне повторяет AppInput и переиспользует его фон bg_input (соблюдение DRY).
 * По клику открывается AppBottomSheet со списком опций; поддерживаются смайлики.
 *
 * Атрибуты XML:
 * app:selectLabel       — подпись над полем
 * app:selectPlaceholder — текст, пока ничего не выбрано
 */
class AppSelect @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAppSelectBinding
    private var options: List<AppSelectOption> = emptyList()
    private var onOptionSelected: ((AppSelectOption) -> Unit)? = null
    private var selectedOption: AppSelectOption? = null
    private var placeholder = ""

    init {
        orientation = VERTICAL
        binding = ViewAppSelectBinding.inflate(LayoutInflater.from(context), this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppSelect)
        val label = typedArray.getString(R.styleable.AppSelect_selectLabel)
        placeholder = typedArray.getString(R.styleable.AppSelect_selectPlaceholder) ?: ""
        typedArray.recycle()

        if (!label.isNullOrEmpty()) {
            binding.labelText.text = label
            binding.labelText.visibility = VISIBLE
        }

        showOption(null)
        binding.selectBox.setOnClickListener { openOptionsSheet() }
    }

    /**
     * Задаёт список доступных опций.
     * @param newOptions список опций для выбора
     */
    fun setOptions(newOptions: List<AppSelectOption>) {
        options = newOptions
    }

    /**
     * Назначает обработчик выбора опции.
     * @param listener вызывается с выбранной опцией
     */
    fun setOnOptionSelectedListener(listener: (AppSelectOption) -> Unit) {
        onOptionSelected = listener
    }

    /** Возвращает выбранную опцию или null, если выбор не сделан. */
    fun getSelectedOption(): AppSelectOption? = selectedOption

    /** Возвращает подпись выбранной опции или пустую строку. */
    fun getSelectedTitle(): String = selectedOption?.title.orEmpty()

    /**
     * Выбирает опцию по подписи (например, при восстановлении черновика).
     * @param title подпись опции; пустая строка сбрасывает выбор
     */
    fun selectByTitle(title: String) {
        showOption(options.firstOrNull { it.title == title })
    }

    /** Показывает выбранную опцию или текст-заглушку. */
    private fun showOption(option: AppSelectOption?) {
        selectedOption = option
        if (option == null) {
            binding.valueText.text = placeholder
            binding.valueText.setTextColor(ContextCompat.getColor(context, R.color.placeholder))
            binding.emojiText.visibility = GONE
        } else {
            binding.valueText.text = option.title
            binding.valueText.setTextColor(ContextCompat.getColor(context, R.color.black))
            binding.emojiText.text = option.emoji
            binding.emojiText.visibility = if (option.emoji.isEmpty()) GONE else VISIBLE
        }
    }

    private fun openOptionsSheet() {
        if (options.isEmpty()) return

        val sheet = AppBottomSheet(context)
        val content = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        options.forEach { content.addView(createOptionRow(it, sheet)) }

        sheet.setContent(content)
        sheet.show()
    }

    /** Создаёт одну строку списка опций: смайлик и подпись. */
    private fun createOptionRow(option: AppSelectOption, sheet: AppBottomSheet): LinearLayout {
        val density = resources.displayMetrics.density

        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, (14 * density).toInt(), 0, (14 * density).toInt())
            isClickable = true
            isFocusable = true
        }

        if (option.emoji.isNotEmpty()) {
            row.addView(TextView(context).apply {
                text = option.emoji
                textSize = 20f
                setPadding(0, 0, (12 * density).toInt(), 0)
            })
        }

        row.addView(TextView(context).apply {
            text = option.title
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.black))
            layoutParams = LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })

        if (option == selectedOption) {
            row.addView(AppIcon(context).apply {
                setIcon(AppIcons.CHECK)
                setIconSize(20f)
                setIconColor(ContextCompat.getColor(context, R.color.accent))
            })
        }

        row.setOnClickListener {
            showOption(option)
            onOptionSelected?.invoke(option)
            sheet.dismiss()
        }

        return row
    }
}
