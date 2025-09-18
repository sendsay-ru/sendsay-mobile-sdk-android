package ru.sendsay.sdk.manager

internal interface PushNotificationSelfCheckManager {
    fun start()
    fun selfCheckPushReceived()
}
