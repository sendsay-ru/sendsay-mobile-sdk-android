package com.sendsay.sdk.models

data class SendsayProject(
    val baseUrl: String,
    val projectToken: String,
    val authorization: String?,
    val inAppContentBlockPlaceholdersAutoLoad: List<String> = emptyList()
)
