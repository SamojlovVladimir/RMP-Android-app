package ru.samoylov.matule.common.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.Project

/**
 * Планирование напоминаний через AlarmManager.
 *
 * Будильник срабатывает, даже если приложение закрыто, и запускает ReminderReceiver.
 * Используется точный будильник: разрешение USE_EXACT_ALARM выдаётся при установке,
 * пользователю ничего подтверждать не нужно. Если точный будильник всё же недоступен,
 * ставится обычный (может прийти с небольшой задержкой).
 */
object ReminderScheduler {

    /** Через сколько напомнить, что приложение давно не открывали */
    const val APP_INACTIVE_DELAY_MS = 60_000L

    /** Через сколько напомнить о неактивности в последнем открытом проекте */
    const val PROJECT_INACTIVE_DELAY_MS = 60_000L

    /** Касания чаще этого интервала не переставляют будильник (экономия ресурсов) */
    private const val TOUCH_THROTTLE_MS = 5_000L

    private var lastTouchReschedule = 0L

    /**
     * Пользователь открыл проект: запоминаем его и запускаем отсчёт неактивности.
     * @param context контекст
     * @param project открытый проект
     */
    fun onProjectOpened(context: Context, project: Project) {
        ServiceLocator.storage.saveLastProject(project.title)
        schedule(context, ReminderReceiver.ACTION_PROJECT, PROJECT_INACTIVE_DELAY_MS)
    }

    /**
     * Пользователь коснулся экрана: отсчёт неактивности в проекте начинается заново.
     * @param context контекст
     */
    fun onUserActivity(context: Context) {
        if (ServiceLocator.storage.getLastProject() == null) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastTouchReschedule < TOUCH_THROTTLE_MS) return
        lastTouchReschedule = now
        schedule(context, ReminderReceiver.ACTION_PROJECT, PROJECT_INACTIVE_DELAY_MS)
    }

    /** Приложение открыто: убираем напоминание и снова разрешаем его один раз. */
    fun onAppOpened(context: Context) {
        cancel(context, ReminderReceiver.ACTION_APP)
        ServiceLocator.storage.setAppReminderSent(false)
    }

    /** Приложение свёрнуто или закрыто: ставим напоминание, если оно ещё не приходило. */
    fun onAppClosed(context: Context) {
        val storage = ServiceLocator.storage
        if (!storage.hasSession() || storage.isAppReminderSent()) return
        schedule(context, ReminderReceiver.ACTION_APP, APP_INACTIVE_DELAY_MS)
    }

    private fun schedule(context: Context, action: String, delayMs: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = SystemClock.elapsedRealtime() + delayMs
        val pendingIntent = createPendingIntent(context, action)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        try {
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent
            )
        }
    }

    private fun cancel(context: Context, action: String) {
        context.getSystemService(AlarmManager::class.java)
            .cancel(createPendingIntent(context, action))
    }

    // Один и тот же action даёт один и тот же PendingIntent, поэтому новый будильник
    // заменяет старый, а cancel находит именно его
    private fun createPendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
