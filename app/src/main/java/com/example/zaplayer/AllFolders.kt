package com.example.zaplayer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zaplayer.databinding.FragmentAllFoldersBinding
import com.example.zaplayer.databinding.FragmentAllVideosBinding


class AllFolders : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.themeList[MainActivity.themeIndex],true)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_folders, container, false)

        val binding = FragmentAllFoldersBinding.bind(view)

        binding.folderrv.hasFixedSize()
        binding.folderrv.setItemViewCacheSize(10)
        binding.folderrv.layoutManager = LinearLayoutManager(requireContext())
        binding.folderrv.adapter = FoldersAdapter(requireContext(),MainActivity.folderList)

        binding.totalfolders.text = "Total Folders: ${MainActivity.folderList.size}"
        return view
    }

}