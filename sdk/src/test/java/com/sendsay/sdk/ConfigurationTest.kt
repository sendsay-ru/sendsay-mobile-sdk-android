package com.sendsay.sdk

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.exceptions.InvalidConfigurationException
import com.sendsay.sdk.models.EventType
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.SendsayConfiguration.HttpLoggingLevel.BASIC
import com.sendsay.sdk.models.SendsayConfiguration.TokenFrequency.EVERY_LAUNCH
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.testutil.SendsaySDKTest
import com.sendsay.sdk.testutil.componentForTesting
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ConfigurationTest : SendsaySDKTest() {

    private fun setupSendsay(
        authorization: String,
        projectToken: String = "projectToken",
        projectMapping: Map<EventType, List<SendsayProject>>? = null
    ) {
        val configuration = SendsayConfiguration(
            projectToken = projectToken,
            authorization = authorization
        )
        projectMapping?.let {
            configuration.projectRouteMap = it
        }
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(ApplicationProvider.getApplicationContext(), configuration)
    }

    @Rule @JvmField
    val expectedException = ExpectedException.none()

    @Test
    fun `should initialize with correct authorization`() {
        setupSendsay("Token asdf")
        assertEquals(Sendsay.isInitialized, true)
    }

    @Test
    fun `should throw error when initializing sdk with basic authorization`() {
        expectedException.expect(InvalidConfigurationException::class.java)
        expectedException.expectMessage("""
            Basic authentication is not supported by mobile SDK for security reasons.
        """.trimIndent())
        setupSendsay("Basic asdf")
        assertEquals(Sendsay.isInitialized, true)
    }

    @Test
    fun `should throw error when initializing sdk with unknown authorization`() {
        expectedException.expect(InvalidConfigurationException::class.java)
        expectedException.expectMessage("Use 'Token <access token>' as authorization for SDK.")
        setupSendsay("asdf")
        assertEquals(Sendsay.isInitialized, true)
    }

    @Test
    fun `should initialize SDK from file`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Sendsay.flushMode = FlushMode.MANUAL
        @Suppress("DEPRECATION")
        Sendsay.initFromFile(context)
        assertEquals(Sendsay.isInitialized, true)
    }

    @Test
    fun `should initialize with correct secured authorization`() {
        setupSendsay("Token asdf")
        assertEquals(Sendsay.isInitialized, true)
    }

    @Test
    fun `should use basic token for no secured token`() {
        val basicToken = "Token asdf"
        setupSendsay(basicToken)
        assertEquals(
            basicToken,
            Sendsay.componentForTesting.projectFactory.mutualSendsayProject.authorization
        )
    }

    @Test
    fun `should deserialize empty config`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val runTimeConfig = SendsayConfiguration()
        SendsayConfigRepository.set(context, runTimeConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(runTimeConfig, storedConfig)
    }

    @Test
    fun `should deserialize basic config`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val runTimeConfig = SendsayConfiguration(
            projectToken = "project-token",
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com"
        )
        SendsayConfigRepository.set(context, runTimeConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(runTimeConfig, storedConfig)
    }

    @Test
    fun `should deserialize full config`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val runTimeConfig = SendsayConfiguration(
            projectToken = "project-token",
            projectRouteMap = mapOf(EventType.TRACK_CUSTOMER to listOf(SendsayProject(
                baseUrl = "https://api.sendsay.com",
                projectToken = "project-token",
                authorization = "Token mock-auth"
            ))),
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com",
            httpLoggingLevel = BASIC,
            maxTries = 20,
            sessionTimeout = 60.0,
            campaignTTL = 20.0,
            automaticSessionTracking = true,
            automaticPushNotification = true,
            pushIcon = 1,
            pushAccentColor = 1,
            pushChannelName = "Push",
            pushChannelDescription = "Description",
            pushChannelId = "1",
            pushNotificationImportance = NotificationManager.IMPORTANCE_HIGH,
            defaultProperties = hashMapOf("def" to "val"),
            tokenTrackFrequency = EVERY_LAUNCH,
            allowDefaultCustomerProperties = true
        )
        SendsayConfigRepository.set(context, runTimeConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(runTimeConfig, storedConfig)
    }

    @Test
    fun `should deserialize config after switch project`() {
        // de-init to be able init SDK
        resetSendsay()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sdkConfig = SendsayConfiguration(
            projectToken = "project-token",
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com"
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, sdkConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(sdkConfig, storedConfig)
        // switch project
        val baseUrlSwitch = "https://switch-api.sendsay.com"
        val projectTokenSwitch = "switch-project-token"
        val authorizationSwitch = "Token switch-mock-auth"
        Sendsay.anonymize(
            sendsayProject = SendsayProject(
                baseUrl = baseUrlSwitch,
                projectToken = projectTokenSwitch,
                authorization = authorizationSwitch
            )
        )
        val storedConfigAfterSwitch = SendsayConfigRepository.get(context)
        assertEquals(baseUrlSwitch, storedConfigAfterSwitch?.baseURL)
        assertEquals(projectTokenSwitch, storedConfigAfterSwitch?.projectToken)
        assertEquals(authorizationSwitch, storedConfigAfterSwitch?.authorization)
    }

    @Test
    fun `should initialize with correct project token`() {
        setupSendsay("Token asdf", "abcd-1234-fgh")
        assertEquals(Sendsay.isInitialized, true)
    }

    @Test
    fun `should initialize with correct project mapping`() {
        setupSendsay(
            "Token asdf",
            "abcd-1234-fgh",
            mapOf(
                EventType.TRACK_CUSTOMER to listOf(
                    SendsayProject(
                        baseUrl = "https://api.sendsay.com",
                        projectToken = "project-token",
                        authorization = "Token mock-auth"
                    )
                )
            )
        )
        assertEquals(Sendsay.isInitialized, true)
    }

    @Test
    fun `should throw error when initializing sdk with empty project token`() {
        expectedException.expect(InvalidConfigurationException::class.java)
        expectedException.expectMessage("""
            Project token provided is not valid. Project token cannot be empty string.
        """.trimIndent())
        setupSendsay("Basic asdf", "")
        assertEquals(Sendsay.isInitialized, false)
    }

    @Test
    fun `should throw error when initializing sdk with empty project token in mapping`() {
        expectedException.expect(InvalidConfigurationException::class.java)
        expectedException.expectMessage("""
            Project token provided is not valid. Project token cannot be empty string.
        """.trimIndent())
        setupSendsay(
            "Token asdf",
            "abcd-1234-fgh",
            mapOf(
                EventType.TRACK_CUSTOMER to listOf(
                    SendsayProject(
                        baseUrl = "https://api.sendsay.com",
                        projectToken = "",
                        authorization = "Token mock-auth"
                    )
                )
            )
        )
        assertEquals(Sendsay.isInitialized, false)
    }

    @Test
    fun `should throw error when initializing sdk with invalid project token`() {
        expectedException.expect(InvalidConfigurationException::class.java)
        expectedException.expectMessage("""
            Project token provided is not valid. Only alphanumeric symbols and dashes are allowed in project token.
        """.trimIndent())
        setupSendsay("Basic asdf", "invalid_token_value")
        assertEquals(Sendsay.isInitialized, false)
    }

    @Test
    fun `should throw error when initializing sdk with invalid project token in mapping`() {
        expectedException.expect(InvalidConfigurationException::class.java)
        expectedException.expectMessage("""
            Project mapping for event type TRACK_CUSTOMER is not valid. Project token provided is not valid. Only alphanumeric symbols and dashes are allowed in project token.
        """.trimIndent())
        setupSendsay(
            "Token asdf",
            "abcd-1234-fgh",
            mapOf(
                EventType.TRACK_CUSTOMER to listOf(
                    SendsayProject(
                        baseUrl = "https://api.sendsay.com",
                        projectToken = "invalid_token_value",
                        authorization = "Token mock-auth"
                    )
                )
            )
        )
        assertEquals(Sendsay.isInitialized, false)
    }

    @Test
    fun `should update sessionTimeout conf in local storage`() {
        // de-init to be able init SDK
        resetSendsay()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sdkConfig = SendsayConfiguration(
            projectToken = "project-token",
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com",
            sessionTimeout = 10.0
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, sdkConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(sdkConfig, storedConfig)
        assertNotNull(storedConfig)
        assertEquals(10.0, storedConfig.sessionTimeout)
        // update sessionTimeout via API
        Sendsay.sessionTimeout = 20.0
        val storedConfigAfterUpdate = SendsayConfigRepository.get(context)
        assertNotNull(storedConfigAfterUpdate)
        assertEquals(20.0, storedConfigAfterUpdate.sessionTimeout)
    }

    @Test
    fun `should update automaticSessionTracking conf in local storage`() {
        // de-init to be able init SDK
        resetSendsay()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sdkConfig = SendsayConfiguration(
            projectToken = "project-token",
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com",
            automaticSessionTracking = false
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, sdkConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(sdkConfig, storedConfig)
        assertNotNull(storedConfig)
        assertFalse(storedConfig.automaticSessionTracking)
        // update automaticSessionTracking via API
        Sendsay.isAutomaticSessionTracking = true
        val storedConfigAfterUpdate = SendsayConfigRepository.get(context)
        assertNotNull(storedConfigAfterUpdate)
        assertTrue(storedConfigAfterUpdate.automaticSessionTracking)
    }

    @Test
    fun `should update automaticPushNotification conf in local storage`() {
        // de-init to be able init SDK
        resetSendsay()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sdkConfig = SendsayConfiguration(
            projectToken = "project-token",
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com",
            automaticPushNotification = false
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, sdkConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(sdkConfig, storedConfig)
        assertNotNull(storedConfig)
        assertFalse(storedConfig.automaticPushNotification)
        // update automaticPushNotification via API
        Sendsay.isAutoPushNotification = true
        val storedConfigAfterUpdate = SendsayConfigRepository.get(context)
        assertNotNull(storedConfigAfterUpdate)
        assertTrue(storedConfigAfterUpdate.automaticPushNotification)
    }

    @Test
    fun `should update campaignTTL conf in local storage`() {
        // de-init to be able init SDK
        resetSendsay()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sdkConfig = SendsayConfiguration(
            projectToken = "project-token",
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com",
            campaignTTL = 10.0
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, sdkConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(sdkConfig, storedConfig)
        assertNotNull(storedConfig)
        assertEquals(10.0, storedConfig.campaignTTL)
        // update campaignTTL via API
        Sendsay.campaignTTL = 20.0
        val storedConfigAfterUpdate = SendsayConfigRepository.get(context)
        assertNotNull(storedConfigAfterUpdate)
        assertEquals(20.0, storedConfigAfterUpdate.campaignTTL)
    }

    @Test
    fun `should update defaultProperties conf in local storage`() {
        // de-init to be able init SDK
        resetSendsay()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sdkConfig = SendsayConfiguration(
            projectToken = "project-token",
            authorization = "Token mock-auth",
            baseURL = "https://api.sendsay.com",
            defaultProperties = hashMapOf("defTestProp" to "defTestVal")
        )
        Sendsay.flushMode = FlushMode.MANUAL
        Sendsay.init(context, sdkConfig)
        val storedConfig = SendsayConfigRepository.get(context)
        assertEquals(sdkConfig, storedConfig)
        assertNotNull(storedConfig)
        assertEquals("defTestVal", storedConfig.defaultProperties["defTestProp"])
        // update defaultProperties via API
        Sendsay.defaultProperties = hashMapOf("defTestPropUpdate" to "defTestValUpdate")
        val storedConfigAfterUpdate = SendsayConfigRepository.get(context)
        assertNotNull(storedConfigAfterUpdate)
        assertTrue(storedConfigAfterUpdate.automaticPushNotification)
        assertNull(storedConfigAfterUpdate.defaultProperties["defTestProp"])
        assertEquals("defTestValUpdate", storedConfigAfterUpdate.defaultProperties["defTestPropUpdate"])
    }
}
