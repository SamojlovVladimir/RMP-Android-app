package ru.samoylov.matule.presentation.common

import android.content.Context
import android.content.Intent
import ru.samoylov.matule.presentation.auth.AuthActivity
import ru.samoylov.matule.presentation.auth.CreatePasswordActivity
import ru.samoylov.matule.presentation.cart.CartActivity
import ru.samoylov.matule.presentation.document.PdfActivity
import ru.samoylov.matule.presentation.main.MainActivity
import ru.samoylov.matule.presentation.profile.CreateProfileActivity

/**
 * Возврат на последний открытый экран после перезапуска приложения.
 *
 * Каждый экран при открытии сохраняет своё имя (BaseActivity.screenName).
 * При запуске здесь по этому имени собирается цепочка экранов:
 * например, для «Корзины» — сначала «Каталог», поверх него «Корзина»,
 * чтобы кнопка «назад» вела туда же, куда и до перезапуска.
 */
object LastScreenNavigator {

    const val SCREEN_AUTH = "auth"
    const val SCREEN_CREATE_PROFILE = "create_profile"
    const val SCREEN_CREATE_PASSWORD = "create_password"
    const val SCREEN_CART = "cart"
    const val SCREEN_PDF = "pdf"
    const val SCREEN_CREATE_PROJECT = "create_project"

    /** К этому префиксу добавляется номер вкладки: main_tab_0 … main_tab_3 */
    const val SCREEN_MAIN_TAB = "main_tab_"

    /**
     * Экраны для авторизованного пользователя (после входа по коду).
     * @param context контекст
     * @param lastScreen сохранённое имя последнего экрана
     */
    fun createMainIntents(context: Context, lastScreen: String?): Array<Intent> {
        val intents = when {
            lastScreen == SCREEN_CART -> arrayOf(
                MainActivity.createIntent(context, MainActivity.TAB_CATALOG),
                Intent(context, CartActivity::class.java)
            )
            lastScreen == SCREEN_PDF -> arrayOf(
                MainActivity.createIntent(context, MainActivity.TAB_PROFILE),
                PdfActivity.createPrivacyIntent(context)
            )
            lastScreen == SCREEN_CREATE_PROJECT -> arrayOf(
                MainActivity.createIntent(context, MainActivity.TAB_PROJECTS, openCreateProject = true)
            )
            lastScreen != null && lastScreen.startsWith(SCREEN_MAIN_TAB) -> arrayOf(
                MainActivity.createIntent(
                    context,
                    lastScreen.removePrefix(SCREEN_MAIN_TAB).toIntOrNull() ?: MainActivity.TAB_HOME
                )
            )
            else -> arrayOf(MainActivity.createIntent(context, MainActivity.TAB_HOME))
        }
        return clearTask(intents)
    }

    /**
     * Экраны для неавторизованного пользователя: незаконченная регистрация
     * продолжается с того же шага. Под экранами регистрации
     * лежит экран входа, чтобы «назад» возвращал на него, а не закрывал приложение.
     * @param context контекст
     * @param lastScreen сохранённое имя последнего экрана
     */
    fun createAuthIntents(context: Context, lastScreen: String?): Array<Intent> {
        val intents = when (lastScreen) {
            SCREEN_CREATE_PROFILE -> arrayOf(
                Intent(context, AuthActivity::class.java),
                Intent(context, CreateProfileActivity::class.java)
            )
            SCREEN_CREATE_PASSWORD -> arrayOf(
                Intent(context, AuthActivity::class.java),
                Intent(context, CreateProfileActivity::class.java),
                Intent(context, CreatePasswordActivity::class.java)
            )
            else -> arrayOf(Intent(context, AuthActivity::class.java))
        }
        return clearTask(intents)
    }

    // Первый экран цепочки начинает задачу заново, чтобы «назад» не вёл на заставку или код
    private fun clearTask(intents: Array<Intent>): Array<Intent> {
        intents[0].flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return intents
    }
}
