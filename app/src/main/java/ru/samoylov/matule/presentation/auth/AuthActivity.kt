package ru.samoylov.matule.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import ru.samoylov.matule.R
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.presentation.common.UiState
import ru.samoylov.matule.presentation.main.MainActivity
import ru.samoylov.matule.presentation.pin.PinActivity
import ru.samoylov.matule.presentation.profile.CreateProfileActivity
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppInput

/**
 * Экран «Вход и регистрация».
 *
 * Три состояния макета: поля пустые (кнопка «Далее» неактивна), поля
 * заполнены (кнопка активна), пароль показан (нажатием на «глаз»).
 * Почта проверяется при уходе из поля и перед входом.
 */
class AuthActivity : BaseActivity() {

    override val screenName = "auth"

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var emailInput: AppInput
    private lateinit var passwordInput: AppInput
    private lateinit var continueButton: AppButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        continueButton = findViewById(R.id.continueButton)

        emailInput.setOnTextChangedListener { updateButton() }
        passwordInput.setOnTextChangedListener { updateButton() }
        emailInput.setOnFocusLostListener { viewModel.validateEmail(emailInput.getText()) }

        continueButton.setOnClickListener {
            viewModel.login(emailInput.getText(), passwordInput.getText())
        }

        findViewById<TextView>(R.id.registerLink).setOnClickListener {
            startActivity(Intent(this, CreateProfileActivity::class.java))
        }

        val socialListener = View.OnClickListener {
            showMessage("Вход через соцсети пока недоступен. Войдите по почте")
        }
        findViewById<View>(R.id.vkButton).setOnClickListener(socialListener)
        findViewById<View>(R.id.yandexButton).setOnClickListener(socialListener)

        observeState()
    }

    /** Кнопка «Далее» активна, только когда оба поля заполнены. */
    private fun updateButton() {
        continueButton.isEnabled =
            emailInput.getText().isNotEmpty() && passwordInput.getText().isNotEmpty()
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            when (state) {
                is UiState.Loading -> continueButton.isEnabled = false
                is UiState.Success -> {
                    val intent = if (state.data == AuthViewModel.Next.MAIN) {
                        Intent(this, MainActivity::class.java)
                    } else {
                        PinActivity.createIntent(this, PinActivity.MODE_CREATE)
                    }
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is UiState.Error -> {
                    updateButton()
                    showMessage(state.message) { viewModel.consumeState() }
                }
                is UiState.Idle -> updateButton()
            }
        }

        viewModel.emailError.observe(this) { error ->
            emailInput.setError(error)
        }
    }
}
