package com.example.firenews.authentication.google

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task

class NativeGoogleSignInContract: ActivityResultContract <GoogleSignInOptions, Task<GoogleSignInAccount> >(){
    override fun createIntent(context: Context, input: GoogleSignInOptions): Intent {
        val mGoogleSignInClient = GoogleSignIn.getClient(context, input)
        return mGoogleSignInClient.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount> {
        return GoogleSignIn.getSignedInAccountFromIntent(intent)
    }
}