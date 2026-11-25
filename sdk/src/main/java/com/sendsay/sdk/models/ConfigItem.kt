package com.sendsay.sdk.models

import com.google.gson.annotations.SerializedName

data class ConfigItem(
    @SerializedName("isInAppMessagesEnabled")
    var isInAppMessagesEnabled: Boolean? = null,
    @SerializedName("isInAppCBEnabled")
    var isInAppCBEnabled: Boolean? = null,
    @SerializedName("isAppInboxEnabled")
    var isAppInboxEnabled: Boolean? = null,
    @SerializedName("isADTrackEnabled")
    var isADTrackEnabled: Boolean? = null,
)
