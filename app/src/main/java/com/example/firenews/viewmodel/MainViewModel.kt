package com.example.firenews.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firenews.Navigator
import com.example.firenews.adapter.NewsAdapter
import com.example.firenews.model.Article
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel:ViewModel(), ValueEventListener {
    var navigator:Navigator?=null
    private val firebaseAnalytics = Firebase.analytics
    private val _news=MutableLiveData<MutableList<Article>>().apply {
        value= mutableListOf()
    }
    val news:LiveData<MutableList<Article>>
    get() {return _news}
    //
    var recyclerAdapter: NewsAdapter
    init{
        val database= Firebase.database
        val sourceRef=database.getReference("/sources/")
        sourceRef.addValueEventListener(this)
        recyclerAdapter=NewsAdapter(this)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        val sources=snapshot.children
        val newsArticles:MutableList<Article> = mutableListOf()
        sources.forEach {source ->
            val articles=source.children
            articles.forEach { articleSnapshot->
                val article =articleSnapshot.getValue(Article::class.java)
                if(article is Article)
                    newsArticles.add(article)
            }

        }
        newsArticles.shuffle()
        _news.value?.addAll(newsArticles)
        _news.postValue(_news.value)
    }

    override fun onCancelled(error: DatabaseError) {
        Log.e("DB Error",error.toString())
    }

    fun onItemClick(article:Article){
        val bundle=Bundle().apply {
            putString("title",article.title)
            putString("id",article.id)
            putString("source", article.source)
        }
        firebaseAnalytics.logEvent("article_click",bundle)
        val url=article.url
        if(url!=null) navigator?.loadUrl(url)
    }
}