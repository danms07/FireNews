package com.example.firenews.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.firenews.databinding.ArticleBinding
import com.example.firenews.model.Article
import com.example.firenews.viewmodel.MainViewModel

class NewsAdapter(private val viewModel:MainViewModel): RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {
    var items:List<Article> = listOf()

    class NewsViewHolder(private val binding: ArticleBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(article:Article,viewModel: MainViewModel){
            binding.article=article
            binding.mainVM=viewModel
            binding.articleImage.load(article.urlToImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val binding=ArticleBinding.inflate(inflater,parent,false)
        return NewsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position],viewModel)
    }

    fun onDataChanged(articles:List<Article>){
        this.items=articles
        notifyDataSetChanged()
    }
}