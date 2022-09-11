package com.example.zaplayer

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.zaplayer.databinding.ActivityMainBinding
import com.example.zaplayer.databinding.ThemeViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.lang.Exception
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private val sortList = arrayOf(MediaStore.Video.Media.DATE_ADDED + " DESC ",MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.TITLE, MediaStore.Video.Media.TITLE + " DESC ",
        MediaStore.Video.Media.SIZE, MediaStore.Video.Media.SIZE + " DESC ")
    private var runable:Runnable? = null
    companion object{
        lateinit var videoList:ArrayList<Video>
        lateinit var folderList:ArrayList<Folders>
        lateinit var searchList:ArrayList<Video>
        var search:Boolean = false
        var themeIndex:Int = 0
        var dataChange:Boolean = false
        var adapterChanged:Boolean = false
        private var sortValue:Int = 0
        val themeList = arrayOf(R.style.orange,R.style.green,R.style.blue,R.style.red,R.style.mela,R.style.yellow)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val editor = getSharedPreferences("Themes", MODE_PRIVATE)
        themeIndex = editor.getInt("themeIndex" , 0)

        setTheme(themeList[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)


        toggle = ActionBarDrawerToggle(this,binding.root,R.string.open,R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (getpermittion()){
            folderList = ArrayList()
            videoList = getallvideos()
            setFragment(AllVideos())
            runable = Runnable {
                if (dataChange){
                    videoList = getallvideos()
                    dataChange = false
                    adapterChanged = true
                }
                Handler(Looper.getMainLooper()).postDelayed(runable!!,200)
            }
            Handler(Looper.getMainLooper()).postDelayed(runable!!,0)

        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId)
            {
                R.id.video -> setFragment(AllVideos())
                R.id.folders -> setFragment(AllFolders())

            }
            return@setOnItemSelectedListener true
        }
        binding.navview.setNavigationItemSelectedListener {
            if(dataChange) videoList = getallvideos()
            when(it.itemId)
            {
                R.id.feedback -> Toast.makeText(this,"Feedback",Toast.LENGTH_SHORT).show()
                R.id.themes -> {
                    val customdialog = LayoutInflater.from(this).inflate(R.layout.theme_view,binding.root,false)
                    val bindingTV = ThemeViewBinding.bind(customdialog)
                    val diaolog = MaterialAlertDialogBuilder(this).setView(customdialog)
                        .setTitle("Set Theme !")
                        .create()
                    diaolog.show()
                    when(themeIndex){
                        0 -> bindingTV.orange.setBackgroundColor(Color.YELLOW)
                        1 -> bindingTV.green.setBackgroundColor(Color.YELLOW)
                        2 -> bindingTV.blue.setBackgroundColor(Color.YELLOW)
                        3 -> bindingTV.red.setBackgroundColor(Color.YELLOW)
                        4 -> bindingTV.mela.setBackgroundColor(Color.YELLOW)
                        5 -> bindingTV.yellow.setBackgroundColor(Color.YELLOW)
                    }
                    bindingTV.orange.setOnClickListener {
                        saveTheme(0)
                    }
                    bindingTV.green.setOnClickListener {
                        saveTheme(1)
                    }
                    bindingTV.blue.setOnClickListener {
                        saveTheme(2)
                    }
                    bindingTV.red.setOnClickListener {
                        saveTheme(3)
                    }
                    bindingTV.mela.setOnClickListener {
                        saveTheme(4)
                    }
                    bindingTV.yellow.setOnClickListener {
                        saveTheme(5)
                    }
                }
                R.id.sortorder -> {
                    val menuItem= arrayOf("Latest","Oldest","Name(A to Z)",
                        "Name(Z to A)","File Size(Smallest)","File Size(Largest)")
                    var value = sortValue
                    val diaolog = MaterialAlertDialogBuilder(this)
                        .setTitle("Sort Order")
                        .setPositiveButton("Ok"){_, _ ->
                            val sortEditor = getSharedPreferences("Sorting", MODE_PRIVATE).edit()
                            sortEditor.putInt("sortValue" , value)
                            sortEditor.apply()
                            finish()
                            startActivity(intent)

                        }
                        .setSingleChoiceItems(menuItem, sortValue){ _, pos ->
                            value = pos

                        }
                        .create()
                    diaolog.show()
                    diaolog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.BLACK)
                }
                R.id.about -> startActivity(Intent(this,AboutActivity::class.java))
                R.id.exit -> showFinalScoreDialog()
            }
            return@setNavigationItemSelectedListener true
        }
    }
    private fun setFragment(fragment:Fragment){
        val transition = supportFragmentManager.beginTransaction()
        transition.replace(R.id.fragmentcontainer,fragment)
        transition.disallowAddToBackStack()
        transition.commit()
    }
    private fun getpermittion():Boolean{
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),26)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 26){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Permittion granted....",Toast.LENGTH_SHORT).show()
                folderList = ArrayList()
                videoList = getallvideos()
                setFragment(AllVideos())
            }

            else
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),26)

        }
    }
    private fun showFinalScoreDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure to Exit.....")
        builder.setTitle("Exit")
        builder.setCancelable(true)
        builder.setPositiveButton("Yes"){ _, _ ->


            exitProcess(1)

        }
        builder.setNegativeButton("NO"){ dialog, _ ->
            dialog.dismiss()
        }
        val alert = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.BLACK)
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(Color.BLACK)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)

    }
    @SuppressLint("Range")
    private fun getallvideos():ArrayList<Video>{
        val sortEditor = getSharedPreferences("Sorting", MODE_PRIVATE)
        sortValue =sortEditor.getInt("sortValue" , 0)
        val tempList = ArrayList<Video>()
        val tempFolderList = ArrayList<String>()
        val projection = arrayOf(MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATA, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION ,MediaStore.Video.Media.BUCKET_ID)
        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null,
            sortList[sortValue])
        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderidC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
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
                        if (!tempFolderList.contains(folderC)){
                            tempFolderList.add(folderC)
                            folderList.add(Folders(id = folderidC, foldername = folderC))
                        }

                    }catch (e:Exception){}

                }while (cursor.moveToNext())
                cursor?.close()
        return tempList
    }
    private fun saveTheme(index:Int){
        val editor = getSharedPreferences("Themes", MODE_PRIVATE).edit()
        editor.putInt("themeIndex" , index)
        editor.apply()
        finish()
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        runable=null
    }
}
