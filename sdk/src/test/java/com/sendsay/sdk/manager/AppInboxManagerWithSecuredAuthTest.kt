package ru.sendsay.sdk.manager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import ru.sendsay.sdk.Sendsay
import ru.sendsay.sdk.models.CustomerIds
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.models.FlushMode
import ru.sendsay.sdk.repository.AppInboxCache
import ru.sendsay.sdk.repository.AppInboxCacheImpl
import ru.sendsay.sdk.repository.CustomerIdsRepository
import ru.sendsay.sdk.repository.DrawableCache
import ru.sendsay.sdk.services.AuthorizationProvider
import ru.sendsay.sdk.services.SendsayProjectFactory
import ru.sendsay.sdk.testutil.SendsaySDKTest
import ru.sendsay.sdk.testutil.waitForIt
import ru.sendsay.sdk.util.backgroundThreadDispatcher
import ru.sendsay.sdk.util.mainThreadDispatcher
import com.google.gson.Gson
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
internal class AppInboxManagerWithSecuredAuthTest : SendsaySDKTest() {

    private lateinit var appInboxManager: AppInboxManager
    private lateinit var appInboxCache: AppInboxCache
    private lateinit var customerIdsRepository: CustomerIdsRepository
    private lateinit var drawableCache: DrawableCache
    private lateinit var fetchManager: FetchManager

    @Before
    fun before() {
        fetchManager = mockk()
        every { fetchManager.fetchAppInbox(any(), any(), any(), any(), any()) } just Runs
        drawableCache = mockk()
        every { drawableCache.has(any()) } returns false
        every { drawableCache.preload(any(), any()) } just Runs
        every { drawableCache.clearExcept(any()) } just Runs
        customerIdsRepository = mockk()
        every { customerIdsRepository.get() } returns CustomerIds()
        appInboxCache = AppInboxCacheImpl(
            ApplicationProvider.getApplicationContext(), Gson()
        )
    }

    @Before
    fun overrideThreadBehaviour() {
        mainThreadDispatcher = CoroutineScope(Dispatchers.Main)
        backgroundThreadDispatcher = CoroutineScope(Dispatchers.Main)
    }

    @After
    fun restoreThreadBehaviour() {
        mainThreadDispatcher = CoroutineScope(Dispatchers.Main)
        backgroundThreadDispatcher = CoroutineScope(Dispatchers.Default)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should use basic auth`() {
        val projectWithAuth = slot<SendsayProject>()
        prepareAuth(basic = "mock-auth-basic")
        appInboxManager.fetchAppInbox { }
        verify(exactly = 1) {
            fetchManager.fetchAppInbox(capture(projectWithAuth), any(), any(), any(), any())
        }
        assertEquals("Token mock-auth-basic", projectWithAuth.captured.authorization)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should use auth from provider`() {
        val projectWithAuth = slot<SendsayProject>()
        TestSecureAuthProvider.token = "mock-auth-secured-by-provider"
        prepareAuth(provider = TestSecureAuthProvider::class)
        appInboxManager.fetchAppInbox { }
        verify(exactly = 1) {
            fetchManager.fetchAppInbox(capture(projectWithAuth), any(), any(), any(), any())
        }
        assertEquals("Bearer mock-auth-secured-by-provider", projectWithAuth.captured.authorization)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should prioritize auth from provider than basic`() {
        val projectWithAuth = slot<SendsayProject>()
        TestSecureAuthProvider.token = "mock-auth-secured-by-provider"
        prepareAuth(
            basic = "mock-auth-basic",
            provider = TestSecureAuthProvider::class
        )
        appInboxManager.fetchAppInbox { }
        verify(exactly = 1) {
            fetchManager.fetchAppInbox(capture(projectWithAuth), any(), any(), any(), any())
        }
        assertEquals("Bearer mock-auth-secured-by-provider", projectWithAuth.captured.authorization)
    }

    @Test
    @LooperMode(LooperMode.Mode.LEGACY)
    fun `should failed with defined auth provider without token`() {
        TestSecureAuthProvider.token = null
        prepareAuth(
            provider = TestSecureAuthProvider::class
        )
        waitForIt(2000) { done ->
            appInboxManager.fetchAppInbox {
                assertNull(it)
                done()
            }
        }
        verify(exactly = 0) {
            fetchManager.fetchAppInbox(any(), any(), any(), any(), any())
        }
    }

    private fun prepareAuth(
        basic: String? = null,
        provider: KClass<out AuthorizationProvider>? = null
    ) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Sendsay.flushMode = FlushMode.MANUAL
        val configuration = SendsayConfiguration(
            baseURL = "https://base-url.com",
            projectToken = "project_token",
            authorization = "Token " + (basic ?: "mock-auth"),
            advancedAuthEnabled = provider != null
        )
        val projectFactory = object : SendsayProjectFactory(context, configuration) {
            override fun readAuthorizationProviderName(context: Context): String? {
                return provider?.qualifiedName
            }
        }
        appInboxManager = AppInboxManagerImpl(
            fetchManager = fetchManager,
            drawableCache = drawableCache,
            customerIdsRepository = customerIdsRepository,
            appInboxCache = appInboxCache,
            projectFactory = projectFactory
        )
    }
}

class TestSecureAuthProvider : AuthorizationProvider {
    companion object {
        var token: String? = null
    }
    override fun getAuthorizationToken(): String? {
        return token
    }
}
