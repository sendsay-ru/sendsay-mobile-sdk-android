package com.sendsay.sdk.preferences

internal interface SendsayPreferences {
    fun setString(key: String, value: String)
    fun setBoolean(key: String, value: Boolean)
    fun setLong(key: String, value: Long)
    fun setDouble(key: String, value: Double)

    fun getString(key: String, default: String): String
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getLong(key: String, default: Long): Long
    fun getDouble(key: String, default: Double): Double

    fun remove(key: String): Boolean
}
