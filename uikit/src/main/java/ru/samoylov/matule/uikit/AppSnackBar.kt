package ru.samoylov.matule.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import ru.samoylov.matule.uikit.databinding.ViewAppSnackbarBinding

/**
 * AppSnackBar — уведомление UIKit: белая карточка с текстом
 * и круглым крестиком принудительного закрытия в правом верхнем углу.
 * Используется для ошибок сервера и отсутствия сети, а также для сообщения
 * об успешном заказе.
 *
 * Использование:
 * snackBar.show("Произошла ошибка") { /* действие после закрытия */ }
 */
class AppSnackBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewAppSnackbarBinding

    init {
        setBackgroundResource(R.drawable.bg_snackbar)
        elevation = 8 * resources.displayMetrics.density
        binding = ViewAppSnackbarBinding.inflate(LayoutInflater.from(context), this)
        visibility = GONE
    }

    /**
     * Показывает уведомление.
     * @param message текст уведомления
     * @param onClose вызывается после нажатия на крестик
     */
    fun show(message: String, onClose: (() -> Unit)? = null) {
        binding.messageText.text = message
        visibility = VISIBLE
        bringToFront()
        binding.closeIcon.setOnClickListener {
            visibility = GONE
            onClose?.invoke()
        }
    }

    /** Скрывает уведомление. */
    fun hide() {
        visibility = GONE
    }
}
