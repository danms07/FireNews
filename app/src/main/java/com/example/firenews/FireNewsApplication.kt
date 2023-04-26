package com.example.firenews

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class FireNewsApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        AppEventsLogger.activateApp(this)
    }
}