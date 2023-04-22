package com.example.firenews

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firenews.databinding.LoginBinding
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


class LoginActivity : AppCompatActivity() {
    lateinit var loginBinding:LoginBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        loginBinding= LoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)
        loginBinding.googleSignInButton.setOnClickListener { googleSignIn() }

    }

    private fun googleSignIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.web_client_id))
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val googleSignInIntent=mGoogleSignInClient.signInIntent
        startActivityForResult(googleSignInIntent,GOOGLE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== GOOGLE_REQUEST){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                val account = task.result
                val token= account.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(token, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this){
                            task ->
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
            } catch (e:ApiException ) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Firebase.crashlytics.recordException(e)
                Log.w(TAG, "signInResult:failed code=" + e.statusCode);

            }
        }
    }

    private fun jumpToMain(user: FirebaseUser?) {
        val welcome=getString(R.string.welcome)
        Toast.makeText(this,"$welcome ${user?.displayName?:""}",Toast.LENGTH_SHORT ).show()
        val intent=Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object{
        const val TAG="LoginActivity"
        const val GOOGLE_REQUEST=1000
    }
}