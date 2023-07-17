package com.example.firenews


import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.firenews.authentication.NativeGoogleSignInContract
import com.example.firenews.databinding.ActivityLoginBinding
import com.example.firenews.databinding.VerificationCodeInputBinding
import com.example.firenews.input.DialogContent
import com.example.firenews.input.InputDialogBuilder
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
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
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity(),
    OnCodeDialogInteractionListener {
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var facebookCallbackManager = CallbackManager.Factory.create()
    private val fbLauncher = registerForActivityResult(
        LoginManager.getInstance().createLogInActivityResultContract(facebookCallbackManager)
    ) {

    }

    private val nativeGoogleSignInLauncher= registerForActivityResult(NativeGoogleSignInContract()){
        onNativeGoogleSignInResult(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth

        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)
        loginBinding.googleLoginButton.setOnClickListener { googleSignIn() }
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
                    val exception=task.exception
                    if(exception!=null){
                        displayErrorAlert(exception.message.toString())
                    }

                }
            }
    }

    private fun loginWithFacebook(){
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

    fun createFacebookCredential(accessToken: AccessToken){
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
        if(googleSignInAccountTask!=null){
            try {
                val account = googleSignInAccountTask.result
                val token = account.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
                signInWithAuthCredential(firebaseCredential)
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
                    val exception=task.exception
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
            .setNeutralButton(R.string.ok){dialogInterface,_->
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

    companion object {
        const val TAG = "LoginActivity"
        const val EMAIL = "email"
        const val PROFILE= "public_profile"

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


}