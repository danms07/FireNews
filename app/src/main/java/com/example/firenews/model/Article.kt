package com.example.firenews.model

import com.google.firebase.database.Exclude
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.util.*

data class Article(
    var author:String?="",
    var content:String?="",
    var description:String?="",
    var publishedAt:String?="",
    var title:String?="",
    var url:String?="",
    var urlToImage:String?=""

) {
    val time:String
        get() {
            val time=publishedAt
            return if(time!=null){
                val date =if(time.contains("."))
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(time)
                else SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).parse(time)
                date?.toString() ?: ""
            }
            else ""
        }

    @Exclude
    fun toMap():Map<String,Any?>{
        return mapOf(
            "author" to author,
            "content" to content,
            "description" to description,
            "publishedAt" to publishedAt,
            "title" to title,
            "url" to url,
            "urlToImage" to urlToImage
        )
    }
}