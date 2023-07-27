package com.example.firenews.authentication.linkedIn

import android.net.Uri
import android.util.Log
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.util.UUID


class LinkedInLoginHelper {

    companion object {
        //const val DISCOVERY_ENDPOINT = "https://www.linkedin.com/oauth"
        const val TAG = "Login Activity"
        const val SCOPES = "openid profile email"
        const val AUTHORIZATION_URL = "https://www.linkedin.com/oauth/v2/authorization"
        const val MY_REDIRECT_URI = "https://firenews-92f91.web.app/oauth2redirect"
        const val CLIENT_ID = "78gvimlnm6ejmt"
    }

    var callback: LinkedInLoginCallback? = null

    fun getIdToken(authCode: String) {
        val functions = Firebase.functions
        val data = hashMapOf("authCode" to authCode)
        functions.getHttpsCallable("getIdToken")
            .call(data)
            .addOnSuccessListener {
                val response = it.data.toString()
                Log.e(TAG,response)
                val json = JSONObject(response)
                callback?.onIdToken(json.getString("idToken"))
            }
            .addOnFailureListener {
                callback?.onLinkedInAuthException(it)
            }
    }

    /*fun exchangeAccessToken(authCode: String) {
        Log.i(TAG, "Exchanging access token")
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.linkedin.com")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        val service: LinkedinService = retrofit.create(LinkedinService::class.java)
        val call = service.exchangeAccessToken(
            authorizationCode = authCode,
            clientId = CLIENT_ID,
            clientSecret = MY_CLIENT_SECRET,
            redirectUri = MY_REDIRECT_URI
        )

        CoroutineScope(Dispatchers.Default).launch {
            val res = withContext(Dispatchers.IO) { call.execute() }
            if (res.isSuccessful) {
                val idToken = res.body()?.idToken
                if (idToken != null) {
                    callback?.onIdToken(idToken)
                }
            } else {
                Log.e(TAG, "${res.errorBody()}")
            }
        }

    }*/

    private fun buildUri(uniqueState: String): Uri {
        return Uri.parse(AUTHORIZATION_URL)
            .buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", MY_REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("state", uniqueState)
            .build()
    }

    fun getWebAuthConfig(): AuthWebViewContract.WebAuthConfiguration {
        val uniqueState = UUID.randomUUID().toString()
        val uri = buildUri(uniqueState)
        return AuthWebViewContract.WebAuthConfiguration(uri, MY_REDIRECT_URI, uniqueState)
    }


    interface LinkedInLoginCallback {
        fun onLinkedInAuthException(exception: Exception)
        fun onIdToken(idToken: String)
    }
}