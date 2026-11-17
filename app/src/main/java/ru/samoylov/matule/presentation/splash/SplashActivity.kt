package ru.samoylov.matule.presentation.splash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import ru.samoylov.matule.R
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.usecase.GetStartDestinationUseCase
import ru.samoylov.matule.presentation.common.LastScreenNavigator
import ru.samoylov.matule.presentation.pin.PinActivity
import ru.samoylov.matule.presentation.showcase.ShowcaseActivity

/**
 * Экран заставки: синий градиент и надпись «Matule».
 *
 * Решает, куда отправить пользователя дальше: если он авторизован —
 * на вход по коду (или создание кода), если нет — на «Вход и регистрацию».
 * Само решение принимает доменный сценарий, экран только выполняет переход.
 *
 * Долгое нажатие на «Matule» открывает витрину компонентов UIKit.
 */
class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val navigateTask = Runnable { navigate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<TextView>(R.id.logoText).setOnLongClickListener {
            handler.removeCallbacks(navigateTask)
            startActivity(Intent(this, ShowcaseActivity::class.java))
            finish()
            true
        }

        handler.postDelayed(navigateTask, SPLASH_DELAY_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateTask)
        super.onDestroy()
    }

    private fun navigate() {
        when (ServiceLocator.startDestinationUseCase()) {
            GetStartDestinationUseCase.Destination.PIN_LOGIN ->
                startActivity(PinActivity.createIntent(this, PinActivity.MODE_LOGIN))
            GetStartDestinationUseCase.Destination.PIN_CREATE ->
                startActivity(PinActivity.createIntent(this, PinActivity.MODE_CREATE))
            GetStartDestinationUseCase.Destination.AUTH ->
                // Регистрацию бросили на середине — продолжаем с того же шага
                startActivities(
                    LastScreenNavigator.createAuthIntents(this, ServiceLocator.storage.getLastScreen())
                )
        }
        finish()
    }

    private companion object {
        const val SPLASH_DELAY_MS = 1500L
    }
}
