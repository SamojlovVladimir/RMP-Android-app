package ru.samoylov.matule.uikit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * AppBottomSheet — выезжающая снизу панель UIKit (белая, скруглён верх).
 *
 * Высота подстраивается под контент. Если контент выше экрана, панель
 * открывается на весь экран, а внутри появляется скролл (NestedScrollView
 * в контейнере). Промежуточного «полуоткрытого» состояния нет.
 *
 * Использование:
 * val sheet = AppBottomSheet(context)
 * sheet.setContent(anyView)
 * sheet.show()
 *
 * @param context контекст экрана, на котором показывается панель
 */
class AppBottomSheet(context: Context) : BottomSheetDialog(context, R.style.AppBottomSheetTheme) {

    init {
        val container = LayoutInflater.from(context)
            .inflate(R.layout.view_bottom_sheet_container, null)
        setContentView(container)

        behavior.skipCollapsed = true
        setOnShowListener { behavior.state = BottomSheetBehavior.STATE_EXPANDED }
    }

    /**
     * Вставляет произвольный View внутрь панели.
     * @param contentView содержимое панели (любой layout или View)
     */
    fun setContent(contentView: View) {
        val contentContainer = findViewById<FrameLayout>(R.id.contentContainer)
        contentContainer?.removeAllViews()
        contentContainer?.addView(contentView)
    }
}
