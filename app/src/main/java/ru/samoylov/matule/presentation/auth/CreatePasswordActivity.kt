package ru.samoylov.matule.presentation.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import ru.samoylov.matule.R
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.presentation.common.UiState
import ru.samoylov.matule.presentation.pin.PinActivity
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppInput

/**
 * Экран «Создание пароля» профиля: новый пароль и его повтор,
 * у обоих полей можно показать символы. «Сохранить» активна,
 * когда оба поля заполнены. После успешной регистрации —
 * экран создания кода для входа в приложение.
 */
class CreatePasswordActivity : BaseActivity() {

    override val screenName = "create_password"

    private val viewModel: CreatePasswordViewModel by viewModels()

    private lateinit var passwordInput: AppInput
    private lateinit var confirmInput: AppInput
    private lateinit var saveButton: AppButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_password)

        passwordInput = findViewById(R.id.passwordInput)
        confirmInput = findViewById(R.id.confirmInput)
        saveButton = findViewById(R.id.saveButton)

        passwordInput.setOnTextChangedListener { updateButton() }
        confirmInput.setOnTextChangedListener { updateButton() }

        saveButton.setOnClickListener {
            viewModel.save(passwordInput.getText(), confirmInput.getText())
        }

        viewModel.passwordError.observe(this) { passwordInput.setError(it) }
        viewModel.confirmError.observe(this) { confirmInput.setError(it) }
        viewModel.state.observe(this) { state ->
            when (state) {
                is UiState.Loading -> saveButton.isEnabled = false
                is UiState.Success -> {
                    val intent = PinActivity.createIntent(this, PinActivity.MODE_CREATE)
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
    }

    private fun updateButton() {
        saveButton.isEnabled =
            passwordInput.getText().isNotEmpty() && confirmInput.getText().isNotEmpty()
    }
}
