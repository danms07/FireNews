package com.example.firenews

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.firenews.databinding.WebBinding
import java.lang.Exception


class WebViewActivity : AppCompatActivity() {
    companion object{
        const val URL="url"
        const val URI="uri"
        const val REDIRECT_URL="redirectUrl"
        const val UNIQUE_STATE="unique_state"
        const val AUTHORIZATION_CODE="AUTHORIZATION_CODE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url=intent.getStringExtra(URL)
        if(url.isNullOrEmpty()) {
            val uriString=intent.extras?.getString(URI)
            if(uriString.isNullOrEmpty()){
                finish()
            }
            else{
                setupOauthMode(uriString)
            }
        }
        else{
            setupNewsMode(url)
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupOauthMode(uriString: String) {
        val redirectUrl=intent.extras?.getString(REDIRECT_URL)
        val uniqueState=intent.extras?.getString(UNIQUE_STATE)
        if(redirectUrl==null) throw Exception("REDIRECT_URL cannot be null if URI mode is used")
        if(uniqueState==null)  throw Exception("UNIQUE_STATE cannot be null if URI mode is used")
        val binding=WebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val webView=binding.webView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        //val uri= Uri.parse(uriString)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                request?.let {
                    // Check if this url is our OAuth redirectUrl, otherwise ignore it
                    if (request.url.toString().startsWith(redirectUrl)) {
                        // To prevent CSRF attacks, check that we got the same state value we sent, otherwise ignore it
                        val responseState = request.url.getQueryParameter("state")
                        if (responseState == uniqueState) {
                            // This is our request. Parse the redirect URL query parameters to get the code
                            request.url.getQueryParameter("code")?.let { code ->
                                // Got it!
                                Log.d("OAuth", "Here is the authorization code! $code")
                                intent.putExtra(AUTHORIZATION_CODE,code)
                                setResult(RESULT_OK,intent)
                                finish()
                                // TODO: Use authorization code to continue with API flow
                            } ?: run {
                                // User cancelled the login flow
                                Log.d("OAuth", "Authorization code not received :(")
                                // TODO: Handle error
                            }
                        }
                    }
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView.loadUrl(uriString)

    }

    private fun setupNewsMode(url:String){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val binding=WebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webView.webViewClient= WebViewClient()
        binding.webView.loadUrl(url)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}