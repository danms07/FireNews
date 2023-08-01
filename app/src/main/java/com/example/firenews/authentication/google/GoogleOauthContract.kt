package com.example.firenews.authentication.google

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class GoogleOauthContract:ActivityResultContract<AuthorizationRequest,Intent?>() {
    override fun createIntent(context: Context, input: AuthorizationRequest): Intent {
        val authService = AuthorizationService(context)
        return authService.getAuthorizationRequestIntent(input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent?{
        return intent
    }
}