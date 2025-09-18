package ru.sendsay.example.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.example.databinding.FragmentAnonymizeBinding
import ru.sendsay.example.models.Constants
import ru.sendsay.example.view.AuthenticationActivity
import ru.sendsay.example.view.base.BaseFragment
import ru.sendsay.sdk.Sendsay

class AnonymizeFragment : BaseFragment() {

    private lateinit var viewBinding: FragmentAnonymizeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentAnonymizeBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Track visited screen
        trackPage(Constants.ScreenNames.anonymizeScreen)

        (activity as AppCompatActivity).supportActionBar?.subtitle = "anonymize"
        viewBinding.btnAnonymize.setOnClickListener {
            Sendsay.anonymize()
            AlertDialog.Builder(context)
                .setTitle("Customer anonymized")
                .setMessage("Stored customer data cleared.")
                .setPositiveButton("OK") { _, _ -> }
                .create()
                .show()
        }

        viewBinding.btnStopIntegration.setOnClickListener {
            Sendsay.stopIntegration()
            AlertDialog.Builder(context)
                .setTitle("SDK stopped!")
                .setMessage("""
                    SDK has been de-integrated from your app.
                    You may return app 'Back to Auth' to re-integrate.
                    You may 'Continue' in using app without initialised SDK.
                """.trimIndent())
                .setPositiveButton("Back to Auth") { _, _ ->
                    startActivity(Intent(requireContext(), AuthenticationActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Continue") { _, _ -> }
                .create()
                .show()
        }
    }
}
