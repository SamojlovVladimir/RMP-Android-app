package ru.samoylov.matule.presentation.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.samoylov.matule.R
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.presentation.catalog.CatalogFragment
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.presentation.common.LastScreenNavigator
import ru.samoylov.matule.presentation.home.HomeFragment
import ru.samoylov.matule.presentation.profile.ProfileFragment
import ru.samoylov.matule.presentation.projects.CreateProjectFragment
import ru.samoylov.matule.presentation.projects.ProjectsFragment
import ru.samoylov.matule.uikit.AppTabBar

/**
 * Основной экран приложения с нижней навигацией AppTabBar.
 * Каждая вкладка — отдельный фрагмент со своей логикой, поэтому изменение
 * одного экрана не затрагивает остальные. Экран «Создание проекта» открывается
 * поверх вкладки «Проекты», нижняя навигация при этом остаётся.
 *
 * Открытая вкладка (или «Создание проекта») запоминается как последний экран.
 * При первом открытии запрашивается разрешение на уведомления (Android 13+).
 */
class MainActivity : BaseActivity() {

    /** Имя меняется при переключении вкладок, см. updateScreenName() */
    override val screenName: String
        get() = currentScreen

    private var currentScreen = LastScreenNavigator.SCREEN_MAIN_TAB + TAB_HOME

    private lateinit var tabBar: AppTabBar

    // Ответ пользователя не важен: без разрешения напоминания просто не покажутся
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabBar = findViewById(R.id.tabBar)
        tabBar.setItems(AppTabBar.defaultItems())
        tabBar.setOnTabSelectedListener { index -> openTab(index) }

        // «Назад» с экрана «Создание проекта» тоже меняет последний экран
        supportFragmentManager.addOnBackStackChangedListener { updateScreenName() }

        if (savedInstanceState == null) {
            handleIntent(intent)
            askNotificationPermission()
        } else {
            tabBar.setSelectedIndex(savedInstanceState.getInt(KEY_TAB, TAB_HOME))
            updateScreenName()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, tabBar.getSelectedIndex())
    }

    /**
     * Открывает вкладку.
     * @param index TAB_HOME, TAB_CATALOG, TAB_PROJECTS или TAB_PROFILE
     */
    fun openTab(index: Int) {
        tabBar.setSelectedIndex(index)
        val fragment: Fragment = when (index) {
            TAB_CATALOG -> CatalogFragment()
            TAB_PROJECTS -> ProjectsFragment()
            TAB_PROFILE -> ProfileFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        // popBackStack выполняется позже, поэтому имя сохраняем сразу для новой вкладки
        saveScreenName(LastScreenNavigator.SCREEN_MAIN_TAB + index)
    }

    /** Открывает экран «Создание проекта»; «назад» вернёт к списку проектов. */
    fun openCreateProject() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, CreateProjectFragment())
            .addToBackStack(null)
            .commit()
    }

    /** Закрывает экран «Создание проекта». */
    fun closeCreateProject() {
        supportFragmentManager.popBackStack()
    }

    private fun handleIntent(intent: Intent) {
        openTab(intent.getIntExtra(EXTRA_TAB, TAB_HOME))
        if (intent.getBooleanExtra(EXTRA_CREATE_PROJECT, false)) openCreateProject()
    }

    private fun updateScreenName() {
        saveScreenName(
            if (supportFragmentManager.backStackEntryCount > 0) {
                LastScreenNavigator.SCREEN_CREATE_PROJECT
            } else {
                LastScreenNavigator.SCREEN_MAIN_TAB + tabBar.getSelectedIndex()
            }
        )
    }

    private fun saveScreenName(name: String) {
        currentScreen = name
        ServiceLocator.storage.saveLastScreen(name)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val EXTRA_TAB = "tab"
        private const val EXTRA_CREATE_PROJECT = "create_project"
        private const val KEY_TAB = "selected_tab"

        const val TAB_HOME = 0
        const val TAB_CATALOG = 1
        const val TAB_PROJECTS = 2
        const val TAB_PROFILE = 3

        /**
         * Intent для перехода на нужную вкладку из другого экрана.
         * @param context контекст
         * @param tab номер вкладки
         * @param openCreateProject сразу открыть «Создание проекта» поверх вкладки
         */
        fun createIntent(context: Context, tab: Int, openCreateProject: Boolean = false): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_TAB, tab)
                .putExtra(EXTRA_CREATE_PROJECT, openCreateProject)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
