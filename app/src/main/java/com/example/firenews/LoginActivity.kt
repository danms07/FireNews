package com.example.firenews

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firenews.databinding.EmailDialogBinding
import com.example.firenews.databinding.LoginBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.ktx.actionCodeSettings


class LoginActivity : AppCompatActivity(), OnDialogInteractionListener {
    private lateinit var loginBinding: LoginBinding
    private lateinit var auth: FirebaseAuth
    private val callbackManager = CallbackManager.Factory.create()
    private val emailDialog: AlertDialog by lazy {
        buildDialog()
    }

    private fun buildDialog(): AlertDialog {
        val emailBinding = EmailDialogBinding.inflate(layoutInflater)
        emailBinding.listener = this
        return AlertDialog.Builder(this).setView(emailBinding.root).setCancelable(true).create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        loginBinding = LoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)
        loginBinding.googleSignInButton.setOnClickListener { googleSignIn() }
        setupFbSignIn()
        loginBinding.anonLoginBtn.setOnClickListener { anonSignIn() }
        //loginBinding.mailLoginBtn.setOnClickListener { mailSignIn() }


    }

    override fun onResume() {
        super.onResume()
        val intent = intent
        val emailLink = intent.data.toString()

// Confirm the link is a sign-in with email link.
        if (auth.isSignInWithEmailLink(emailLink)) {
            // Retrieve this from wherever you stored it
            val email = retrieveEmail()

            // The client SDK will parse the code from the link for you.
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully signed in with email link!")
                        val result = task.result
                        jumpToMain(result.user)
                    } else {
                        Firebase.crashlytics.recordException(java.lang.Exception(task.exception))
                        Log.e(TAG, "Error signing in with email link", task.exception)
                    }
                }
        }
    }

    private fun mailSignIn() {
        displayEmailDialog()
    }

    private fun displayEmailDialog() {
        emailDialog.show()
    }

    private fun anonSignIn() {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInAnonymously:success")
                    val user = auth.currentUser
                    jumpToMain(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setupFbSignIn() {
        loginBinding.fbLoginButton.setReadPermissions("email", "public_profile")
        loginBinding.fbLoginButton.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {

                handleFirebaseFacebookLogin(result.accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {
                Log.e(TAG, error.message ?: "")
                Firebase.crashlytics.recordException(error)
            }

        })
    }

    private fun handleFirebaseFacebookLogin(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                jumpToMain(user)
            }
            else{
                AlertDialog.Builder(this)
                    .setMessage(task.exception?.toString())
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok){dialog, _ ->dialog.dismiss()}
                    .create()
                    .show()
            }
        }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.web_client_id))
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(googleSignInIntent, GOOGLE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_REQUEST) {
            val googleSignInAccountTask: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = googleSignInAccountTask.result
                val token = account.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success")
                            val user = auth.currentUser
                            jumpToMain(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                            Firebase.crashlytics.recordException(java.lang.Exception(task.exception))

                        }
                    }
            } catch (e: ApiException) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Firebase.crashlytics.recordException(e)
                Log.w(TAG, "signInResult:failed code=" + e.statusCode)

            }
        }
    }

    private fun jumpToMain(user: FirebaseUser?) {
        val welcome = getString(R.string.welcome)
        Toast.makeText(this, "$welcome ${user?.displayName ?: ""}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }



    override fun onEmail(email: String) {
        emailDialog.dismiss()
        if (validateEmail(email)) {
            storeEmail(email)
            setupEmailAuthFlow(email)
        } else {
            showToast(getString(R.string.invalid_email))
            displayEmailDialog()
        }
    }

    private fun storeEmail(email: String) {
        val editor=getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).edit()
        editor.putString(EMAIL,email)
        editor.apply()
    }

    private fun retrieveEmail():String{
        val preferences=getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE)
        val email= preferences.getString(EMAIL,"")?:""
        /*val editor=preferences.edit()
        editor.putString(EMAIL,"")
        editor.apply()*/
        return email
    }

    private fun setupEmailAuthFlow(email: String) {
        val actionCodeSettings = actionCodeSettings {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://firenews-92f91.web.app/"
            // This must be true
            handleCodeInApp = true
            setAndroidPackageName(
                "com.example.firenews",
                true, /* installIfNotAvailable */
                "1" /* minimumVersion */)
        }

        Firebase.auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(getString(R.string.sending_email))
                    Log.d(TAG, "Email sent.")
                }
            }
    }

    private fun validateEmail(email: String): Boolean {
        val pattern = Regex("^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})\$")
        return pattern.matches(email)
    }

    override fun onCancel() {
        emailDialog.dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "LoginActivity"
        const val GOOGLE_REQUEST = 1000
        const val EMAIL="EMAIL"
        const val PREFERENCES="login"

    }
}