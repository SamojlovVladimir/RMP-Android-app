package ru.samoylov.matule.presentation.common

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.samoylov.matule.common.notifications.ReminderScheduler
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.uikit.AppSnackBar

/**
 * Общий предок экранов приложения. Убирает из экранов повторяющийся код (DRY):
 * - отступы от статус-бара, навигации и клавиатуры (Android 15+ рисует
 *   приложение под системными панелями);
 * - показ уведомления AppSnackBar с крестиком (ошибки сервера и сети);
 * - запоминание последнего открытого экрана;
 * - учёт касаний экрана для напоминания о неактивности в проекте.
 */
abstract class BaseActivity : AppCompatActivity() {

    /**
     * Имя экрана, под которым он сохраняется как последний открытый
     * (см. LastScreenNavigator). Пустая строка — экран не запоминается.
     */
    abstract val screenName: String

    private var snackBar: AppSnackBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Прозрачные системные панели с тёмными значками на белом фоне
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        val content = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()
        if (screenName.isNotEmpty()) ServiceLocator.storage.saveLastScreen(screenName)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) ReminderScheduler.onUserActivity(this)
        return super.dispatchTouchEvent(event)
    }

    /**
     * Показывает уведомление сверху экрана. Закрывается только крестиком.
     * @param message текст уведомления (например, текст ошибки сервера)
     * @param onClose вызывается после закрытия
     */
    fun showMessage(message: String, onClose: (() -> Unit)? = null) {
        val bar = snackBar ?: createSnackBar()
        bar.show(message, onClose)
    }

    /** Скрывает уведомление, если оно показано. */
    fun hideMessage() {
        snackBar?.hide()
    }

    private fun createSnackBar(): AppSnackBar {
        val bar = AppSnackBar(this)
        val margin = (16 * resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        )
        params.setMargins(margin, margin, margin, 0)
        findViewById<FrameLayout>(android.R.id.content).addView(bar, params)
        snackBar = bar
        return bar
    }
}
