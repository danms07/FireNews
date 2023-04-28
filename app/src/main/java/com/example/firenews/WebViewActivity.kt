package com.example.firenews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.firenews.databinding.WebBinding

class WebViewActivity : AppCompatActivity() {
    companion object{
        const val URL="url"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url=intent.getStringExtra(URL)
        if(url.isNullOrEmpty()) finish()

        val binding=WebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webView.loadUrl(url!!)
    }
}