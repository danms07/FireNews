package com.example.firenews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.firenews.databinding.SignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    companion object{
        const val EMAIL_REGEX="^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"
        const val TAG="SignUpActivity"
    }

    private lateinit var binding:SignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= SignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.addAccountButton.setOnClickListener{
            verifyAndCreateAccount(
                binding.etEmailInput.text.toString(),
                binding.etPassword.text.toString(),
                binding.etConfirmPassword.text.toString()
            )
        }
    }

    private fun validatePassword(password: String): Boolean {//Needs improvements
        return password.length>=8
    }

    private fun verifyAndCreateAccount(email: String, password: String, confirmPassword: String) {
        if(validateEmail(email)){
            if(validatePassword(password)){
                if(password == confirmPassword){
                    createAccount(email,password)
                }
                else{
                    showSnackbar("The passwords doesn't match")
                }
            }else{
                showSnackbar("Enter a password longer than 8 chars")
            }
        }else{
            showSnackbar("Enter a valid email address")
        }

    }

    private fun createAccount(email: String, password: String) {
        val auth=Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    sendEmailVerification(email)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    val exception=task.exception
                    if(exception!=null){
                        AlertDialog.Builder(this)
                            .setTitle("createUserWithEmail:failure")
                            .setMessage(exception.message)
                            .setCancelable(false)
                            .setPositiveButton(R.string.ok){dialogInterface,_->
                                dialogInterface.dismiss()
                            }
                            .create().show()
                    }

                }
            }
    }

    private fun sendEmailVerification(email: String) {
        val url = "https://firenews-92f91.web.app/"
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            .setHandleCodeInApp(false)
            .setAndroidPackageName(applicationContext.packageName, true, "1.0")
            .build()

        val user = Firebase.auth.currentUser
        user!!.sendEmailVerification(actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    jumpToMain()
                }
            }
    }

    private fun jumpToMain() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.addAccountButton,message,Snackbar.LENGTH_SHORT).show()
    }

    private fun validateEmail(email: String): Boolean {
        return email.matches(Regex(EMAIL_REGEX))
    }
}