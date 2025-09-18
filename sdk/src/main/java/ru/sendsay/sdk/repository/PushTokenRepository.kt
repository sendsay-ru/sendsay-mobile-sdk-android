package ru.sendsay.sdk.repository

import android.content.Context
import ru.sendsay.sdk.preferences.SendsayPreferencesImpl
import ru.sendsay.sdk.util.TokenType
import ru.sendsay.sdk.services.OnIntegrationStoppedCallback

internal interface PushTokenRepository : OnIntegrationStoppedCallback {
    fun get(): String?
    fun getLastTrackDateInMilliseconds(): Long?
    fun setTrackedToken(
        token: String,
        lastTrackDateInMilliseconds: Long,
        tokenType: TokenType,
        permissionGranted: Boolean
    )
    fun setUntrackedToken(token: String, tokenType: TokenType, permissionGranted: Boolean)
    fun clear(): Boolean
    fun getLastTokenType(): TokenType
    fun getLastPermissionFlag(): Boolean
    override fun onIntegrationStopped()
}

internal object PushTokenRepositoryProvider {
    fun get(context: Context): PushTokenRepositoryImpl {
        return PushTokenRepositoryImpl(SendsayPreferencesImpl(context, "SENDSAY_PUSH_TOKEN"))
    }
}
