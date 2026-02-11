package com.sendsay.example.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.sendsay.example.App
import com.sendsay.example.databinding.ActivityAuthenticationBinding
import com.sendsay.example.managers.CustomerTokenStorage
import com.sendsay.example.utils.isVaildUrl
import com.sendsay.example.utils.isValid
import com.sendsay.example.utils.onTextChanged
import com.sendsay.sdk.BuildConfig
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.Sendsay.runDebugMode
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.SendsayConfiguration.Companion.TOKEN_AUTH_PREFIX
import com.sendsay.sdk.models.SendsayConfiguration.TokenFrequency.EVERY_LAUNCH
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.services.SendsayContextProvider
import com.sendsay.sdk.util.Logger
import kotlin.collections.set

class AuthenticationActivity : AppCompatActivity() {
    // Simple Sendsay configuration - without SDK init
    val defaultProperties = CustomerTokenStorage.INSTANCE

    var projectToken = defaultProperties.projectToken ?: ""
    var apiUrl = defaultProperties.host ?: "https://mobi.sendsay.ru/xnpe/v100"
    var authorizationToken =
        "Token ${defaultProperties.authToken ?: ""}"
    var advancedPublicKey = defaultProperties.publicKey ?: "PK"
    var registeredIds = defaultProperties.customerIds?.values?.last() ?: ""

    private lateinit var viewBinding: ActivityAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.editTextAuthCode.setText(authorizationToken)
        viewBinding.editTextAdvancedPublicKey.setText(advancedPublicKey)
        viewBinding.editTextRegisteredIds.setText(registeredIds)
        viewBinding.editTextProjectToken.setText(projectToken)
        viewBinding.editTextApiUrl.setText(apiUrl)

        viewBinding.editTextAuthCode.onTextChanged { authorizationToken = it }
        viewBinding.editTextAdvancedPublicKey.onTextChanged { advancedPublicKey = it }
        viewBinding.editTextRegisteredIds.onTextChanged { registeredIds = it }
        viewBinding.editTextProjectToken.onTextChanged { projectToken = it }
        viewBinding.editTextApiUrl.onTextChanged { apiUrl = it }

        viewBinding.authenticateButton.setOnClickListener {
            if (!viewBinding.editTextProjectToken.isValid() ||
                !viewBinding.editTextAuthCode.isValid() ||
                !viewBinding.editTextApiUrl.isVaildUrl()
            ) {
                Toast.makeText(this, "Empty field", Toast.LENGTH_SHORT).show()
            } else {
                initSdk()
            }
        }

        viewBinding.clearLocalDataButton.setOnClickListener {
            Sendsay.clearLocalCustomerData()
        }
    }

    private fun initSdk() {
        val configuration = SendsayConfiguration()
        // Saving current field state
        configuration.defaultProperties["projectToken"] = projectToken
        configuration.defaultProperties["apiUrl"] = apiUrl
        configuration.defaultProperties["authorizationToken"] = authorizationToken.split(" ").last()
        configuration.defaultProperties["advancedPublicKey"] = advancedPublicKey
        configuration.defaultProperties["registeredIds"] = registeredIds

        // ingore existing prefix for authorization token (Token, Bearer or Basic)
        configuration.authorization =
            TOKEN_AUTH_PREFIX + authorizationToken.split(" ").last()
        configuration.advancedAuthEnabled = advancedPublicKey.isNotBlank()
        configuration.projectToken = projectToken
        configuration.baseURL = apiUrl
        configuration.httpLoggingLevel = SendsayConfiguration.HttpLoggingLevel.BODY
//        configuration.defaultProperties["thisIsADefaultStringProperty"] =
//            "This is a default string value"
//        configuration.defaultProperties["thisIsADefaultIntProperty"] = 1
        configuration.automaticPushNotification = true
        configuration.tokenTrackFrequency = EVERY_LAUNCH
        configuration.pushChannelId = "Push channel (Sendsay)"

        // Prepare Example Advanced Auth
        CustomerTokenStorage.INSTANCE.configure(
            host = apiUrl,
            projectToken = projectToken,
            authToken = authorizationToken.split(" ").last(),
            publicKey = advancedPublicKey,
            customerIds = null,
            expiration = null
        )

        // Set our customer registration id
        if (viewBinding.editTextRegisteredIds.isValid()) {
            App.instance.registeredIdManager.registeredID = registeredIds
            CustomerTokenStorage.INSTANCE.configure(
                customerIds = hashMapOf(
                    "registered" to (App.instance.registeredIdManager.registeredID ?: ""),
                )
            )
        }

        // Set up our flushing
        Sendsay.flushMode = FlushMode.IMMEDIATE
        Sendsay.checkPushSetup = runDebugMode

        // Start our SDK
        // Sendsay.initFromFile(App.instance)
        try {
            Sendsay.init(App.instance, configuration)
        } catch (e: Exception) {
            AlertDialog.Builder(this)
                .setTitle("Error configuring SDK")
                .setMessage(e.localizedMessage)
                .setPositiveButton("OK") { _, _ -> }
                .create()
                .show()
            return
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
