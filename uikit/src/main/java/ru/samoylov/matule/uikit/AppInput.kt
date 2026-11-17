package ru.samoylov.matule.uikit

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import ru.samoylov.matule.uikit.databinding.ViewAppInputBinding

/**
 * AppInput — поле ввода UIKit.
 *
 * Состояния:
 * - обычное  — серая заливка и тонкая рамка
 * - в фокусе — синяя рамка (переключается автоматически селектором bg_input)
 * - ошибка   — розовая заливка, красная рамка и текст ошибки снизу (setError)
 * - disabled — через стандартный isEnabled
 * - пароль   — справа «глаз» для показа и скрытия символов
 * - дата     — вводятся только цифры, точки ставятся сами: ДД.ММ.ГГГГ
 *
 * Атрибуты XML:
 * app:inputLabel     — подпись над полем
 * app:inputHint      — текст-подсказка внутри поля
 * app:inputFieldType — text | password | email | number | date
 */
class AppInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAppInputBinding
    private var isPasswordVisible = false
    private var onTextChanged: ((String) -> Unit)? = null
    private var isFormatting = false
    private var fieldType = TYPE_TEXT

    init {
        orientation = VERTICAL
        binding = ViewAppInputBinding.inflate(LayoutInflater.from(context), this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AppInput)
        val label = typedArray.getString(R.styleable.AppInput_inputLabel)
        val hint = typedArray.getString(R.styleable.AppInput_inputHint)
        fieldType = typedArray.getInt(R.styleable.AppInput_inputFieldType, TYPE_TEXT)
        typedArray.recycle()

        if (!label.isNullOrEmpty()) setLabel(label)
        binding.inputField.hint = hint

        applyFieldType(fieldType)

        binding.inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return
                if (fieldType == TYPE_DATE) formatDate(s)
                // Ошибка гасится, как только пользователь начинает править текст
                if (binding.errorText.visibility == VISIBLE) setError(null)
                onTextChanged?.invoke(s.toString())
            }
        })
    }

    /**
     * @param type 0 — text, 1 — password, 2 — email, 3 — number, 4 — date
     */
    private fun applyFieldType(type: Int) {
        when (type) {
            TYPE_PASSWORD -> {
                binding.inputField.inputType = PASSWORD_HIDDEN
                binding.inputField.setPadding(
                    binding.inputField.paddingLeft, 0,
                    (44 * resources.displayMetrics.density).toInt(), 0
                )
                binding.togglePassword.visibility = VISIBLE
                binding.togglePassword.setOnClickListener { togglePasswordVisibility() }
            }
            TYPE_EMAIL -> binding.inputField.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            TYPE_NUMBER, TYPE_DATE -> binding.inputField.inputType = InputType.TYPE_CLASS_NUMBER
            else -> binding.inputField.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
    }

    /** Расставляет точки в дате: 28021991 → 28.02.1991. */
    private fun formatDate(s: Editable) {
        val digits = s.toString().filter { it.isDigit() }.take(8)
        val formatted = StringBuilder()
        digits.forEachIndexed { index, char ->
            if (index == 2 || index == 4) formatted.append('.')
            formatted.append(char)
        }
        if (formatted.toString() != s.toString()) {
            isFormatting = true
            s.replace(0, s.length, formatted.toString())
            isFormatting = false
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            binding.togglePassword.setIcon(AppIcons.EYE)
            binding.togglePassword.setIconColor(ContextCompat.getColor(context, R.color.black))
            binding.inputField.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.togglePassword.setIcon(AppIcons.EYE_OFF)
            binding.togglePassword.setIconColor(ContextCompat.getColor(context, R.color.description))
            binding.inputField.inputType = PASSWORD_HIDDEN
        }
        binding.inputField.setSelection(binding.inputField.text.length)
    }

    /**
     * Показывает или скрывает ошибку.
     * @param message текст ошибки; null или пустая строка убирает ошибку
     */
    fun setError(message: String?) {
        if (message.isNullOrEmpty()) {
            binding.errorText.visibility = GONE
            binding.inputBox.setBackgroundResource(R.drawable.bg_input)
        } else {
            binding.errorText.text = message
            binding.errorText.visibility = VISIBLE
            binding.inputBox.setBackgroundResource(R.drawable.bg_input_error)
        }
    }

    /** Возвращает введённый текст; у обычных полей пробелы по краям убираются, у пароля — нет. */
    fun getText(): String {
        val text = binding.inputField.text.toString()
        return if (fieldType == TYPE_PASSWORD) text else text.trim()
    }

    /**
     * Подставляет текст в поле.
     * @param text новый текст
     */
    fun setText(text: String) {
        binding.inputField.setText(text)
    }

    /**
     * Меняет подпись над полем.
     * @param text текст подписи
     */
    fun setLabel(text: String) {
        binding.labelText.text = text
        binding.labelText.visibility = VISIBLE
    }

    /**
     * Меняет подсказку внутри поля.
     * @param text текст подсказки
     */
    fun setHint(text: String) {
        binding.inputField.hint = text
    }

    /**
     * Назначает обработчик изменения текста (например, чтобы включать кнопку «Далее»).
     * @param listener вызывается с новым текстом на каждое изменение
     */
    fun setOnTextChangedListener(listener: (String) -> Unit) {
        onTextChanged = listener
    }

    /**
     * Назначает обработчик ухода фокуса из поля (удобно для проверки почты).
     * @param listener вызывается, когда пользователь перешёл к другому полю
     */
    fun setOnFocusLostListener(listener: () -> Unit) {
        binding.inputField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) listener()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.inputField.isEnabled = enabled
        binding.inputBox.isEnabled = enabled
        binding.inputBox.alpha = if (enabled) 1f else 0.6f
    }

    private companion object {
        const val TYPE_TEXT = 0
        const val TYPE_PASSWORD = 1
        const val TYPE_EMAIL = 2
        const val TYPE_NUMBER = 3
        const val TYPE_DATE = 4
        const val PASSWORD_HIDDEN =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
}
