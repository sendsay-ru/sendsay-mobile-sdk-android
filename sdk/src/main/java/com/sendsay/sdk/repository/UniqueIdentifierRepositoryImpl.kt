package com.sendsay.sdk.repository

import com.sendsay.sdk.preferences.SendsayPreferences
import java.util.UUID

internal class UniqueIdentifierRepositoryImpl(
    private val preferences: SendsayPreferences
) : UniqueIdentifierRepository {

    companion object {
        internal val key = "SendsayUniqueIdentifierToken"
    }

    override fun get(): String {
        var token = preferences.getString(key, "")

        if (token.isEmpty()) {
            token = UUID.randomUUID().toString()
            preferences.setString(key, token)
        }

        return token
    }

    override fun clear(): Boolean {
        return preferences.remove(key)
    }
}
