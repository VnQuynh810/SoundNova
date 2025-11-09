package com.example.soundnova.screens.music_player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.soundnova.HomeActivity
import com.example.soundnova.R
import com.example.soundnova.databinding.AlbumListBinding
import com.example.soundnova.models.Albums
import com.example.soundnova.models.Tracks
import com.example.soundnova.screens.adapters.OnItemClickTrackListener
import com.example.soundnova.screens.adapters.SongAdapter
import com.example.soundnova.service.DeezerApiHelper
import kotlinx.coroutines.launch

class AlbumPlayerFragment: Fragment() {

    private lateinit var binding: AlbumListBinding
    private lateinit var tracks: Tracks
    private lateinit var albums: Albums
    private var currentAlbumIndex = 0
    private lateinit var adapterSong: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = AlbumListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AlbumListBinding.bind(view)

        try {
            albums = arguments?.getParcelable<Albums>("albums")!!
            currentAlbumIndex = arguments?.getInt("position") ?: 0
            playAlbum(currentAlbumIndex)
        } catch (e: Exception) {
            Log.e("MusicPlayerFragment", "Error retrieving tracks", e)
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun playAlbum(Index: Int) {
        val album = albums.data!![Index]
        Glide.with(this).load(album.coverBig).into(binding.imageAlbumCover)
        binding.textAlbumName.text = album.title
        binding.textAlbumName.isSelected = true
        binding.textArtistName.text = "BLACKPINK"
        binding.textArtistName.isSelected = true

        lifecycleScope.launch {
            //val tracks = album.tracks
            val artist = DeezerApiHelper.getArtist()
            val tracks = DeezerApiHelper.getTracksOfAlbum(album.id!!)
            for (track in tracks.data) {
                track.artist = artist
                track.album = album
            }
            binding.recyclerViewSongs.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            if (tracks != null) {
                adapterSong = SongAdapter(tracks, object : OnItemClickTrackListener {
                    override fun onItemClick(position: Int, tracks: Tracks) {
                        val bundle = Bundle().apply {
                            putParcelable("tracks", tracks)
                            putInt("position", position)
                        }
                        (activity as? HomeActivity)?.handleMusicBottomBar(bundle)
                    }
                }, 1)
            }
            binding.recyclerViewSongs.adapter = adapterSong
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}