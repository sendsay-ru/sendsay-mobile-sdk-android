package com.sendsay.example.view.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.sendsay.example.databinding.FragmentAnonymizeBinding
import com.sendsay.example.models.Constants
import com.sendsay.example.view.AuthenticationActivity
import com.sendsay.example.view.base.BaseFragment
import com.sendsay.example.BuildConfig
import com.sendsay.sdk.Sendsay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        // Track visited screen (if build not RSM flavor)
        if (BuildConfig.FLAVOR != "RSM") {
            trackPage(Constants.ScreenNames.anonymizeScreen)
        }

        (activity as AppCompatActivity).supportActionBar?.subtitle = "anonymize"
        viewBinding.btnAnonymize.setOnClickListener {
//            Sendsay.anonymize()
            CoroutineScope(Dispatchers.Default).launch {
                Sendsay.trackGAID(requireContext())
            }.invokeOnCompletion { Sendsay.getGAID() }

            AlertDialog.Builder(requireContext())
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
