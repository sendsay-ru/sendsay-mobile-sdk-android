package com.sendsay.example

import android.app.Application
import android.view.LayoutInflater
import android.widget.TextView
import com.sendsay.example.managers.RegisteredIdManager
import com.sendsay.example.view.fragments.TrackFragment.Companion.mockItems
import com.sendsay.sdk.BuildConfig

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
