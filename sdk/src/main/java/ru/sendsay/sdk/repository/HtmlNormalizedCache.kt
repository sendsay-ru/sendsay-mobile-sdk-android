package ru.sendsay.sdk.repository

import ru.sendsay.sdk.util.HtmlNormalizer.NormalizedResult

internal interface HtmlNormalizedCache {
    fun get(key: String, htmlOrigin: String): NormalizedResult?
    fun set(key: String, htmlOrigin: String, normalizedResult: NormalizedResult)
    fun remove(key: String)
    fun clearAll()
}
