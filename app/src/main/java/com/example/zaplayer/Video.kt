package com.example.zaplayer

import android.net.Uri

data class Video(
    val id:String,
    var title:String,
    val duration:Long = 0,
    val foldername:String,
    val size:String,
    var path:String,
    var artUri:Uri
)
data class Folders(
    val id: String,
    val foldername: String
)
