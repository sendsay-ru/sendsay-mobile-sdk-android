package ru.sendsay.example.view.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.example.databinding.FragmentFlushBinding
import ru.sendsay.example.models.Constants
import ru.sendsay.example.view.base.BaseFragment
import ru.sendsay.sdk.Sendsay

class FlushFragment : BaseFragment() {

    private lateinit var viewBinding: FragmentFlushBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentFlushBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.subtitle = "flush"

        // Track visited screen
        trackPage(Constants.ScreenNames.settingsScreen)

        viewBinding.settingsBtnFlush.setOnClickListener {
            viewBinding.progressBar.visibility = View.VISIBLE
            Sendsay.flushData { result ->
                if (!this.isVisible) return@flushData
                Handler(Looper.getMainLooper()).post {
                    viewBinding.progressBar.visibility = View.INVISIBLE
                    AlertDialog.Builder(context)
                        .setTitle(if (result.isSuccess) "Flush successful" else "Flush failed")
                        .setMessage(if (result.isFailure) result.exceptionOrNull()?.localizedMessage else null)
                        .setPositiveButton("OK") { _, _ -> }
                        .create()
                        .show()
                }
            }
        }
    }
}
