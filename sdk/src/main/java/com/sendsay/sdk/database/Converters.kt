package com.sendsay.sdk.database

import androidx.room.TypeConverter
import com.google.gson.reflect.TypeToken
import com.sendsay.sdk.models.Route
import com.sendsay.sdk.models.SendsayProject
import com.sendsay.sdk.models.TrackSSECData
import com.sendsay.sdk.util.Logger
import com.sendsay.sdk.util.SendsayGson
import com.sendsay.sdk.util.fromJson

object Converters {

    private val separator = "§§§§§"

    @TypeConverter
    @JvmStatic
    fun fromRoute(value: Route?): String {
        if (value == null) return ""
        return value.name
    }

    @TypeConverter
    @JvmStatic
    fun toRoute(value: String): Route? {
        if (value.isEmpty()) return null
        return Route.valueOf(value)
    }

    @TypeConverter
    @JvmStatic
    fun fromProject(value: SendsayProject?): String {
        if (value == null) return ""
        return value.projectToken + separator + value.authorization + separator + value.baseUrl
    }

    @TypeConverter
    @JvmStatic
    fun toProject(value: String): SendsayProject? {
        if (value.isEmpty()) return null
        val parts = value.split(separator)
        return if (parts.size < 3) {
            null
        } else {
            val result = SendsayProject(
                projectToken = parts[0],
                authorization = parts[1],
                baseUrl = parts[2],
                inAppContentBlockPlaceholdersAutoLoad = toStringList(parts.getOrNull(3))
            )
            return result
        }
    }

    @TypeConverter
    @JvmStatic
    fun toStringList(source: String?): List<String> {
        if (source.isNullOrBlank()) {
            return emptyList()
        }
        try {
            return SendsayGson.instance.fromJson<List<String>>(source)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to deserialize the list", ex)
        }
        return emptyList()
    }

    @TypeConverter
    @JvmStatic
    fun fromStringList(data: List<String>?): String? {
        if (data == null) return ""
        try {
            return SendsayGson.instance.toJson(data)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to serialize the list", ex)
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun toAnyMap(value: String?): HashMap<String, Any>? {
        if (value == null || value.isEmpty()) return null
        try {
            return SendsayGson.instance.fromJson<HashMap<String, Any>>(value)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to deserialize the map", ex)
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun fromAnyMap(data: HashMap<String, Any>?): String? {
        if (data == null) return ""
        try {
            return SendsayGson.instance.toJson(data)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to serialize the map", ex)
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun toStringMap(value: String?): HashMap<String, String>? {
        if (value == null || value.isEmpty()) return null
        try {
            return SendsayGson.instance.fromJson<HashMap<String, String>>(value)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to deserialize the map", ex)
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun fromStringMap(data: HashMap<String, String>?): String? {
        if (data == null) return ""
        try {
            return SendsayGson.instance.toJson(data)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to serialize the map", ex)
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun toOptionalStringMap(value: String?): HashMap<String, String?>? {
        if (value == null || value.isEmpty()) return null
        try {
            return SendsayGson.instance.fromJson<HashMap<String, String?>>(value)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to deserialize the map", ex)
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    fun fromOptionalStringMap(data: HashMap<String, String?>?): String? {
        if (data == null) return ""
        try {
            return SendsayGson.instance.toJson(data)
        } catch (ex: Exception) {
            Logger.e(this, ex.message ?: "Unable to serialize the map", ex)
        }
        return null
    }
}
