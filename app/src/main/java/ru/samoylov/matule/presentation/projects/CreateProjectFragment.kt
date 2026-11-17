package ru.samoylov.matule.presentation.projects

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.samoylov.matule.R
import ru.samoylov.matule.di.ServiceLocator
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.presentation.common.UiState
import ru.samoylov.matule.presentation.common.dp
import ru.samoylov.matule.presentation.common.showMessage
import ru.samoylov.matule.presentation.main.MainActivity
import ru.samoylov.matule.uikit.AppBottomSheet
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppIcon
import ru.samoylov.matule.uikit.AppInput
import ru.samoylov.matule.uikit.AppSelect
import ru.samoylov.matule.uikit.AppSelectOption
import java.io.File

/**
 * Экран «Создание проекта». Все поля обязательны.
 * По нажатию на «+» всплывает BottomSheet: фото из галереи или с камеры.
 * Камера вызывается системным приложением и пишет фото в полном размере
 * в файл приложения, поэтому разрешение CAMERA не нужно.
 *
 * Каждое изменение формы сохраняется как черновик: после перезапуска
 * приложения экран откроется с теми же данными и фото.
 */
class CreateProjectFragment : Fragment(R.layout.fragment_create_project) {

    private lateinit var viewModel: CreateProjectViewModel
    private val photoStorage = ServiceLocator.photoStorage

    private lateinit var typeSelect: AppSelect
    private lateinit var titleInput: AppInput
    private lateinit var startDateInput: AppInput
    private lateinit var endDateInput: AppInput
    private lateinit var targetSelect: AppSelect
    private lateinit var sourceInput: AppInput
    private lateinit var categorySelect: AppSelect
    private lateinit var photoPreview: ImageView
    private lateinit var photoPlus: AppIcon
    private lateinit var confirmButton: AppButton

    // Файл, в который сейчас пишет камера. Сохраняется в Bundle, потому что
    // система может закрыть приложение, пока открыта камера
    private var pendingCameraPath: String? = null

    private val pickFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val path = photoStorage.copyFromGallery(uri)
            if (path != null) onPhotoSelected(path) else showMessage("Не удалось загрузить фото")
        }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            val path = pendingCameraPath ?: return@registerForActivityResult
            pendingCameraPath = null
            if (success) onPhotoSelected(path) else File(path).delete()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingCameraPath = savedInstanceState?.getString(KEY_PENDING_PHOTO)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PENDING_PHOTO, pendingCameraPath)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CreateProjectViewModel::class.java]

        typeSelect = view.findViewById(R.id.typeSelect)
        titleInput = view.findViewById(R.id.titleInput)
        startDateInput = view.findViewById(R.id.startDateInput)
        endDateInput = view.findViewById(R.id.endDateInput)
        targetSelect = view.findViewById(R.id.targetSelect)
        sourceInput = view.findViewById(R.id.sourceInput)
        categorySelect = view.findViewById(R.id.categorySelect)
        photoPreview = view.findViewById(R.id.photoPreview)
        photoPlus = view.findViewById(R.id.photoPlus)
        confirmButton = view.findViewById(R.id.confirmButton)

        typeSelect.setOptions(
            listOf(
                AppSelectOption("Личный", "🧑"),
                AppSelectOption("Семейный", "👨‍👩‍👧"),
                AppSelectOption("На заказ", "💼")
            )
        )
        targetSelect.setOptions(
            listOf(
                AppSelectOption("Себе", "🙋"),
                AppSelectOption("Женщине", "👩"),
                AppSelectOption("Мужчине", "👨"),
                AppSelectOption("Ребёнку", "🧒")
            )
        )
        categorySelect.setOptions(
            listOf(
                AppSelectOption("Одежда", "👕"),
                AppSelectOption("Аксессуары", "🧣"),
                AppSelectOption("Игрушки", "🧸")
            )
        )

        // Сначала восстанавливаем черновик, потом вешаем обработчики,
        // чтобы восстановление не перезаписывало черновик по одному полю
        restoreDraft()

        listOf(titleInput, startDateInput, endDateInput, sourceInput).forEach {
            it.setOnTextChangedListener { onFormChanged() }
        }
        listOf(typeSelect, targetSelect, categorySelect).forEach {
            it.setOnOptionSelectedListener { onFormChanged() }
        }

        val photoTile = view.findViewById<View>(R.id.photoTile)
        photoTile.clipToOutline = true
        photoTile.setOnClickListener { showPhotoSourceSheet() }
        confirmButton.setOnClickListener { viewModel.create(readForm()) }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> confirmButton.isEnabled = false
                is UiState.Success -> (activity as? MainActivity)?.closeCreateProject()
                is UiState.Error -> {
                    updateButton()
                    showMessage(state.message) { viewModel.consumeState() }
                }
                is UiState.Idle -> updateButton()
            }
        }
    }

    private fun restoreDraft() {
        val draft = viewModel.restoreDraft() ?: return
        typeSelect.selectByTitle(draft.type)
        titleInput.setText(draft.title)
        startDateInput.setText(draft.startDate)
        endDateInput.setText(draft.endDate)
        targetSelect.selectByTitle(draft.target)
        sourceInput.setText(draft.source)
        categorySelect.selectByTitle(draft.category)
        showPhoto(viewModel.photoPath)
    }

    /** BottomSheet выбора: галерея или камера. */
    private fun showPhotoSourceSheet() {
        val sheet = AppBottomSheet(requireContext())
        val content = LayoutInflater.from(requireContext())
            .inflate(R.layout.sheet_photo_source, null)
        content.findViewById<AppButton>(R.id.galleryButton).setOnClickListener {
            sheet.dismiss()
            pickFromGallery.launch("image/*")
        }
        content.findViewById<AppButton>(R.id.cameraButton).setOnClickListener {
            sheet.dismiss()
            openCamera()
        }
        sheet.setContent(content)
        sheet.show()
    }

    private fun openCamera() {
        val file = photoStorage.createFile()
        pendingCameraPath = file.absolutePath
        try {
            takePhoto.launch(photoStorage.getUriForCamera(file))
        } catch (e: ActivityNotFoundException) {
            pendingCameraPath = null
            showMessage("На устройстве нет приложения камеры")
        }
    }

    private fun onPhotoSelected(path: String) {
        viewModel.photoPath = path
        showPhoto(path)
        onFormChanged()
    }

    /** Показывает фото в плитке или «+», если фото нет. */
    private fun showPhoto(path: String) {
        val bitmap = if (path.isBlank()) null else photoStorage.loadPreview(path, dp(PREVIEW_SIZE_DP))
        if (bitmap == null) {
            viewModel.photoPath = ""
            photoPreview.visibility = View.GONE
            photoPlus.visibility = View.VISIBLE
        } else {
            photoPreview.setImageBitmap(bitmap)
            photoPreview.visibility = View.VISIBLE
            photoPlus.visibility = View.GONE
        }
    }

    /** Сохраняет черновик и обновляет кнопку. */
    private fun onFormChanged() {
        viewModel.saveDraft(readForm())
        updateButton()
    }

    private fun updateButton() {
        confirmButton.isEnabled = viewModel.isComplete(readForm())
    }

    private fun readForm(): Project = Project(
        id = "",
        title = titleInput.getText(),
        type = typeSelect.getSelectedTitle(),
        startDate = startDateInput.getText(),
        endDate = endDateInput.getText(),
        target = targetSelect.getSelectedTitle(),
        source = sourceInput.getText(),
        category = categorySelect.getSelectedTitle(),
        photoUrl = viewModel.photoPath
    )

    private companion object {
        const val KEY_PENDING_PHOTO = "pending_photo"

        /** Размер плитки фото на макете */
        const val PREVIEW_SIZE_DP = 190
    }
}
