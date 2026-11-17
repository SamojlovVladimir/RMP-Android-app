package ru.samoylov.matule.presentation.pin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import ru.samoylov.matule.R
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.presentation.common.BiometricAuth
import ru.samoylov.matule.presentation.common.LastScreenNavigator
import ru.samoylov.matule.uikit.AppIcons
import ru.samoylov.matule.uikit.AppPinPad

/**
 * Экран кода из четырёх цифр. Один экран на два сценария (DRY):
 * - MODE_LOGIN  — «Вход по паролю или биометрии» при повторном запуске
 * - MODE_CREATE — «Создайте пароль» для входа в приложение после регистрации
 *
 * Логики здесь почти нет, поэтому отдельная ViewModel не нужна:
 * экран вызывает доменный сценарий PinCodeUseCase напрямую.
 */
class PinActivity : BaseActivity() {

    // Экран кода не запоминается: иначе при каждом запуске он затирал бы
    // настоящий последний экран, на который нужно вернуться после входа
    override val screenName = ""

    private val pinCodeUseCase = ServiceLocator.pinCodeUseCase
    private lateinit var pinPad: AppPinPad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        pinPad = findViewById(R.id.pinPad)
        val title = findViewById<TextView>(R.id.titleText)
        val subtitle = findViewById<TextView>(R.id.subtitleText)

        if (intent.getIntExtra(EXTRA_MODE, MODE_LOGIN) == MODE_CREATE) {
            title.text = "Создайте пароль"
            subtitle.text = "Для защиты ваших персональных данных"
            subtitle.visibility = View.VISIBLE
            pinPad.setOnPinEnteredListener { pin -> onPinCreated(pin) }
        } else {
            title.text = "Вход"
            pinPad.setOnPinEnteredListener { pin -> onPinEntered(pin) }
            if (BiometricAuth.isAvailable(this)) {
                pinPad.setExtraKey(AppIcons.FINGERPRINT) { askBiometric() }
                // Окно биометрии показываем, когда экран уже на месте
                pinPad.post { askBiometric() }
            }
        }
    }

    private fun onPinCreated(pin: String) {
        val error = pinCodeUseCase.save(pin)
        if (error != null) {
            pinPad.showError()
            showMessage(error)
        } else {
            // Код создан сразу после регистрации или входа — открываем «Главную»
            startActivities(LastScreenNavigator.createMainIntents(this, null))
        }
    }

    private fun onPinEntered(pin: String) {
        if (pinCodeUseCase.check(pin)) {
            openMain()
        } else {
            pinPad.showError()
        }
    }

    private fun askBiometric() {
        BiometricAuth.authenticate(this) { openMain() }
    }

    /** После входа по коду возвращаемся на экран, открытый до перезапуска. */
    private fun openMain() {
        val lastScreen = ServiceLocator.storage.getLastScreen()
        startActivities(LastScreenNavigator.createMainIntents(this, lastScreen))
    }

    companion object {
        private const val EXTRA_MODE = "mode"

        /** Вход по коду */
        const val MODE_LOGIN = 0

        /** Создание кода */
        const val MODE_CREATE = 1

        /**
         * Создаёт Intent для открытия экрана.
         * @param context контекст
         * @param mode MODE_LOGIN или MODE_CREATE
         */
        fun createIntent(context: Context, mode: Int): Intent =
            Intent(context, PinActivity::class.java).putExtra(EXTRA_MODE, mode)
    }
}
