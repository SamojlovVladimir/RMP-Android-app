package ru.samoylov.matule.presentation.showcase

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import ru.samoylov.matule.R
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.uikit.AppBottomSheet
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppCard
import ru.samoylov.matule.uikit.AppCartButton
import ru.samoylov.matule.uikit.AppCounter
import ru.samoylov.matule.uikit.AppHeader
import ru.samoylov.matule.uikit.AppIcon
import ru.samoylov.matule.uikit.AppIcons
import ru.samoylov.matule.uikit.AppInput
import ru.samoylov.matule.uikit.AppPinPad
import ru.samoylov.matule.uikit.AppSelect
import ru.samoylov.matule.uikit.AppSelectOption
import ru.samoylov.matule.uikit.AppTabBar
import ru.samoylov.matule.uikit.R as UiKitR

/**
 * Витрина библиотеки UIKit (Модуль Б, спринт 1): все компоненты во всех
 * состояниях. Открывается долгим нажатием на надпись «Matule» на заставке.
 */
class ShowcaseActivity : BaseActivity() {

    override val screenName = "showcase"

    private val density by lazy { resources.displayMetrics.density }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_showcase)

        setupColors()
        setupIcons()
        setupInputs()
        setupSelects()
        setupHeaders()
        setupCards()
        setupModals()
        setupTabBars()

        findViewById<AppCartButton>(R.id.cartButtonDemo).setTotal("500 ₽")
        findViewById<AppPinPad>(R.id.pinPadDemo).setOnPinEnteredListener { pin ->
            showMessage("Введён код $pin")
            findViewById<AppPinPad>(R.id.pinPadDemo).clear()
        }
    }

    private fun setupColors() {
        val container = findViewById<LinearLayout>(R.id.colorsContainer)
        val colors = listOf(
            "Accent" to UiKitR.color.accent,
            "Accent inactive" to UiKitR.color.accent_inactive,
            "Black" to UiKitR.color.black,
            "White" to UiKitR.color.white,
            "Error" to UiKitR.color.error,
            "Success" to UiKitR.color.success,
            "Инпут бг" to UiKitR.color.input_bg,
            "Инпут строк" to UiKitR.color.input_stroke,
            "Инпут иконки" to UiKitR.color.input_icon,
            "Плейсхолдер" to UiKitR.color.placeholder,
            "Дескрипшн" to UiKitR.color.description,
            "Кард строк" to UiKitR.color.card_stroke
        )
        colors.forEach { (name, colorRes) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, dp(4))
            }
            row.addView(View(this).apply {
                setBackgroundColor(ContextCompat.getColor(context, colorRes))
                elevation = 1 * density
            }, LinearLayout.LayoutParams(dp(32), dp(32)))
            row.addView(TextView(this).apply {
                text = name
                setPadding(dp(12), 0, 0, 0)
            })
            container.addView(row)
        }
    }

    /** Все иконки шрифта по четыре в ряд с подписями. */
    private fun setupIcons() {
        val container = findViewById<LinearLayout>(R.id.iconsContainer)
        var row = LinearLayout(this)
        AppIcons.byName.entries.forEachIndexed { index, (name, glyph) ->
            if (index % 4 == 0) {
                val newRow = LinearLayout(this)
                newRow.orientation = LinearLayout.HORIZONTAL
                container.addView(newRow)
                row = newRow
            }
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(0, dp(8), 0, dp(8))
            }
            cell.addView(AppIcon(this).apply { setIcon(glyph) })
            cell.addView(TextView(this).apply {
                text = name
                textSize = 10f
            })
            row.addView(cell, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun setupInputs() {
        findViewById<AppInput>(R.id.inputFilled).setText("Иван")
        findViewById<AppInput>(R.id.inputError).setError("Введите ваше имя")
        findViewById<AppInput>(R.id.inputDisabled).isEnabled = false
        findViewById<AppInput>(R.id.inputPassword).setText("oNQZnz\$Hx2")
    }

    private fun setupSelects() {
        val genders = listOf(AppSelectOption("Мужской"), AppSelectOption("Женский"))
        findViewById<AppSelect>(R.id.selectEmpty).setOptions(genders)
        findViewById<AppSelect>(R.id.selectValue).apply {
            setOptions(genders)
            selectByTitle("Мужской")
        }
        findViewById<AppSelect>(R.id.selectEmoji).apply {
            setOptions(
                listOf(
                    AppSelectOption("Гарвард Троцкий", "🧑‍🎓"),
                    AppSelectOption("Личный проект", "🧑"),
                    AppSelectOption("Командный проект", "👥")
                )
            )
            selectByTitle("Гарвард Троцкий")
        }
    }

    private fun setupHeaders() {
        listOf(R.id.headerLarge, R.id.headerSmall).forEach { id ->
            findViewById<AppHeader>(id).apply {
                setOnBackClickListener { showMessage("Назад") }
                setActionIcon(AppIcons.TRASH) { showMessage("Очистить корзину") }
            }
        }
    }

    private fun setupCards() {
        val container = findViewById<LinearLayout>(R.id.cardsContainer)

        val primary = AppCard(this).apply {
            setTitle("Рубашка Воскресенье для машинного вязания")
            setSubtitle("Мужская одежда")
            setPrice("300 ₽")
            setActionView(AppButton.small(context, "Добавить"))
        }
        val primaryInCart = AppCard(this).apply {
            setTitle("Рубашка Воскресенье для машинного вязания")
            setSubtitle("Мужская одежда")
            setPrice("300 ₽")
            setActionView(AppButton.small(context, "Убрать", AppButton.VARIANT_OUTLINE))
        }
        val cart = AppCard(this).apply {
            setTitle("Рубашка воскресенье для машинного вязания")
            setPrice("300 ₽")
            setActionView(AppCounter(context))
            setOnCloseListener { showMessage("Удалено из корзины") }
        }
        val project = AppCard(this).apply {
            setTitle("Мой первый проект")
            setCaption("Прошло 2 дня")
            setActionView(AppButton.small(context, "Открыть"))
        }

        listOf(primary, primaryInCart, cart, project).forEach { card ->
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dp(12)
            container.addView(card, params)
        }
    }

    private fun setupModals() {
        findViewById<AppButton>(R.id.openSheetButton).setOnClickListener {
            showSheet("Рубашка Воскресенье для машинного вязания", "Короткий текст — высота по контенту.")
        }
        findViewById<AppButton>(R.id.openLongSheetButton).setOnClickListener {
            showSheet("Длинное описание", "Мой выбор для этих шапок – кардные составы. ".repeat(80))
        }
        findViewById<AppButton>(R.id.openSnackButton).setOnClickListener {
            showMessage("Произошла ошибка\nНу вот опять")
        }
    }

    private fun showSheet(title: String, text: String) {
        val sheet = AppBottomSheet(this)
        val content = layoutInflater.inflate(R.layout.sheet_text, null)
        content.findViewById<TextView>(R.id.sheetTitle).text = title
        content.findViewById<TextView>(R.id.sheetText).text = text
        sheet.setContent(content)
        sheet.show()
    }

    /** Четыре состояния TabBar: активна каждая из вкладок. */
    private fun setupTabBars() {
        val container = findViewById<LinearLayout>(R.id.tabBarsContainer)
        repeat(4) { index ->
            val tabBar = AppTabBar(this)
            tabBar.setItems(AppTabBar.defaultItems())
            tabBar.setSelectedIndex(index)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dp(8)
            container.addView(tabBar, params)
        }
    }

    private fun dp(value: Int): Int = (value * density).toInt()
}
