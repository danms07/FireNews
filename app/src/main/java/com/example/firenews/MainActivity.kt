package com.example.firenews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.firenews.databinding.MainBinding
import com.example.firenews.viewmodel.MainViewModel
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), Navigator {

    private lateinit var binding: MainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isSignedUser()) {
            jumpToLogin()
        }
        supportActionBar?.apply {
            setDisplayUseLogoEnabled(true)
            setIcon(R.drawable.ic_action_bar)
            setDisplayShowHomeEnabled(true)
            elevation = 12.0f
        }
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        /*viewModel.news.observe(this){articles->
            articles.forEach { article->
                addRecord(article.title?:"empty")
            }
        }*/
        val user = Firebase.auth.currentUser
        if (user != null && user.isEmailVerified) {
            Toast.makeText(this, "Email verified", Toast.LENGTH_SHORT).show()
        }
        viewModel.navigator = this
        binding.recyclerNews.adapter = viewModel.recyclerAdapter
        viewModel.news.observe(this) { articles ->
            viewModel.recyclerAdapter.onDataChanged(articles)
        }
    }

    override fun onResume() {
        super.onResume()
        lookForDynamicLinks()
    }


    private fun isSignedUser(): Boolean {
        val auth = Firebase.auth
        val user = auth.currentUser
        Log.e("user", "${user?.email} verified? ${user?.isEmailVerified}")
        return user != null
    }

    private fun jumpToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout_menu -> {
                val auth = Firebase.auth
                auth.signOut()
                jumpToLogin()
            }

            R.id.crash -> {
                throw RuntimeException("Test Crash from menu")
            }

            R.id.license_item -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun loadUrl(url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.URL, url)
        startActivity(intent)
    }

    override fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun lookForDynamicLinks() {
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                if (pendingDynamicLinkData != null) {
                    val deepLink = pendingDynamicLinkData.link
                    if (deepLink != null) {
                        //Get mode
                        when (deepLink.getQueryParameter("mode")) {
                            "verifyEmail" -> {
                                //Get the action code from the query parameters
                                val verificationCode = deepLink.getQueryParameter("oobCode")
                                Log.d("EmailVerification", verificationCode.toString())
                                if (verificationCode != null) {
                                    //Complete the email verification flow
                                    verifyEmail(verificationCode)
                                }
                            }
                            else -> {/*Handle other deeplink modes*/
                            }
                        }
                    }
                }
            }
            .addOnFailureListener(this) { e ->
                Log.w(
                    "Dynamic links",
                    "getDynamicLink:onFailure",
                    e
                )
            }
    }

    private fun verifyEmail(emailVerificationCode: String) {
        Firebase.auth.checkActionCode(emailVerificationCode).addOnSuccessListener {
            Firebase.auth.applyActionCode(emailVerificationCode).addOnSuccessListener {
                val user = Firebase.auth.currentUser
                user?.reload()//Force a refresh on the access token to get the latest value of isEmailVerified
            }
                .addOnFailureListener {
                    Log.e("EmailVerification", "Email verification error $it")
                }
        }
            .addOnFailureListener {
                Log.e("EmailVerification", "Action code check error $it")
                //Consider to send a new email verification message
            }

    }
}