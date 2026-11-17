package ru.samoylov.matule.presentation.common

import android.app.Activity
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal

/**
 * Вход по отпечатку пальца или лицу через системное окно Android.
 * Работает на Android 10 и новее; на старых версиях кнопка просто не показывается.
 * Отдельная библиотека не нужна — используется встроенный BiometricPrompt.
 */
object BiometricAuth {

    /**
     * Можно ли сейчас войти по биометрии (есть датчик и сохранён отпечаток).
     * @param context контекст экрана
     */
    fun isAvailable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val manager = context.getSystemService(BiometricManager::class.java) ?: return false
        @Suppress("DEPRECATION")
        return manager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Показывает системное окно биометрии.
     * @param activity экран, поверх которого показывается окно
     * @param onSuccess вызывается после успешной проверки
     */
    fun authenticate(activity: Activity, onSuccess: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val executor = activity.mainExecutor
        val prompt = BiometricPrompt.Builder(activity)
            .setTitle("Вход в Matule")
            .setSubtitle("Приложите палец к датчику")
            .setNegativeButton("Ввести код", executor) { _, _ -> }
            .build()

        prompt.authenticate(
            CancellationSignal(),
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    onSuccess()
                }
            }
        )
    }
}
