package com.example.firenews

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.firenews.adapter.NewsAdapter
import com.example.firenews.databinding.MainBinding
import com.example.firenews.viewmodel.MainViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(),Navigator {

    lateinit var binding:MainBinding
    lateinit var viewModel:MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isSignedUser()){
            jumpToLogin()
        }
        binding= MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel= ViewModelProvider(this)[MainViewModel::class.java]
        /*viewModel.news.observe(this){articles->
            articles.forEach { article->
                addRecord(article.title?:"empty")
            }
        }*/
        viewModel.navigator=this
        binding.recyclerNews.adapter=viewModel.recyclerAdapter
        viewModel.news.observe(this){articles->
            viewModel.recyclerAdapter.onDataChanged(articles)
        }
        /*val database=Firebase.database
        val sourceRef=database.getReference("/sources/")
        sourceRef.addValueEventListener(this)*/
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



    override fun loadUrl(url: String) {
        val intent=Intent(this,WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.URL,url)
        startActivity(intent)
    }

    override fun displayMessage(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

}