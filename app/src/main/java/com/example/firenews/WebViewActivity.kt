package com.example.firenews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebViewClient
import androidx.core.app.NavUtils
import com.example.firenews.databinding.WebBinding

class WebViewActivity : AppCompatActivity() {
    companion object{
        const val URL="url"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url=intent.getStringExtra(URL)
        if(url.isNullOrEmpty()) finish()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val binding=WebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webView.webViewClient= WebViewClient()
        binding.webView.loadUrl(url!!)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}