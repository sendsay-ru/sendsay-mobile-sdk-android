package com.sendsay.sdk.services

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.sendsay.sdk.Sendsay
import com.sendsay.sdk.manager.EventManagerImpl
import com.sendsay.sdk.mockkConstructorFix
import com.sendsay.sdk.models.SendsayConfiguration
import com.sendsay.sdk.models.FlushMode
import com.sendsay.sdk.preferences.SendsayPreferencesImpl
import com.sendsay.sdk.receiver.AppUpdateReceiver
import com.sendsay.sdk.repository.SendsayConfigRepository
import com.sendsay.sdk.repository.PushTokenRepositoryProvider
import com.sendsay.sdk.testutil.SendsaySDKTest
import io.mockk.every
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class TokenMigrationTest() : SendsaySDKTest() {

    private val token_repo_key = "SendsayFirebaseToken"
    private val token_1 = "ABCD_token_1"
    private val token_repo_name = "SENDSAY_PUSH_TOKEN"

    lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockkConstructorFix(EventManagerImpl::class) {
            every { anyConstructed<EventManagerImpl>().addEventToQueue(any(), any(), any()) }
        }
        Sendsay.flushMode = FlushMode.MANUAL
    }

    @Test
    fun `should have no token after install`() {
        initSendsay()
        assertNull(PushTokenRepositoryProvider.get(context).get())
    }

    @Test
    fun `should migrate token after update`() {
        val obsoleteStorage = SendsayPreferencesImpl(context)
        obsoleteStorage.setString(token_repo_key, token_1)
        initSendsay()
        simulateAppUpdate()
        assertEquals(token_1, PushTokenRepositoryProvider.get(context).get())
    }

    @Test
    fun `should not migrate after update`() {
        initSendsay()
        simulateAppUpdate()
        assertNull(PushTokenRepositoryProvider.get(context).get())
    }

    private fun simulateAppUpdate() {
        val updateIntent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED)
        AppUpdateReceiver().onReceive(context, updateIntent)
    }

    @Test
    fun `should have no token after reinstall`() {
        SendsayConfigRepository.set(context, getSendsayConfiguration())
        Sendsay.handleNewToken(context, token_1)
        assertEquals(token_1, PushTokenRepositoryProvider.get(context).get())
        simulateUninstall()
        initSendsay()
        assertNull(PushTokenRepositoryProvider.get(context).get())
    }

    private fun simulateUninstall() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        context.getSharedPreferences(token_repo_name, 0).edit().clear().commit()
    }

    @Test
    fun `should have token after future update`() {
        SendsayConfigRepository.set(context, getSendsayConfiguration())
        Sendsay.handleNewToken(context, token_1)
        assertEquals(token_1, PushTokenRepositoryProvider.get(context).get())
        // simulate future updates == app will use only new storage
        simulateAppUpdate()
        assertEquals(token_1, PushTokenRepositoryProvider.get(context).get())
    }

    private fun initSendsay() {
        Sendsay.init(context, getSendsayConfiguration())
    }

    private fun getSendsayConfiguration(): SendsayConfiguration {
        val configuration = SendsayConfiguration(projectToken = "mock-token")
        configuration.automaticPushNotification = true
        return configuration
    }
}
