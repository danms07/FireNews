package com.example.firenews.authentication


import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firenews.MainActivity
import com.example.firenews.R
import com.example.firenews.authentication.google.GoogleOauthContract
import com.example.firenews.authentication.google.NativeGoogleSignInContract
import com.example.firenews.authentication.linkedIn.AuthWebViewContract
import com.example.firenews.authentication.linkedIn.LinkedInLoginHelper
import com.example.firenews.databinding.ActivityLoginBinding
import com.example.firenews.databinding.VerificationCodeInputBinding
import com.example.firenews.authentication.input.DialogContent
import com.example.firenews.authentication.input.InputDialogBuilder
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity(),
    OnCodeDialogInteractionListener,
    LinkedInLoginHelper.LinkedInLoginCallback {

    companion object {
        const val TAG = "LoginActivity"
        const val EMAIL = "email"
        const val PROFILE = "public_profile"
        const val OIDC_LINKEDIN_ID = "oidc.linkedin"

    }

    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var linkedInLoginHelper: LinkedInLoginHelper? = null
    private var facebookCallbackManager = CallbackManager.Factory.create()
    private val linkedInLoginLauncher = registerForActivityResult(AuthWebViewContract()) {
        onLinkedInAuthCode(it)
    }
    private val fbLauncher = registerForActivityResult(
        LoginManager.getInstance().createLogInActivityResultContract(facebookCallbackManager)
    ) {

    }

    private val nativeGoogleSignInLauncher =
        registerForActivityResult(NativeGoogleSignInContract()) {
            onNativeGoogleSignInResult(it)
        }

    private val googleOauthLauncher = registerForActivityResult(GoogleOauthContract()) {
        onGoogleOauthResult(it)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)
        loginBinding.googleLoginButton.setOnClickListener {
            val availability=GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            if(availability==ConnectionResult.SUCCESS){
                googleSignIn()
            }else googleOauthLogin()
        }
        loginBinding.fbLoginButton.setOnClickListener { loginWithFacebook() }
        loginBinding.anonLoginButton.setOnClickListener { anonSignIn() }
        loginBinding.phoneLoginButton.setOnClickListener { phonePickUp() }
        loginBinding.emailLoginButton.setOnClickListener {
            val email = loginBinding.emailInputEdittext.text.toString()
            val password = loginBinding.passwordInputEditText.text.toString()
            emailPassLogin(email, password)
        }
        loginBinding.forgotPasswordOptionTextView.setOnClickListener { mailPickUp() }
        loginBinding.createAccountOptionTextView.setOnClickListener { goToSignUp() }
        loginBinding.linkedInLogin.setOnClickListener { startLinkedInFlow() }
    }

    private fun mailPickUp() {
        InputDialogBuilder(this, DialogContent.EmailContent())
            .setOnDataInputListener { input -> onEmailInput(input) }
            .build()
            .show()
    }

    private fun onEmailInput(email: String) {
        if (validateEmail(email)) {
            sendPasswordResetEmail(email)
        } else {
            showToast(getString(R.string.invalid_email))
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                showToast(getString(R.string.sending_email))
            }
            .addOnFailureListener {
                Log.e(TAG, it.stackTraceToString())
            }
    }

    private fun emailPassLogin(email: String, password: String) {
        if (validateEmail(email)) {
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val user = it.user
                    jumpToMain(user)
                }
        }
    }


    private fun goToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun phonePickUp() {
        InputDialogBuilder(this, DialogContent.PhoneNumberContent())
            .setOnDataInputListener { input -> onPhoneNumber(input) }
            .build()
            .show()
    }

    private fun onPhoneNumber(phoneNumber: String) {
        if (validatePhoneNumber(phoneNumber)) {
            phoneSignIn("+52$phoneNumber")
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.length == 10
    }

    private fun phoneSignIn(phoneNumber: String) {

        //FirebaseAuth.getInstance().firebaseAuthSettings.forceRecaptchaFlowForTesting(true)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
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
                    val exception = task.exception
                    if (exception != null) {
                        displayErrorAlert(exception.message.toString())
                    }

                }
            }
    }

    private fun loginWithFacebook() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        if (isLoggedIn && accessToken != null) {
            createFacebookCredential(accessToken)
        } else {
            setupFbSignIn()
        }
    }

    private fun setupFbSignIn() {
        LoginManager.getInstance().registerCallback(facebookCallbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    createFacebookCredential(result.accessToken)
                }

                override fun onCancel() {
                    Log.e(TAG, "On cancel")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, error.message.toString())
                    displayErrorAlert(error.message.toString())
                }
            })
        fbLauncher.launch(listOf(EMAIL, PROFILE))
    }

    fun createFacebookCredential(accessToken: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        signInWithAuthCredential(credential)
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.web_client_id))
            .build()

        nativeGoogleSignInLauncher.launch(gso)
    }


    private fun onNativeGoogleSignInResult(googleSignInAccountTask: Task<GoogleSignInAccount>?) {
        if (googleSignInAccountTask != null) {
            try {
                val account = googleSignInAccountTask.result
                val token = account.idToken
                createGoogleCredential(token)
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

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            val simOperatorName = telephonyManager.simOperatorName
            Firebase.crashlytics.setCustomKey("Carrier", simOperatorName)
            Firebase.crashlytics.recordException(e)
            Log.e(TAG, e.toString())
            AlertDialog.Builder(this@LoginActivity)
                .setTitle("Error")
                .setMessage(e.message.toString())
                .create()
                .show()
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    // Invalid request
                }

                is FirebaseTooManyRequestsException -> {
                    // The SMS quota for the project has been exceeded
                }

                is FirebaseAuthMissingActivityForRecaptchaException -> {
                    // reCAPTCHA verification attempted with null Activity
                }
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")
            buildVerificationCodeInputDialog(verificationId).show()
            //otpCodePickUp(verificationId)
            // Save verification ID and resending token so we can use them later
            //storedVerificationId = verificationId
            //resendToken = token
        }
    }

    private fun buildVerificationCodeInputDialog(verificationId: String): AlertDialog {
        val codeBinding = VerificationCodeInputBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(codeBinding.root)
            .setCancelable(false)
            .create()
        codeBinding.apply {
            id = verificationId
            listener = this@LoginActivity
            dialogInterface = dialog
        }
        return dialog
    }

    private fun signInWithAuthCredential(credential: AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    jumpToMain(user)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        displayErrorAlert(exception.message.toString())
                    }
                    // Update UI
                }
            }
    }

    private fun displayErrorAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setMessage(message)
            .setNeutralButton(R.string.ok) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    private fun validateEmail(email: String): Boolean {
        val pattern = Regex("^([a-zA-Z0-9_\\-.]+)@([a-zA-Z0-9_\\-.]+)\\.([a-zA-Z]{2,5})\$")
        return pattern.matches(email)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    override fun onCodeInput(
        verificationId: String,
        verificationCode: String,
        dialogInterface: DialogInterface
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
        signInWithAuthCredential(credential)
    }

    override fun onPhoneVerificationCancelled() {

    }


    private fun startLinkedInFlow() {
        linkedInLoginHelper = LinkedInLoginHelper().apply {
            callback = this@LoginActivity
            //createAuthRequest()
        }

        linkedInLoginHelper?.getWebAuthConfig().let {
            linkedInLoginLauncher.launch(it)
        }

    }

    private fun onLinkedInAuthCode(authCode: String) {
        Log.e(TAG, "Auth code received: $authCode")
        //linkedInLoginHelper?.exchangeAccessToken(authCode)
        linkedInLoginHelper?.getIdToken(authCode)
    }

    override fun onLinkedInAuthException(exception: Exception) {
        Log.e(TAG, exception.stackTraceToString())
    }

    override fun onIdToken(idToken: String) {
        auth.signInWithCustomToken(idToken)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCustomToken:success")
                    val user = auth.currentUser
                    jumpToMain(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun tryLinkedInAsOIDC() {
        val providerBuilder = OAuthProvider.newBuilder(OIDC_LINKEDIN_ID)
        providerBuilder.scopes = listOf("openid", "profile", "email")
        auth.startActivityForSignInWithProvider(this, providerBuilder.build())
            .addOnSuccessListener {
                // User is signed in.
                // IdP data available in
                // authResult.getAdditionalUserInfo().getProfile().
                // The OAuth access token can also be retrieved:
                // ((OAuthCredential)authResult.getCredential()).getAccessToken().
                // The OAuth secret can be retrieved by calling:
                // ((OAuthCredential)authResult.getCredential()).getSecret().
                val user = it.user
                jumpToMain(user)
            }
            .addOnFailureListener {
                Log.e(TAG, it.stackTraceToString())
            }

    }

    private fun googleOauthLogin() {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/auth"), // authorization endpoint
            Uri.parse("https://oauth2.googleapis.com/token")
        ) // token endpoint
        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,  // the authorization service configuration
            "OAUTH_CLIENT",  // the client ID, typically pre-registered and static
            ResponseTypeValues.CODE,  //
            Uri.parse("com.example.firenews:/oauth2redirect")
        ) // the redirect URI to which the auth response is sent
        authRequestBuilder.setScope("openid email profile")
        val authRequest = authRequestBuilder.build()
        googleOauthLauncher.launch(authRequest)
    }

    private fun onGoogleOauthResult(data: Intent?) {
        if (data != null) {
            val response = AuthorizationResponse.fromIntent(data)
            val ex = AuthorizationException.fromIntent(data)
            val authState = AuthState(response, ex)
            if (response != null) {
                performGoogleTokenRequest(authState, response)
            } else if (ex != null) {
                Log.e(TAG, ex.stackTraceToString())
            }
        }
    }

    private fun performGoogleTokenRequest(authState: AuthState, response: AuthorizationResponse) {
        val service = AuthorizationService(this)
        service.performTokenRequest(
            response.createTokenExchangeRequest()
        ) { tokenResponse, exception ->
            if (exception != null) {
                Log.e(TAG, "Token Exchange failed", exception)
            } else if (tokenResponse != null) {
                authState.update(tokenResponse, null)
                val idToken=tokenResponse.idToken
                Log.e(
                    TAG,
                    "Token Response [ Access Token: ${tokenResponse.accessToken}, ID Token: ${tokenResponse.idToken}"
                )
                if(idToken!=null){
                    createGoogleCredential(idToken)
                }

            }

        }
    }

    private fun createGoogleCredential(idToken: String?) {
        val credential=GoogleAuthProvider.getCredential(idToken,null)
        signInWithAuthCredential(credential)
    }

}