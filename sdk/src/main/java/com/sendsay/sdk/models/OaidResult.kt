package com.sendsay.sdk.models

/** The result of an OAID request. */
sealed interface OaidResult {

    /** An OAID returned by the device provider. */
    data class Available(val id: String) : OaidResult

    /** The reason an OAID is unavailable. */
    enum class Unavailable : OaidResult {
        NOT_AVAILABLE_YET,
        DISABLED_BY_CONFIG,
        LIMITED_BY_USER,
        UNSUPPORTED,

        /** No usable ID or explicit tracking restriction was returned. */
        EMPTY_RESPONSE,

        TIMEOUT,
        FETCH_FAILED,

        /** The automatic selection did not request the OAID provider. */
        NOT_SELECTED,
    }

    companion object {
        internal fun fromProvider(
            id: String?,
            isLimitAdTrackingEnabled: Boolean? = null,
        ): OaidResult {
            if (isLimitAdTrackingEnabled == true) {
                return Unavailable.LIMITED_BY_USER
            }

            val normalizedId = id.usableAdvertisingId()
                ?: return Unavailable.EMPTY_RESPONSE

            return Available(normalizedId)
        }
    }
}
