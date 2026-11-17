package ru.samoylov.matule.presentation.projects

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.samoylov.matule.R
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.presentation.common.UiState
import ru.samoylov.matule.presentation.common.showMessage
import ru.samoylov.matule.presentation.main.MainActivity
import ru.samoylov.matule.uikit.AppBottomSheet
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppIcon
import ru.samoylov.matule.uikit.AppInput
import ru.samoylov.matule.uikit.AppSelect
import ru.samoylov.matule.uikit.AppSelectOption

/**
 * Экран «Создание проекта». Все поля обязательны.
 * По нажатию на «+» всплывает BottomSheet: фото из галереи или с камеры.
 * Камера вызывается системным приложением, поэтому разрешение CAMERA не нужно.
 */
class CreateProjectFragment : Fragment(R.layout.fragment_create_project) {

    private lateinit var viewModel: CreateProjectViewModel

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

    private val pickFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                photoPreview.setImageURI(uri)
                onPhotoSelected()
            }
        }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                photoPreview.setImageBitmap(bitmap)
                onPhotoSelected()
            }
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

        listOf(titleInput, startDateInput, endDateInput, sourceInput).forEach {
            it.setOnTextChangedListener { updateButton() }
        }
        listOf(typeSelect, targetSelect, categorySelect).forEach {
            it.setOnOptionSelectedListener { updateButton() }
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
            try {
                takePhoto.launch(null)
            } catch (e: Exception) {
                showMessage("На устройстве нет приложения камеры")
            }
        }
        sheet.setContent(content)
        sheet.show()
    }

    private fun onPhotoSelected() {
        viewModel.hasPhoto = true
        photoPreview.visibility = View.VISIBLE
        photoPlus.visibility = View.GONE
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
        category = categorySelect.getSelectedTitle()
    )
}
