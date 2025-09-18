package ru.sendsay.example

import android.app.Application
import ru.sendsay.example.managers.RegisteredIdManager

class App : Application() {
    companion object {
        lateinit var instance: App
    }

    lateinit var registeredIdManager: RegisteredIdManager

    override fun onCreate() {
        super.onCreate()

        // Assign our instance to this
        instance = this

        // Create our RegisteredIDManager to get the registered ID.
        registeredIdManager = RegisteredIdManager(this)
    }
}
