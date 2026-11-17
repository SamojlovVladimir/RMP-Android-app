package ru.samoylov.matule.presentation.document

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.LinearLayout
import ru.samoylov.matule.R
import ru.samoylov.matule.presentation.common.BaseActivity
import ru.samoylov.matule.presentation.common.LastScreenNavigator
import ru.samoylov.matule.uikit.AppHeader
import java.io.File

/**
 * Просмотр PDF-документа из папки assets (политика конфиденциальности).
 *
 * Используется встроенный в Android PdfRenderer: каждая страница рисуется
 * в картинку по ширине экрана, страницы листаются прокруткой.
 */
class PdfActivity : BaseActivity() {

    override val screenName = LastScreenNavigator.SCREEN_PDF

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)

        val header = findViewById<AppHeader>(R.id.header)
        header.setTitle(intent.getStringExtra(EXTRA_TITLE).orEmpty())
        header.setOnBackClickListener { finish() }

        val assetName = intent.getStringExtra(EXTRA_ASSET) ?: return
        try {
            showPages(assetName, findViewById(R.id.pagesContainer))
        } catch (e: Exception) {
            showMessage("Не удалось открыть документ")
        }
    }

    private fun showPages(assetName: String, container: LinearLayout) {
        // PdfRenderer читает только настоящий файл, поэтому копируем документ из assets
        val file = File(cacheDir, assetName)
        if (!file.exists()) {
            assets.open(assetName).use { input -> file.outputStream().use { input.copyTo(it) } }
        }

        val pageWidth = resources.displayMetrics.widthPixels
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        try {
            for (index in 0 until renderer.pageCount) {
                val page = renderer.openPage(index)
                val pageHeight = pageWidth * page.height / page.width
                val bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val image = ImageView(this)
                image.adjustViewBounds = true
                image.setImageBitmap(bitmap)
                container.addView(
                    image,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        } finally {
            renderer.close()
        }
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_ASSET = "asset"

        /**
         * Intent для открытия политики конфиденциальности.
         * @param context контекст
         */
        fun createPrivacyIntent(context: Context): Intent =
            Intent(context, PdfActivity::class.java)
                .putExtra(EXTRA_TITLE, context.getString(R.string.privacy_title))
                .putExtra(EXTRA_ASSET, "privacy_policy.pdf")
    }
}
