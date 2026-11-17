package ru.samoylov.matule.presentation.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import ru.samoylov.matule.R
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.presentation.auth.CreatePasswordActivity
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppInput
import ru.samoylov.matule.uikit.AppSelect
import ru.samoylov.matule.uikit.AppSelectOption

/**
 * Экран «Создание профиля». Все поля обязательны:
 * пока хотя бы одно пустое, кнопка «Далее» неактивна.
 * Состояние формы сохраняется после перезапуска приложения.
 */
class CreateProfileActivity : BaseActivity() {

    override val screenName = "create_profile"

    private val viewModel: CreateProfileViewModel by viewModels()

    private lateinit var nameInput: AppInput
    private lateinit var patronymicInput: AppInput
    private lateinit var surnameInput: AppInput
    private lateinit var birthDateInput: AppInput
    private lateinit var genderSelect: AppSelect
    private lateinit var emailInput: AppInput
    private lateinit var nextButton: AppButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_profile)

        nameInput = findViewById(R.id.nameInput)
        patronymicInput = findViewById(R.id.patronymicInput)
        surnameInput = findViewById(R.id.surnameInput)
        birthDateInput = findViewById(R.id.birthDateInput)
        genderSelect = findViewById(R.id.genderSelect)
        emailInput = findViewById(R.id.emailInput)
        nextButton = findViewById(R.id.nextButton)

        genderSelect.setOptions(
            listOf(
                AppSelectOption("Мужской", "👨"),
                AppSelectOption("Женский", "👩")
            )
        )

        restoreDraft()

        listOf(nameInput, patronymicInput, surnameInput, birthDateInput, emailInput).forEach {
            it.setOnTextChangedListener { onFormChanged() }
        }
        genderSelect.setOnOptionSelectedListener { onFormChanged() }

        nextButton.setOnClickListener {
            val error = viewModel.validateEmail(emailInput.getText())
            if (error != null) {
                emailInput.setError(error)
            } else {
                viewModel.saveDraft(readForm())
                startActivity(Intent(this, CreatePasswordActivity::class.java))
            }
        }
    }

    private fun restoreDraft() {
        val draft = viewModel.restoreDraft() ?: return
        nameInput.setText(draft.name)
        patronymicInput.setText(draft.patronymic)
        surnameInput.setText(draft.surname)
        birthDateInput.setText(draft.birthDate)
        genderSelect.selectByTitle(draft.gender)
        emailInput.setText(draft.email)
        nextButton.isEnabled = viewModel.isComplete(draft)
    }

    /** Сохраняет черновик и включает кнопку, когда всё заполнено. */
    private fun onFormChanged() {
        val user = readForm()
        viewModel.saveDraft(user)
        nextButton.isEnabled = viewModel.isComplete(user)
    }

    private fun readForm(): User = User(
        id = "",
        email = emailInput.getText(),
        name = nameInput.getText(),
        surname = surnameInput.getText(),
        patronymic = patronymicInput.getText(),
        birthDate = birthDateInput.getText(),
        gender = genderSelect.getSelectedTitle()
    )
}
