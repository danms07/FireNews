package com.example.firenews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isSignedUser()){
            jumpToLogin()
        }
        setContentView(R.layout.activity_main)
    }

    private fun isSignedUser():Boolean {
        val auth= Firebase.auth
        val user=auth.currentUser
        return user!=null
    }

    private fun jumpToLogin() {
        val intent= Intent(this,LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}