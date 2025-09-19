package com.sendsay.example.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.sendsay.example.R
import com.sendsay.example.databinding.ActivityMainBinding
import com.sendsay.example.services.ExampleAppInboxProvider
import com.sendsay.example.view.NavigationItem.Anonymize
import com.sendsay.example.view.NavigationItem.Manual
import com.sendsay.example.view.NavigationItem.Track
import com.sendsay.example.view.NavigationItem.Fetch
import com.sendsay.example.view.NavigationItem.InAppContentBlock
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.models.SendsayNotificationActionType
import com.sendsay.sdk.models.InAppMessage
import com.sendsay.sdk.models.InAppMessageButton
import com.sendsay.sdk.models.InAppMessageCallback
import com.sendsay.sdk.models.PushNotificationDelegate
import com.sendsay.sdk.models.Segment
import com.sendsay.sdk.models.SegmentationDataCallback
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.isResumedActivity
import com.sendsay.sdk.util.isViewUrlIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlin.collections.mutableSetOf

class MainActivity : AppCompatActivity() {

    private val contentSegmentsCallback = object : SegmentationDataCallback() {
        override val exposingCategory = "content"
        override val includeFirstLoad = false
        override fun onNewData(segments: List<Segment>) {
            Logger.i(
                this@MainActivity,
                "Segments: New for category $exposingCategory with IDs: $segments"
            )
        }
    }

    private val discoverySegmentsCallback = object : SegmentationDataCallback() {
        override val exposingCategory = "discovery"
        override val includeFirstLoad = false
        override fun onNewData(segments: List<Segment>) {
            Logger.i(
                this@MainActivity,
                "Segments: New for category $exposingCategory with IDs: $segments"
            )
        }
    }

    private val merchandisingSegmentsCallback = object : SegmentationDataCallback() {
        override val exposingCategory = "merchandising"
        override val includeFirstLoad = false
        override fun onNewData(segments: List<Segment>) {
            Logger.i(
                this@MainActivity,
                "Segments: New for category $exposingCategory with IDs: $segments"
            )
        }
    }

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.title = "Examples"

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.toolbar) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                left = bars.left,
                top = bars.top,
                right = bars.right,
                bottom = bars.bottom,
            )
            WindowInsetsCompat.CONSUMED
        }

        // Set log level before first call to SDK function
        Sendsay.loggerLevel = Logger.Level.VERBOSE
        Sendsay.checkPushSetup = true
        Sendsay.handleCampaignIntent(intent, applicationContext)
        Sendsay.pushNotificationsDelegate = object : PushNotificationDelegate {
            override fun onSilentPushNotificationReceived(notificationData: Map<String, Any>) {
                informPushNotificationAction("Silent", "received", notificationData)
            }

            override fun onPushNotificationReceived(notificationData: Map<String, Any>) {
                informPushNotificationAction("Normal", "received", notificationData)
            }

            override fun onPushNotificationOpened(
                action: SendsayNotificationActionType,
                url: String?,
                notificationData: Map<String, Any>
            ) {
                informPushNotificationAction(action.name, "clicked $url", notificationData)
            }

            private fun informPushNotificationAction(
                notifType: String,
                notifFlow: String,
                notificationData: Map<String, Any>
            ) {
                val message = """
                    $notifType Push data $notifFlow:
                    ${notificationData.entries.joinToString { "${it.key}: ${it.value}" }}
                    """.trimIndent()
                if (this@MainActivity.isResumedActivity()) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("$notifType Push notification $notifFlow")
                        .setMessage(message)
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                } else {
                    Logger.i(this, message)
                }
            }
        }

        // TODO: Check this, is it works?
//        Uncomment this section, if you want to test in-app callback
//        Sendsay.inAppMessageActionCallback = getInAppMessageCallback()
//        Sendsay.appInboxProvider = ExampleAppInboxProvider()

        if (!Sendsay.isInitialized) {
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
            return
        }

        setupNavigation()
        Sendsay.registerSegmentationDataCallback(discoverySegmentsCallback)
        Sendsay.registerSegmentationDataCallback(contentSegmentsCallback)
        Sendsay.registerSegmentationDataCallback(merchandisingSegmentsCallback)

        val deeplinkDestination = resolveDeeplinkDestination(intent)

        if (deeplinkDestination != null) {
            handleDeeplinkDestination(deeplinkDestination)
        } else if (savedInstanceState == null) {
            navigateToItem(NavigationItem.Track)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val deeplinkDestination = resolveDeeplinkDestination(intent)
        if (deeplinkDestination != null) {
            handleDeeplinkDestination(deeplinkDestination)
        }
    }

    override fun onDestroy() {
        Sendsay.unregisterSegmentationDataCallback(discoverySegmentsCallback)
        Sendsay.unregisterSegmentationDataCallback(contentSegmentsCallback)
        Sendsay.unregisterSegmentationDataCallback(merchandisingSegmentsCallback)
        super.onDestroy()
    }

    private fun navigateToItem(item: NavigationItem) {
        viewBinding.navigation.selectedItemId = item.navigationId
    }

    private fun resolveDeeplinkDestination(intent: Intent?): DeeplinkFlow? {
        fun String.toDeeplinkDestination() = when {
            this.contains("track") -> DeeplinkFlow.Track
            this.contains("flush") -> DeeplinkFlow.Manual
            this.contains("fetch") -> if (Sendsay.isInAppMessagesEnabled) DeeplinkFlow.Fetch else DeeplinkFlow.Track
            this.contains("inappcb") -> if (Sendsay.isInAppCBEnabled) DeeplinkFlow.InAppCb else DeeplinkFlow.Track
            this.contains("anonymize") -> DeeplinkFlow.Anonymize
            this.contains("stopAndContinue") -> DeeplinkFlow.StopAndContinue
            this.contains("stopAndRestart") -> DeeplinkFlow.StopAndRestart
            else -> null
        }
        return if (intent.isViewUrlIntent("http")) {
            intent?.data?.path.orEmpty().toDeeplinkDestination()
        } else if (intent.isViewUrlIntent("sendsay")) {
            intent?.data?.path.orEmpty().toDeeplinkDestination()
        } else {
            null
        }
    }

    private fun handleDeeplinkDestination(deeplinkDestination: DeeplinkFlow) {
        when (deeplinkDestination) {
            DeeplinkFlow.Anonymize -> navigateToItem(Anonymize)
            DeeplinkFlow.Fetch -> if (Sendsay.isInAppMessagesEnabled) navigateToItem(Fetch)
            DeeplinkFlow.Manual -> navigateToItem(Manual)
            DeeplinkFlow.Track -> navigateToItem(Track)
            DeeplinkFlow.InAppCb -> if (Sendsay.isInAppCBEnabled) navigateToItem(InAppContentBlock)
            DeeplinkFlow.StopAndContinue -> {
                Sendsay.stopIntegration()
                if (viewBinding.navigation.selectedItemId == 0) {
                    navigateToItem(Track)
                }
            }

            DeeplinkFlow.StopAndRestart -> {
                Sendsay.stopIntegration()
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finish()
            }
        }
    }

    private fun setupNavigation() {
        val navController = getNavController()
        val topLevelDestinationIds = mutableSetOf<Int>(
            R.id.trackFragment,
            R.id.flushFragment,
            R.id.anonymizeFragment,
        ).also {
            if (Sendsay.isInAppMessagesEnabled) it.add(R.id.fetchFragment)
            if (Sendsay.isInAppCBEnabled) it.add(R.id.inAppContentBlocksFragment)
        }


        val appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = topLevelDestinationIds
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        viewBinding.navigation.setupWithNavController(navController)
        viewBinding.navigation.menu.findItem(R.id.inAppContentBlocks)?.isVisible =
            Sendsay.isInAppCBEnabled
        viewBinding.navigation.menu.findItem(R.id.fetchFragment)?.isVisible =
            Sendsay.isInAppMessagesEnabled
        onBackPressedDispatcher.addCallback {
            if (topLevelDestinationIds.contains(navController.currentDestination?.id)) {
                finish()
            } else if (!navController.navigateUp()) {
                finish()
            }
        }
    }

    private fun getNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = getNavController()
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun getInAppMessageCallback(): InAppMessageCallback {
        return object : InAppMessageCallback {
            override var overrideDefaultBehavior = true
            override var trackActions = false

            override fun inAppMessageShown(message: InAppMessage, context: Context) {
                Logger.i(this, "In app message ${message.name} has been shown")
                if (message.name.contains("StopSDK")) {
                    Logger.i(this, "In app message ${message.name} will stop SDK")
                    CoroutineScope(Dispatchers.Default).async {
                        delay(4000)
                        Logger.i(this, "Stopping SDK")
                        Sendsay.stopIntegration()
                    }
                }
            }

            override fun inAppMessageError(
                message: InAppMessage?,
                errorMessage: String,
                context: Context
            ) {
                Logger.e(
                    this,
                    "Error occurred '$errorMessage' while showing in app message ${message?.name}"
                )
            }

            override fun inAppMessageClickAction(
                message: InAppMessage,
                button: InAppMessageButton,
                context: Context
            ) {
//                AlertDialog.Builder(context)
//                    .setTitle("In app action clicked")
//                    .setMessage(" Message id: ${message.id} \n " +
//                        "Interaction: \n ${button.text} \n ${button.url}")
//                    .setPositiveButton("OK") { _, _ -> }
//                    .create()
//                    .show()

                Logger.i(this, "In app message ${message.name} has been clicked: ${button.url}")
                if (messageIsForGdpr(message)) {
                    handleGdprUserResponse(button)
                } else if (button.url != null) {
                    openUrl(button)
                }
            }

            private fun handleGdprUserResponse(button: InAppMessageButton) {
                when (button.url) {
                    "https://bloomreach.com/tracking/allow" -> {
                        Sendsay.trackEvent(
                            eventType = "gdpr",
                            properties =
                                hashMapOf(
                                    "status" to "allowed"
                                )
                        )
                    }

                    "https://bloomreach.com/tracking/deny" -> {
                        Logger.i(this, "Stopping SDK")
                        Sendsay.stopIntegration()
                    }
                }
            }

            private fun messageIsForGdpr(message: InAppMessage): Boolean {
                // apply your detection for GDPR related In-app
                // our example app is triggering GDPR In-app by custom event tracking so we used it for detection
                // you may implement detection against message title, ID, payload, etc.
                return message.applyEventFilter("event_name", mapOf("property" to "gdpr"), null)
            }

            private fun openUrl(button: InAppMessageButton) {
                try {
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            data = Uri.parse(button.url)
                        }
                    )
                } catch (e: ActivityNotFoundException) {
                    Logger.e(this, "Unable to open URL", e)
                }
            }

            override fun inAppMessageCloseAction(
                message: InAppMessage,
                button: InAppMessageButton?,
                interaction: Boolean,
                context: Context
            ) {
//                AlertDialog.Builder(context)
//                    .setTitle("In app closed")
//                    .setMessage(" Message id: ${message.id} \n " +
//                        "Interaction: $interaction \n ${button?.text} \n ${button?.url}")
//                    .setPositiveButton("OK") { _, _ -> }
//                    .create()
//                    .show()

                Logger.i(this, "In app message ${message.name} has been closed: ${button?.url}")
                if (messageIsForGdpr(message) && interaction) {
                    // regardless from `button` nullability, parameter `interaction` tells that user closed message
                    Logger.i(this, "Stopping SDK")
                    Sendsay.stopIntegration()
                }
            }
        }
    }

//    internal fun openCarousel() {
//        getNavController().navigate(NavigationItem.InAppCarousel.navigationId)
//    }
}
