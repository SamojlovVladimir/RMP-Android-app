package ru.samoylov.matule.domain.usecase

/**
 * Проверки вводимых данных.
 *
 * Правила проверки живут в доменном слое, а не в экранах: одно и то же
 * правило используется на нескольких экранах и не дублируется (DRY).
 */

/**
 * Проверка почты на соответствие шаблону из конкурсного задания:
 * имя и доменное имя состоят только из строчных букв и цифр.
 */
object EmailValidator {

    private val PATTERN = Regex("^[a-z0-9]+@[a-z0-9]+\\.[a-z]{2,}$")

    /**
     * @param email проверяемая почта
     * @return текст ошибки или null, если почта корректна
     */
    fun validate(email: String): String? = when {
        email.isBlank() -> "Введите почту"
        !PATTERN.matches(email) -> "Почта должна быть вида name@domenname.ru"
        else -> null
    }
}

/**
 * Проверка надёжности пароля по требованиям конкурсного задания:
 * не менее восьми символов, заглавные и строчные буквы, цифры,
 * пробелы и специальные символы.
 */
object PasswordValidator {

    private const val MIN_LENGTH = 8
    private const val SPECIAL_CHARS = "!@#$%^&*()_+-=[]{};':\",.<>/?\\|`~"

    /**
     * @param password проверяемый пароль
     * @return текст ошибки или null, если пароль надёжный
     */
    fun validate(password: String): String? = when {
        password.length < MIN_LENGTH -> "Пароль должен содержать не менее $MIN_LENGTH символов"
        password.none { it.isUpperCase() } -> "Добавьте заглавную букву"
        password.none { it.isLowerCase() } -> "Добавьте строчную букву"
        password.none { it.isDigit() } -> "Добавьте цифру"
        password.none { it in SPECIAL_CHARS } -> "Добавьте специальный символ"
        else -> null
    }

    /**
     * Сравнивает пароль и его повтор.
     * @param password пароль
     * @param confirmation повтор пароля
     * @return текст ошибки или null, если пароли совпадают
     */
    fun validateConfirmation(password: String, confirmation: String): String? =
        if (password != confirmation) "Пароли не совпадают" else null
}

/** Проверка обязательных полей формы. */
object RequiredFieldValidator {

    /**
     * @param value значение поля
     * @param fieldName название поля для текста ошибки
     * @return текст ошибки или null, если поле заполнено
     */
    fun validate(value: String, fieldName: String): String? =
        if (value.isBlank()) "Заполните поле «$fieldName»" else null
}
