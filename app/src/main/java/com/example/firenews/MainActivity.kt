package com.example.firenews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.logout_menu){
            val auth= Firebase.auth
            auth.signOut()
            jumpToLogin()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showToast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
}