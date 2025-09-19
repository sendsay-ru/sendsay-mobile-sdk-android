package com.sendsay.sdk.repository

import com.sendsay.sdk.models.CustomerIds
import com.sendsay.sdk.preferences.SendsayPreferences
import com.sendsay.sdk.util.Logger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class CustomerIdsRepositoryImpl(
    private val gson: Gson,
    private val uuidRepo: UniqueIdentifierRepository,
    private val prefs: SendsayPreferences
) : CustomerIdsRepository {

    companion object {
        private const val PREFS_CUSTOMERIDS = "SendsayCustomerIds"
    }

    override fun get(): CustomerIds {
        val uuid = uuidRepo.get()
        val json = prefs.getString(PREFS_CUSTOMERIDS, "{}")
        // customer ids used to be 'Any?'. They can still be in DB, let's convert them to 'String?'
        val type = object : TypeToken<HashMap<String, Any?>>() {}.type
        val ids = gson.fromJson<HashMap<String, Any?>>(json, type)
        return CustomerIds().apply {
            cookie = uuid
            externalIds = HashMap(ids.mapValues { it.value as? String })
        }
    }

    override fun set(customerIds: CustomerIds) {
        val json = gson.toJson(customerIds.externalIds)
        prefs.setString(PREFS_CUSTOMERIDS, json)
    }

    override fun clear() {
        Logger.d(this, "Clearing customer ids")
        prefs.remove(PREFS_CUSTOMERIDS)
    }
}
