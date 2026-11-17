package ru.samoylov.matule.common.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ru.samoylov.matule.di.ServiceLocator

/**
 * Получает сигнал от будильника и показывает нужное уведомление.
 * Уведомления приходят только авторизованному пользователю.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val storage = ServiceLocator.storage
        if (!storage.hasSession()) return

        when (intent.action) {
            ACTION_APP -> {
                if (storage.isAppReminderSent()) return
                NotificationHelper.show(
                    context, NotificationHelper.ID_APP_INACTIVE,
                    "Мы скучаем!",
                    "Вы давно не заходили в Matule. Загляните — вас ждут новые товары и акции"
                )
                // Повторно — только после следующего входа в приложение
                storage.setAppReminderSent(true)
            }
            ACTION_PROJECT -> {
                val title = storage.getLastProject() ?: return
                NotificationHelper.show(
                    context, NotificationHelper.ID_PROJECT_INACTIVE,
                    "Проект ждёт вас",
                    "Вы давно ничего не делали в проекте «$title». Продолжите работу над ним"
                )
                // Напоминание об этом проекте больше не повторяется, пока проект не откроют снова
                storage.clearLastProject()
            }
        }
    }

    companion object {
        const val ACTION_APP = "ru.samoylov.matule.REMINDER_APP"
        const val ACTION_PROJECT = "ru.samoylov.matule.REMINDER_PROJECT"
    }
}
