package ru.sendsay.sdk.services

interface AuthorizationProvider {
    fun getAuthorizationToken(): String?
}
