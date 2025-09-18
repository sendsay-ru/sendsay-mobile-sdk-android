package ru.sendsay.sdk.models

import com.google.gson.annotations.SerializedName

data class ConfigItem(
    @SerializedName("isInAppMessagesEnabled")
    var isInAppMessagesEnabled: Boolean? = false,
    @SerializedName("isInAppCBEnabled")
    var isInAppCBEnabled: Boolean? = false,
    @SerializedName("isAppInboxEnabled")
    var isAppInboxEnabled: Boolean? = false,
)