package ru.samoylov.matule.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import ru.samoylov.matule.R
import ru.samoylov.matule.presentation.catalog.CatalogFragment
import ru.samoylov.matule.presentation.common.BaseActivity
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
 */
class MainActivity : BaseActivity() {

    override val screenName = "main"

    private lateinit var tabBar: AppTabBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabBar = findViewById(R.id.tabBar)
        tabBar.setItems(AppTabBar.defaultItems())
        tabBar.setOnTabSelectedListener { index -> openTab(index) }

        if (savedInstanceState == null) {
            openTab(intent.getIntExtra(EXTRA_TAB, TAB_HOME))
        } else {
            tabBar.setSelectedIndex(savedInstanceState.getInt(KEY_TAB, TAB_HOME))
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openTab(intent.getIntExtra(EXTRA_TAB, TAB_HOME))
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

    companion object {
        private const val EXTRA_TAB = "tab"
        private const val KEY_TAB = "selected_tab"

        const val TAB_HOME = 0
        const val TAB_CATALOG = 1
        const val TAB_PROJECTS = 2
        const val TAB_PROFILE = 3

        /**
         * Intent для перехода на нужную вкладку из другого экрана.
         * @param context контекст
         * @param tab номер вкладки
         */
        fun createIntent(context: Context, tab: Int): Intent =
            Intent(context, MainActivity::class.java)
                .putExtra(EXTRA_TAB, tab)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
