package com.example.firenews.authentication.linkedIn

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.example.firenews.WebViewActivity
import java.lang.Exception

class AuthWebViewContract: ActivityResultContract<AuthWebViewContract.WebAuthConfiguration,String>() {
    override fun createIntent(context: Context, input: WebAuthConfiguration): Intent {
        return Intent(context,WebViewActivity::class.java).apply {
            putExtras(input.parseToExtras())
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        return if(resultCode== RESULT_OK&&intent!=null){
            val code=intent.extras?.getString(WebViewActivity.AUTHORIZATION_CODE)
            code?:throw Exception("Auth code is null")
        }else throw Exception("Result intent is null")
    }

    data class WebAuthConfiguration(val uri:Uri,val redirectUrl:String,val uniqueState:String){
        fun parseToExtras():Bundle{
            return Bundle().apply {
                putString(WebViewActivity.URI,uri.toString())
                putString(WebViewActivity.REDIRECT_URL,redirectUrl)
                putString(WebViewActivity.UNIQUE_STATE,uniqueState)
            }
        }
    }
}