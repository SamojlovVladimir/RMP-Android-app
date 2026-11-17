package ru.samoylov.matule

import android.app.Application
import ru.samoylov.matule.common.notifications.AppLifecycleTracker
import ru.samoylov.matule.common.notifications.NotificationHelper
import ru.samoylov.matule.di.ServiceLocator

/**
 * Класс приложения.
 * Собирает зависимости, создаёт канал уведомлений и включает
 * отслеживание сворачивания приложения для напоминаний.
 */
class MatuleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        NotificationHelper.createChannel(this)
        registerActivityLifecycleCallbacks(AppLifecycleTracker(this))
    }
}
