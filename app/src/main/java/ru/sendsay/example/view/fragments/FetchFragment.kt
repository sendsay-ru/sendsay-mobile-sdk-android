package ru.sendsay.example.view.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import ru.sendsay.example.databinding.FragmentFetchBinding
import ru.sendsay.example.models.Constants
import ru.sendsay.example.view.base.BaseFragment
import ru.sendsay.example.view.dialogs.FetchRecommendationDialog
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.CustomerRecommendationOptions
import ru.sendsay.sdk.models.FetchError
import ru.sendsay.sdk.models.Result

class FetchFragment : BaseFragment() {

    private lateinit var viewBinding: FragmentFetchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentFetchBinding.inflate(inflater, container, false)
        if (Sendsay.isAppInboxEnabled) viewBinding.buttonsContainer.addView(
            Sendsay.getAppInboxButton(requireActivity()),
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        )
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Track visited screen
        trackPage(Constants.ScreenNames.mainScreen)
        (activity as AppCompatActivity).supportActionBar?.subtitle = "fetching"

        // Init listeners
        initListeners()
    }

    private fun setProgressBarVisible(visible: Boolean) {
        if (visible) {
            viewBinding.progressBar.visibility = View.VISIBLE
        } else {
            viewBinding.progressBar.visibility = View.INVISIBLE
        }
    }

    /**
     * Initialize button listener
     */
    private fun initListeners() {
        viewBinding.recommendationsButton.setOnClickListener {
            FetchRecommendationDialog.show(childFragmentManager) { fetchRecommended(it) }
        }
        viewBinding.consentsButton.setOnClickListener {
            setProgressBarVisible(true)
            Sendsay.getConsents({ onFetchSuccess(it) }, { onFetchFailed(it) })
        }
        viewBinding.segmentationButton.setOnClickListener {
            val exposingCategory = "discovery"
            Sendsay.getSegments(exposingCategory = exposingCategory, force = false) { segments ->
                runOnUiThread {
                    viewBinding.resultTextView.text =
                        "Segments for $exposingCategory category:\n$segments"
                }
            }
        }
    }

    /**
     * Method handles recommendations loading
     */
    private fun fetchRecommended(options: CustomerRecommendationOptions) {
        setProgressBarVisible(true)
        Sendsay.fetchRecommendation(
            recommendationOptions = options,
            onSuccess = { onFetchSuccess(it) },
            onFailure = { onFetchFailed(it) }
        )
    }

    /**
     * Our success callback
     */
    private fun <T> onFetchSuccess(result: Result<ArrayList<T>>) {
        runOnUiThread {
            setProgressBarVisible(false)
            viewBinding.resultTextView.text = result.toString()
        }
    }

    /**
     * Our failure callback
     */
    private fun onFetchFailed(result: Result<FetchError>) {
        runOnUiThread {
            setProgressBarVisible(false)
            viewBinding.resultTextView.text = "Message: ${result.results.message}" +
                    "\nJson: ${result.results.jsonBody}"
        }
    }

    /**
     * Method to update ui
     */
    private fun runOnUiThread(runnable: () -> Unit) {
        Handler(Looper.getMainLooper()).post(runnable)
    }
}
