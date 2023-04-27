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

class MainActivity : AppCompatActivity() {

    lateinit var binding:MainBinding
    lateinit var viewModel:MainViewModel
    lateinit var recyclerAdapter:NewsAdapter
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
        recyclerAdapter=NewsAdapter(viewModel)

        /*val database=Firebase.database
        val sourceRef=database.getReference("/sources/")
        sourceRef.addValueEventListener(this)*/
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerNews.adapter=recyclerAdapter
        viewModel.news.observe(this){articles->
            recyclerAdapter.items=articles
            recyclerAdapter.notifyDataSetChanged()
        }
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

    private fun addRecord(record:String){
        binding.tvTest.append("$record \n")
    }

}