package com.sendsay.sdk.services

interface AuthorizationProvider {
    fun getAuthorizationToken(): String?
}
