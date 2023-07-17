package com.example.firenews

import android.app.Application
import android.content.Context
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import java.lang.String


class FireNewsApplication : Application() {

    companion object {
        const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET";
        const val PREFS_TEMPLATE = "com.google.firebase.appcheck.debug.store.%s"
        const val TOKEN="D763789E-CB60-4AB0-BE35-4001BD33EF87"
    }

    override fun onCreate() {
        super.onCreate()
        AppEventsLogger.activateApp(this)
        Firebase.initialize(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        if (BuildConfig.DEBUG) {
            val persistenceKey = FirebaseApp.getInstance().persistenceKey
            val prefsName = String.format(PREFS_TEMPLATE, persistenceKey)
            val sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(DEBUG_SECRET_KEY, TOKEN).commit()
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
                //MyDebugAppCheckProviderFactory()

            )

        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }
    }
}