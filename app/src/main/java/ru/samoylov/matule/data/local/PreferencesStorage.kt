package ru.samoylov.matule.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.domain.model.User
import ru.samoylov.matule.network.TokenStore

/**
 * Локальное хранилище на SharedPreferences.
 *
 * Отвечает за всё, что должно пережить перезапуск приложения:
 * токен и активная сессия, код быстрого входа, черновики профиля и проекта,
 * последний открытый экран, данные для напоминаний и кэш главной страницы.
 *
 * @param context контекст приложения
 */
class PreferencesStorage(context: Context) : TokenStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    // ===================== ТОКЕН И СЕССИЯ =====================

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER_ID).apply()
    }

    /**
     * Сохраняет идентификатор вошедшего пользователя.
     * @param userId идентификатор на сервере
     */
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    /** Возвращает идентификатор текущего пользователя или null. */
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    /** Есть ли активная сессия после перезапуска приложения. */
    fun hasSession(): Boolean = !getToken().isNullOrEmpty()

    // ===================== КОД БЫСТРОГО ВХОДА =====================

    /**
     * Сохраняет код для входа в приложение.
     * @param pin код из четырёх цифр
     */
    fun savePin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    /**
     * Сравнивает введённый код с сохранённым.
     * @param pin введённый код
     */
    fun checkPin(pin: String): Boolean = prefs.getString(KEY_PIN, null) == pin

    /** Установлен ли код быстрого входа. */
    fun hasPin(): Boolean = !prefs.getString(KEY_PIN, null).isNullOrEmpty()

    // ===================== ЧЕРНОВИК ПРОФИЛЯ =====================

    /**
     * Сохраняет незаконченное заполнение профиля.
     * @param user черновик профиля
     */
    fun saveProfileDraft(user: User) {
        prefs.edit().putString(KEY_PROFILE_DRAFT, gson.toJson(user)).apply()
    }

    /** Возвращает сохранённый черновик профиля или null. */
    fun getProfileDraft(): User? {
        val json = prefs.getString(KEY_PROFILE_DRAFT, null) ?: return null
        return try {
            gson.fromJson(json, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /** Удаляет черновик профиля. */
    fun clearProfileDraft() {
        prefs.edit().remove(KEY_PROFILE_DRAFT).apply()
    }

    // ===================== ЧЕРНОВИК ПРОЕКТА =====================

    /**
     * Сохраняет незаконченное заполнение экрана «Создание проекта».
     * @param project черновик проекта (photoUrl — путь к файлу фото)
     */
    fun saveProjectDraft(project: Project) {
        prefs.edit().putString(KEY_PROJECT_DRAFT, gson.toJson(project)).apply()
    }

    /** Возвращает черновик проекта или null. */
    fun getProjectDraft(): Project? {
        val json = prefs.getString(KEY_PROJECT_DRAFT, null) ?: return null
        return runCatching { gson.fromJson(json, Project::class.java) }.getOrNull()
    }

    /** Удаляет черновик проекта. */
    fun clearProjectDraft() {
        prefs.edit().remove(KEY_PROJECT_DRAFT).apply()
    }

    // ===================== НАПОМИНАНИЯ =====================

    /**
     * Запоминает последний открытый проект для напоминания о неактивности.
     * @param title название проекта
     */
    fun saveLastProject(title: String) {
        prefs.edit().putString(KEY_LAST_PROJECT, title).apply()
    }

    /** Название последнего открытого проекта или null. */
    fun getLastProject(): String? = prefs.getString(KEY_LAST_PROJECT, null)

    /** Забывает последний открытый проект (напоминание о нём уже пришло). */
    fun clearLastProject() {
        prefs.edit().remove(KEY_LAST_PROJECT).apply()
    }

    /**
     * Отмечает, приходило ли напоминание «давно не заходили» с последнего входа.
     * @param sent true — уже приходило
     */
    fun setAppReminderSent(sent: Boolean) {
        prefs.edit().putBoolean(KEY_APP_REMINDER_SENT, sent).apply()
    }

    /** Приходило ли напоминание с последнего входа в приложение. */
    fun isAppReminderSent(): Boolean = prefs.getBoolean(KEY_APP_REMINDER_SENT, false)

    // ===================== ПОСЛЕДНИЙ ЭКРАН =====================

    /**
     * Запоминает экран, на котором пользователь закрыл приложение.
     * @param screenName имя экрана
     */
    fun saveLastScreen(screenName: String) {
        prefs.edit().putString(KEY_LAST_SCREEN, screenName).apply()
    }

    /** Возвращает имя последнего открытого экрана или null. */
    fun getLastScreen(): String? = prefs.getString(KEY_LAST_SCREEN, null)

    // ===================== КЭШ =====================

    /**
     * Сохраняет произвольные данные в кэш.
     * @param key ключ кэша
     * @param value объект для сохранения
     */
    fun <T> saveCache(key: String, value: T) {
        prefs.edit().putString(CACHE_PREFIX + key, gson.toJson(value)).apply()
    }

    /**
     * Читает сохранённую в кэше строку.
     * @param key ключ кэша
     */
    fun getCacheJson(key: String): String? = prefs.getString(CACHE_PREFIX + key, null)

    /** Полностью очищает хранилище при выходе из аккаунта. */
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val FILE_NAME = "matule_prefs"
        const val KEY_TOKEN = "auth_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_PIN = "pin_code"
        const val KEY_PROFILE_DRAFT = "profile_draft"
        const val KEY_LAST_SCREEN = "last_screen"
        const val KEY_PROJECT_DRAFT = "project_draft"
        const val KEY_LAST_PROJECT = "last_project"
        const val KEY_APP_REMINDER_SENT = "app_reminder_sent"
        const val CACHE_PREFIX = "cache_"
    }
}
