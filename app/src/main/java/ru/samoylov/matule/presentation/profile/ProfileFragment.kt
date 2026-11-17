package ru.samoylov.matule.presentation.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.samoylov.matule.R
import ru.samoylov.matule.presentation.auth.AuthActivity
import ru.samoylov.matule.presentation.common.UiState
import ru.samoylov.matule.presentation.document.PdfActivity
import ru.samoylov.matule.presentation.common.showMessage
import ru.samoylov.matule.uikit.AppBottomSheet

/**
 * Экран «Профиль»: имя и почта, «Мои заказы», переключатель уведомлений,
 * политика конфиденциальности (PDF), пользовательское соглашение и выход.
 */
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var viewModel: ProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        val nameText = view.findViewById<TextView>(R.id.nameText)
        val emailText = view.findViewById<TextView>(R.id.emailText)

        view.findViewById<View>(R.id.ordersRow).setOnClickListener {
            showMessage("Здесь появится история ваших заказов")
        }
        view.findViewById<View>(R.id.privacyText).setOnClickListener {
            startActivity(PdfActivity.createPrivacyIntent(requireContext()))
        }
        view.findViewById<View>(R.id.agreementText).setOnClickListener {
            showDocument(getString(R.string.agreement_title), getString(R.string.agreement_text))
        }
        view.findViewById<View>(R.id.logoutText).setOnClickListener { viewModel.logout() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    nameText.text = state.data.displayName
                    emailText.text = state.data.email
                }
                is UiState.Error -> showMessage(state.message) {
                    if (state.requiresLogin) viewModel.logout()
                }
                else -> Unit
            }
        }
        viewModel.loggedOut.observe(viewLifecycleOwner) { loggedOut ->
            if (loggedOut) {
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        viewModel.load()
    }

    /** Показывает текст документа в BottomSheet со скроллом. */
    private fun showDocument(title: String, text: String) {
        val sheet = AppBottomSheet(requireContext())
        val content = LayoutInflater.from(requireContext()).inflate(R.layout.sheet_text, null)
        content.findViewById<TextView>(R.id.sheetTitle).text = title
        content.findViewById<TextView>(R.id.sheetText).text = text
        sheet.setContent(content)
        sheet.show()
    }
}
