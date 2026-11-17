package ru.samoylov.matule.presentation.common

import androidx.fragment.app.Fragment

/**
 * Показывает уведомление AppSnackBar из фрагмента через общий BaseActivity,
 * чтобы каждый фрагмент не создавал своё уведомление (DRY).
 * @param message текст уведомления
 * @param onClose вызывается после закрытия крестиком
 */
fun Fragment.showMessage(message: String, onClose: (() -> Unit)? = null) {
    (activity as? BaseActivity)?.showMessage(message, onClose)
}

/**
 * Переводит dp в пиксели для отступов, заданных из кода.
 * @param value значение в dp
 */
fun Fragment.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
