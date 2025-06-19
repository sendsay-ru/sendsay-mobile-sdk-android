package com.sendsay.sdk.util

import android.net.Uri

class URLUtils {
    companion object {
        public fun areEqualAsURLs(url1: String?, url2: String?): Boolean {
            if (url1 == null && url2 == null) {
                return true
            }
            if (url1 == null || url2 == null) {
                return false
            }
            try {
                val parsed1 = Uri.parse(url1)
                val parsed2 = Uri.parse(url2)
                val path1 = parsed1.path?.removeSuffix("/")
                val path2 = parsed2.path?.removeSuffix("/")
                val query1 = parsed1.query
                val query2 = parsed2.query
                return parsed1.scheme == parsed2.scheme &&
                    parsed1.host == parsed2.host &&
                    path1 == path2 &&
                    query1 == query2
            } catch (e: Exception) {
                Logger.e(this, "Unable to compare urls $url1 vs $url2", e)
                return false
            }
        }
    }
}
