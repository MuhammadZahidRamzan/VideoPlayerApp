package com.example.zaplayer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zaplayer.databinding.*
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class VideoAdapter(private val context:Context, private var videoList: ArrayList<Video> ,private val isFolder:Boolean = false):RecyclerView.Adapter<VideoAdapter.MyHolder>() {
    class MyHolder(binding: VideoViewBinding): RecyclerView.ViewHolder(binding.root) {
        val title = binding.listVideoname
        val folder = binding.listFolder
        val duration = binding.listDuration
        val image = binding.listPic
        val root = binding.root


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoAdapter.MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: VideoAdapter.MyHolder, position: Int) {
        holder.title.text = videoList[position].title
        holder.folder.text = videoList[position].foldername
        holder.duration.text = DateUtils.formatElapsedTime(videoList[position].duration/1000)
        Glide.with(context).
                asBitmap().load(videoList[position].artUri).apply(RequestOptions().placeholder(R.mipmap.ic_launcher).centerCrop()).
                into(holder.image)
        holder.root.setOnClickListener {
            when{
                videoList[position].id == PlayerActivity.nowPlayingId -> {
                    sendintent(pos = position, ref = "NowPlaying")

                }
                isFolder -> {
                    PlayerActivity.pipStatus = 1
                    sendintent(pos = position, ref = "FolderActivity")

                }
                MainActivity.search -> {
                    PlayerActivity.pipStatus = 2
                    sendintent(pos = position, ref = "SearchVideos")

                }
                else -> {
                    PlayerActivity.pipStatus = 3
                    sendintent(pos = position, ref = "AllVideos")
                }
            }

        }
        holder.root.setOnLongClickListener {
            val customdialog = LayoutInflater.from(context).inflate(R.layout.video_more,holder.root,false)
            val bindingMF = VideoMoreBinding.bind(customdialog)
            val diaolog = MaterialAlertDialogBuilder(context).setView(customdialog)
                .create()
            diaolog.show()
            bindingMF.renamebtn.setOnClickListener {
                requestPermition()
                diaolog.dismiss()
                val customdialogRF = LayoutInflater.from(context).inflate(R.layout.rename_field,holder.root,false)
                val bindingRF = RenameFieldBinding.bind(customdialogRF)
                val diaologRF = MaterialAlertDialogBuilder(context).setView(customdialogRF)
                    .setCancelable(false)
                    .setPositiveButton("Rename"){self, _ ->
                        val currentFile = File(videoList[position].path)
                        val newName = bindingRF.renamefield.text
                        if (newName != null && currentFile.exists() && newName.toString().isNotEmpty()){
                            val newFile = File(currentFile.parentFile , newName.toString()+"."+currentFile.extension)
                            if (currentFile.renameTo(newFile)){
                                MediaScannerConnection.scanFile(context, arrayOf(newFile.toString()), arrayOf("video/*"),null)
                                when{
                                    MainActivity.search -> {
                                        MainActivity.searchList[position].title = newName.toString()
                                        MainActivity.searchList[position].path = newFile.path
                                        MainActivity.searchList[position].artUri = Uri.fromFile(newFile)
                                        notifyItemChanged(position)
                                    }
                                    isFolder -> {
                                        FolderActivity.currentfoldervideos[position].title = newName.toString()
                                        FolderActivity.currentfoldervideos[position].path = newFile.path
                                        FolderActivity.currentfoldervideos[position].artUri = Uri.fromFile(newFile)
                                        notifyItemChanged(position)
                                        MainActivity.dataChange = true
                                    }
                                    else -> {
                                        MainActivity.videoList[position].title = newName.toString()
                                        MainActivity.videoList[position].path = newFile.path
                                        MainActivity.videoList[position].artUri = Uri.fromFile(newFile)
                                        notifyItemChanged(position)
                                    }
                                }




                            }else{
                                Toast.makeText(context,"Permittion Denied",Toast.LENGTH_SHORT).show()
                            }
                        }
                        self.dismiss()

                    }
                    .setNegativeButton("Cancel"){self, _ ->
                        self.dismiss()


                    }
                    .create()
                diaologRF.show()
                bindingRF.renamefield.text = SpannableStringBuilder(videoList[position].title)
                diaologRF.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(MaterialColors.getColor(context,R.attr.themeColor,
                    Color.BLUE))
                diaologRF.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(MaterialColors.getColor(context,R.attr.themeColor,
                    Color.BLUE))

            }
            bindingMF.sharebtn.setOnClickListener {
                diaolog.dismiss()
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "video/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(videoList[position].path))
                ContextCompat.startActivity(context,Intent.createChooser(shareIntent,"Shearing Video File!!"),null)
            }
            bindingMF.detailbtn.setOnClickListener {
                diaolog.dismiss()
                val customdialogIF = LayoutInflater.from(context).inflate(R.layout.detail_view,holder.root,false)
                val bindingIF = DetailViewBinding.bind(customdialogIF)
                val diaologIF = MaterialAlertDialogBuilder(context).setView(customdialogIF)
                    .setCancelable(false)
                    .setPositiveButton("Ok"){self, _ ->
                        self.dismiss()
                    }
                    .create()
                diaologIF.show()
                val infoText = SpannableStringBuilder().bold { append("DETAILS\n\nName:") }.append(videoList[position].title)
                    .bold { append("\n\nDuration:") }.append(DateUtils.formatElapsedTime(videoList[position].duration/1000))
                    .bold { append("\n\nName:") }.append(videoList[position].title)
                    .bold { append("\n\nFile Size:") }.append(Formatter.formatShortFileSize(context,videoList[position].size.toLong()))
                    .bold { append("Location:") }.append(videoList[position].path)
                bindingIF.detail.text = infoText
                diaologIF.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(MaterialColors.getColor(context,R.attr.themeColor,
                    Color.BLUE))

            }
            bindingMF.deleteBtn.setOnClickListener {
                requestPermition()
                diaolog.dismiss()
                val diaologDF = MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Video")
                    .setMessage(videoList[position].title)
                    .setPositiveButton("Yes"){self, _ ->
                        val file = File(videoList[position].path)
                        if (file.exists() && file.delete()){
                            MediaScannerConnection.scanFile(context, arrayOf(file.path),
                                arrayOf("video/*"),null)
                            when{
                                MainActivity.search ->{
                                    MainActivity.dataChange = true
                                    videoList.removeAt(position)
                                    notifyDataSetChanged()
                                }
                                isFolder -> {
                                    MainActivity.dataChange = true
                                    FolderActivity.currentfoldervideos.removeAt(position)
                                    notifyDataSetChanged()
                                }
                                else -> {
                                    MainActivity.videoList.removeAt(position)
                                    notifyDataSetChanged()
                                }

                            }

                        }
                        else
                        {
                            Toast.makeText(context,"Permittion Denied!!",Toast.LENGTH_SHORT).show()
                        }
                        self.dismiss()

                    }
                    .setNegativeButton("No"){self, _ ->
                        self.dismiss()


                    }
                    .create()
                diaologDF.show()
                diaologDF.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(MaterialColors.getColor(context,R.attr.themeColor,
                    Color.BLUE))
                diaologDF.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(MaterialColors.getColor(context,R.attr.themeColor,
                    Color.BLUE))

            }
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount() = videoList.size
    private fun sendintent(pos:Int,ref:String){
        PlayerActivity.position = pos
        val intent = Intent(context,PlayerActivity::class.java)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(searchList : ArrayList<Video>){
        videoList = ArrayList()
        videoList.addAll(searchList)
        notifyDataSetChanged()
    }
    // for requesting andriod11 or haigher storage permition
    private fun requestPermition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:${context.applicationContext.packageName}")
                ContextCompat.startActivity(context,intent,null)
            }
        }
    }


}