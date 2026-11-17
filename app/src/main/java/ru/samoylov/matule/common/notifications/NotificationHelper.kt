package ru.samoylov.matule.common.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ru.samoylov.matule.R

/**
 * Показ локальных уведомлений приложения.
 * Нажатие на уведомление открывает приложение с заставки.
 */
object NotificationHelper {

    private const val CHANNEL_ID = "reminders"

    /** Уведомление «Давно не заходили в приложение» */
    const val ID_APP_INACTIVE = 1

    /** Уведомление «Давно не открывали проект» */
    const val ID_PROJECT_INACTIVE = 2

    /**
     * Создаёт канал уведомлений (нужен на Android 8+). Вызывается при старте приложения.
     * @param context контекст приложения
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Напоминания о неактивности в приложении и проектах"
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * Показывает уведомление. Если пользователь запретил уведомления — ничего не делает.
     * @param context контекст
     * @param id номер уведомления (ID_APP_INACTIVE или ID_PROJECT_INACTIVE)
     * @param title заголовок
     * @param text текст
     */
    fun show(context: Context, id: Int, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val openApp = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return
        openApp.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context, id, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
