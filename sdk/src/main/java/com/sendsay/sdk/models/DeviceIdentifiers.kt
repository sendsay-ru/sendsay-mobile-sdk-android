package com.sendsay.sdk.models

internal data class DeviceIdentifiers(
    val gaid: String? = null,
    val oaid: OaidResult = OaidResult.Unavailable.NOT_AVAILABLE_YET,
    val androidId: String? = null,
)

internal sealed interface GaidResult {
    data class Available(val id: String) : GaidResult
    object Unsupported : GaidResult
    object Unavailable : GaidResult
}

internal suspend fun resolveDeviceIdentifiers(
    fetchGaid: suspend () -> GaidResult,
    fetchOaid: suspend () -> OaidResult,
    fetchAndroidId: suspend () -> String?,
): DeviceIdentifiers = when (val gaid = fetchGaid()) {
    is GaidResult.Available -> DeviceIdentifiers(
        gaid = gaid.id,
        oaid = OaidResult.Unavailable.NOT_SELECTED,
    )
    GaidResult.Unavailable -> DeviceIdentifiers(
        oaid = OaidResult.Unavailable.NOT_SELECTED,
    )
    GaidResult.Unsupported -> {
        val oaid = fetchOaid()
        DeviceIdentifiers(
            oaid = oaid,
            // Only the absence of a provider allows a persistent identifier.
            androidId = if (oaid == OaidResult.Unavailable.UNSUPPORTED) {
                fetchAndroidId()
            } else {
                null
            },
        )
    }
}

internal fun String?.usableAdvertisingId(): String? = this?.trim()?.takeIf {
    it.any { character -> character != '0' && character != '-' }
}
