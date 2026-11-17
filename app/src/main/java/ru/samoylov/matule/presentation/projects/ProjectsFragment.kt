package ru.samoylov.matule.presentation.projects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.samoylov.matule.R
import ru.samoylov.matule.domain.model.Project
import ru.samoylov.matule.presentation.common.dp
import ru.samoylov.matule.presentation.common.showMessage
import ru.samoylov.matule.presentation.main.MainActivity
import ru.samoylov.matule.uikit.AppBottomSheet
import ru.samoylov.matule.uikit.AppButton
import ru.samoylov.matule.uikit.AppCard
import ru.samoylov.matule.uikit.AppHeader
import ru.samoylov.matule.uikit.AppIcons

/**
 * Экран «Список проектов»: карточки проектов с кнопкой «Открыть»
 * и «+» в хэдере для создания нового проекта.
 */
class ProjectsFragment : Fragment(R.layout.fragment_projects) {

    private lateinit var viewModel: ProjectsViewModel
    private lateinit var projectsContainer: LinearLayout
    private lateinit var emptyText: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProjectsViewModel::class.java]

        projectsContainer = view.findViewById(R.id.projectsContainer)
        emptyText = view.findViewById(R.id.emptyText)

        view.findViewById<AppHeader>(R.id.header).setActionIcon(AppIcons.PLUS) {
            (activity as? MainActivity)?.openCreateProject()
        }

        viewModel.projects.observe(viewLifecycleOwner) { showProjects(it) }
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) showMessage(message) { viewModel.clearError() }
        }

        viewModel.load()
    }

    private fun showProjects(projects: List<Project>) {
        projectsContainer.removeAllViews()
        projects.forEach { project ->
            val card = AppCard(requireContext())
            card.setTitle(project.title)
            card.setCaption(project.updatedAt.ifBlank { "Только что" })
            val button = AppButton.small(requireContext(), "Открыть")
            button.setOnClickListener { showDetails(project) }
            card.setActionView(button)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = dp(16)
            projectsContainer.addView(card, params)
        }
        emptyText.visibility = if (projects.isEmpty()) View.VISIBLE else View.GONE
    }

    /** Открывает проект: BottomSheet со всеми полями. */
    private fun showDetails(project: Project) {
        val sheet = AppBottomSheet(requireContext())
        val content = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_text, null)
        content.findViewById<TextView>(R.id.sheetTitle).text = project.title
        content.findViewById<TextView>(R.id.sheetText).text =
            "Тип: ${project.type}\n" +
                "Даты: ${project.startDate} – ${project.endDate}\n" +
                "Кому: ${project.target}\n" +
                "Источник описания: ${project.source}\n" +
                "Категория: ${project.category}"
        sheet.setContent(content)
        sheet.show()
    }
}
