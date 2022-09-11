package com.example.zaplayer

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zaplayer.databinding.ActivityFolderBinding
import com.example.zaplayer.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception

class FolderActivity : AppCompatActivity() {
    companion object{
        lateinit var currentfoldervideos:ArrayList<Video>
    }
    private lateinit var binding: ActivityFolderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.themeList[MainActivity.themeIndex])
        binding = ActivityFolderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
//        val templist = ArrayList<Video>()
//        templist.add(MainActivity.videoList[0])
//        templist.add(MainActivity.videoList[0])
//        templist.add(MainActivity.videoList[0])
//        templist.add(MainActivity.videoList[0])
//        templist.add(MainActivity.videoList[0])

        val position = intent.getIntExtra("position",0)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = MainActivity.folderList[position].foldername
        currentfoldervideos = getallvideos(MainActivity.folderList[position].id)
        binding.recycleracfolder.hasFixedSize()
        binding.recycleracfolder.setItemViewCacheSize(10)
        binding.recycleracfolder.layoutManager = LinearLayoutManager(this@FolderActivity)
        binding.recycleracfolder.adapter = VideoAdapter(this@FolderActivity, currentfoldervideos , isFolder = true)
        binding.totalvideosfolder.text = "Total Videos: ${currentfoldervideos.size}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
    @SuppressLint("Range")
    private fun getallvideos(folderid:String):ArrayList<Video>{
        val tempList = ArrayList<Video>()
        val selection = MediaStore.Video.Media.BUCKET_ID + " like? "
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION , MediaStore.Video.Media.BUCKET_ID)
        val cursor = this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,selection, arrayOf(folderid),
            MediaStore.Video.Media.DATE_ADDED + " DESC ")
        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()
                    try {
                        val file = File(pathC)
                        val arturiC = Uri.fromFile(file)
                        val video = Video(title = titleC, id = idC, foldername = folderC, duration = durationC, size = sizeC,
                            path = pathC, artUri = arturiC
                        )
                        if (file.exists()) tempList.add(video)


                    }catch (e: Exception){}

                }while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }
}