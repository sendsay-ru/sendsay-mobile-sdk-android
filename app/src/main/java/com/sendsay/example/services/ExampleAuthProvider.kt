package com.sendsay.example.services

import com.sendsay.example.managers.CustomerTokenStorage
import com.sendsay.sdk.services.AuthorizationProvider

class ExampleAuthProvider : AuthorizationProvider {

    override fun getAuthorizationToken(): String? {
        // Receive and return JWT token here.
        return CustomerTokenStorage.INSTANCE.retrieveJwtToken()
        // NULL as returned value will be handled by SDK as 'no value' and leads to Error
    }
}
