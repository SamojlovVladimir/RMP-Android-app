package ru.samoylov.matule.common.notifications

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle

/**
 * Определяет, когда приложение открыли и когда свернули.
 * Считает видимые экраны: стало 0 — приложение ушло в фон, стало 1 — вернулось.
 *
 * @param context контекст приложения
 */
class AppLifecycleTracker(private val context: Context) : Application.ActivityLifecycleCallbacks {

    private var startedCount = 0

    override fun onActivityStarted(activity: Activity) {
        if (startedCount == 0) ReminderScheduler.onAppOpened(context)
        startedCount++
    }

    override fun onActivityStopped(activity: Activity) {
        startedCount--
        // При пересоздании экрана (поворот) приложение не уходит в фон
        if (startedCount == 0 && !activity.isChangingConfigurations) {
            ReminderScheduler.onAppClosed(context)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
