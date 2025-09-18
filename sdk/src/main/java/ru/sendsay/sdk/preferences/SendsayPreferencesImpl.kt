package ru.sendsay.sdk.preferences

import android.content.Context
import android.preference.PreferenceManager
import ru.sendsay.sdk.util.Logger

internal class SendsayPreferencesImpl(
    context: Context,
    prefsName: String? = null
) : SendsayPreferences {

    private val sharedPreferences = prefsName?.let {
        context.getSharedPreferences(it, 0)
    } ?: PreferenceManager.getDefaultSharedPreferences(context)

    override fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun setLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    override fun getString(key: String, default: String): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    override fun getLong(key: String, default: Long): Long {
        return sharedPreferences.getLong(key, default)
    }

    override fun setDouble(key: String, value: Double) {
        Logger.d(this, "put double: $value")
        sharedPreferences.edit().putLong(key, value.toRawBits()).apply()
    }

    override fun getDouble(key: String, default: Double): Double {
        Logger.d(this, "get double: ${Double.fromBits(getLong(key, (-1.0).toRawBits()))}")
        return Double.fromBits(sharedPreferences.getLong(key, (-1.0).toRawBits()))
    }

    override fun remove(key: String): Boolean {
        sharedPreferences.edit().remove(key).apply()
        return true
    }
}
