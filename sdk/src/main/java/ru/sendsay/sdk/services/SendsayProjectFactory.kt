package ru.sendsay.sdk.services

import android.content.Context
import android.content.pm.PackageManager
import ru.sendsay.sdk.exceptions.InvalidConfigurationException
import ru.sendsay.sdk.models.SendsayConfiguration
import ru.sendsay.sdk.models.SendsayConfiguration.Companion.BEARER_AUTH_PREFIX
import ru.sendsay.sdk.models.SendsayProject
import ru.sendsay.sdk.util.Logger

internal open class SendsayProjectFactory(
    private val context: Context,
    sendsayConfiguration: SendsayConfiguration
) {

    private lateinit var configuration: SendsayConfiguration
    private var customAuthProvider: AuthorizationProvider? = null

    init {
        reset(sendsayConfiguration)
    }

    val mainSendsayProject
        get() = SendsayProject(
            configuration.baseURL,
            configuration.projectToken,
            configuration.authorization,
            configuration.inAppContentBlockPlaceholdersAutoLoad
        )

    /**
     * Returns SendsayProject that:
     *  - contains CustomerID auth token if AuthProvider is registered
     *  - contains Api auth token otherwise
     *
     *  !!! Access it in background thread due to possibility of fetching of Customer Token value
     */
    val mutualSendsayProject: SendsayProject
        get() {
            val authProvider = customAuthProvider
            if (authProvider == null) {
                return mainSendsayProject
            }
            var authToken = authProvider.getAuthorizationToken()
            if (authToken?.isNotBlank() == true && !authToken.startsWith(BEARER_AUTH_PREFIX)) {
                authToken = BEARER_AUTH_PREFIX + authToken
            }
            return SendsayProject(
                configuration.baseURL,
                configuration.projectToken,
                authToken,
                configuration.inAppContentBlockPlaceholdersAutoLoad
            )
        }

    fun reset(newConfiguration: SendsayConfiguration) {
        configuration = newConfiguration
        if (configuration.advancedAuthEnabled) {
            customAuthProvider = tryLoadAuthorizationProvider(context)
            if (customAuthProvider == null) {
                Logger.e(this, "Advanced auth has been enabled but provider has not been found")
                throw InvalidConfigurationException("""
                Customer token authorization provider is enabled but cannot be found.
                Please check your configuration against https://github.com/sendsay/sendsay-android-sdk/blob/main/Documentation/AUTHORIZATION.md
                """.trimIndent()
                )
            }
        }
    }

    internal fun tryLoadAuthorizationProvider(context: Context): AuthorizationProvider? {
        val customProviderClassname = readAuthorizationProviderName(context)
        if (customProviderClassname == null) {
            // valid exit, no SendsayAuthProvider in metadata
            Logger.i(this, "CustomerID auth provider is not registered")
            return null
        }
        val customProviderClass = try {
            Class.forName(customProviderClassname)
        } catch (e: ClassNotFoundException) {
            Logger.e(this, "Registered $customProviderClassname class has not been found", e)
            throw InvalidConfigurationException("""
                Customer token authorization provider is registered but cannot be found.
                Please check your configuration against https://github.com/sendsay/sendsay-android-sdk/blob/main/Documentation/AUTHORIZATION.md
                """.trimIndent()
            )
        }
        val customProviderInstance = customProviderClass.newInstance()
        if (customProviderInstance is AuthorizationProvider) {
            return customProviderInstance
        }
        Logger.e(this, "Registered $customProviderClassname class has to implement" +
            "${AuthorizationProvider::class.qualifiedName}")
        throw InvalidConfigurationException("""
                Customer token authorization provider is registered but mismatches implementation requirements.
                Please check your configuration against https://github.com/sendsay/sendsay-android-sdk/blob/main/Documentation/AUTHORIZATION.md
                """.trimIndent()
        )
    }

    internal open fun readAuthorizationProviderName(context: Context): String? {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        if (appInfo.metaData == null) {
            // valid exit, no metadata
            return null
        }
        return appInfo.metaData["SendsayAuthProvider"] as String?
    }
}
