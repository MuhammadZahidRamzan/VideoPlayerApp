package com.example.zaplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zaplayer.databinding.FragmentAllVideosBinding


class AllVideos : Fragment() {
    private lateinit var adapter: VideoAdapter
    private lateinit var binding: FragmentAllVideosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.themeList[MainActivity.themeIndex],true)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_videos, container, false)
        binding = FragmentAllVideosBinding.bind(view)

        binding.videorv.hasFixedSize()
        binding.videorv.setItemViewCacheSize(10)
        binding.videorv.layoutManager = LinearLayoutManager(requireContext())
        adapter = VideoAdapter(requireContext(),MainActivity.videoList)
        binding.videorv.adapter = adapter
        binding.totalvideos.text = "Total Videoss: ${MainActivity.videoList.size}"
        binding.nowclickbtn.setOnClickListener {
            val intent = Intent(requireContext(),PlayerActivity::class.java)
            intent.putExtra("class","NowPlaying")
            startActivity(intent)

        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_view,menu)
        val searchView = menu.findItem(R.id.search)?.actionView as SearchView
        searchView.setOnQueryTextListener(object  : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null){
                    MainActivity.searchList = ArrayList()
                    for (video in MainActivity.videoList){
                        if (video.title.lowercase().contains(newText.lowercase()))
                            MainActivity.searchList.add(video)

                    }
                    MainActivity.search = true
                    adapter.updateList(searchList = MainActivity.searchList)
                }
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (PlayerActivity.position != -1){
            binding.nowclickbtn.visibility = View.VISIBLE
        }
        if (MainActivity.adapterChanged) adapter.notifyDataSetChanged()
        MainActivity.adapterChanged = false



    }


}