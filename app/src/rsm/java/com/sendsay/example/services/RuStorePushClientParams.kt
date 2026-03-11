package com.sendsay.example.services

import android.content.Context
import ru.rustore.sdk.pushclient.provider.AbstractRuStorePushClientParams

class RuStorePushClientParams(context: Context) : AbstractRuStorePushClientParams(context) {

    override fun getTestModeEnabled(): Boolean = true

}