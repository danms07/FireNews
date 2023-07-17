package com.example.firenews.appcheck

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckProvider
import com.google.firebase.appcheck.AppCheckProviderFactory

class MyDebugAppCheckProviderFactory : AppCheckProviderFactory {
    override fun create(firebaseApp: FirebaseApp): AppCheckProvider {
        // Create and return an AppCheckProvider object.
        return DebugAppCheckProvider(firebaseApp)

    }
}