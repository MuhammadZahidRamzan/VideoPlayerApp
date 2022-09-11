package com.example.zaplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.zaplayer.databinding.FolderViewBinding

class FoldersAdapter(private val context: Context, private var folderList: ArrayList<Folders>):
    RecyclerView.Adapter<FoldersAdapter.MyHolder>() {
    class MyHolder(binding: FolderViewBinding): RecyclerView.ViewHolder(binding.root) {
        val foldername = binding.foldername
        val root = binding.root



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoldersAdapter.MyHolder {
        return MyHolder(FolderViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: FoldersAdapter.MyHolder, position: Int) {
        holder.foldername.text = folderList[position].foldername
        holder.root.setOnClickListener {
            val intent = Intent(context,FolderActivity::class.java)
            intent.putExtra("position" , position)
            ContextCompat.startActivity(context,intent,null)
        }

    }

    override fun getItemCount() = folderList.size

}