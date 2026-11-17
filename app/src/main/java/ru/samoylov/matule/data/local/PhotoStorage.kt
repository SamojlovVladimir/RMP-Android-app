package ru.samoylov.matule.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Хранение фотографий проектов во внутренней папке приложения.
 *
 * Фото с камеры записывается сразу в файл, фото из галереи копируется в файл.
 * Так путь к фото можно сохранить в черновике, и фото не пропадёт после перезапуска.
 *
 * @param context контекст приложения
 */
class PhotoStorage(private val context: Context) {

    private val folder: File
        get() = File(context.filesDir, FOLDER).apply { mkdirs() }

    /** Создаёт пустой файл для нового фото. */
    fun createFile(): File = File(folder, "photo_${System.currentTimeMillis()}.jpg")

    /**
     * Адрес файла, по которому приложение камеры сможет записать фото.
     * @param file файл из createFile()
     */
    fun getUriForCamera(file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    /**
     * Копирует выбранное в галерее фото к себе.
     * @param uri адрес фото из галереи
     * @return путь к копии или null, если файл не прочитался
     */
    fun copyFromGallery(uri: Uri): String? {
        val file = createFile()
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            file.absolutePath
        } catch (e: Exception) {
            file.delete()
            null
        }
    }

    /**
     * Загружает уменьшенное фото для показа на экране и поворачивает его,
     * если камера сохранила его «на боку».
     * @param path путь к файлу
     * @param maxSize наибольшая сторона картинки в пикселях
     */
    fun loadPreview(path: String, maxSize: Int): Bitmap? {
        if (!File(path).exists()) return null

        // Сначала узнаём размер, чтобы не загружать в память фото 4000×3000
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) {
            sample *= 2
        }
        val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null

        val degrees = when (runCatching { ExifInterface(path) }.getOrNull()
            ?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private companion object {
        const val FOLDER = "photos"
    }
}
