package com.example.soundnova

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnova.databinding.SearchBinding
import com.example.soundnova.models.TrackData
import com.example.soundnova.models.Tracks
import com.example.soundnova.screens.adapters.OnItemClickTrackListener
import com.example.soundnova.screens.adapters.SongAdapter
import com.example.soundnova.service.DeezerSearchApiHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {
    private var _binding: SearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter
    private var tracks: Tracks = Tracks()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = SearchBinding.bind(view)
        val searchRecent = SearchRecent(requireContext())


        binding.editTextSearch.requestFocus()
        binding.editTextSearch.isFocusable = true
        binding.editTextSearch.isFocusableInTouchMode = true
        binding.editTextSearch.requestFocus()

        binding.recentSearchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SongAdapter(tracks, object : OnItemClickTrackListener {
            override fun onItemClick(position: Int, tracks: Tracks) {
                val selectedTrack = tracks.data[position]
                lifecycleScope.launch {
                    saveTrackToFirebase(selectedTrack, searchRecent)
                    val bundle = Bundle().apply {
                        putParcelable("tracks", tracks)
                        putInt("position", position)
                    }
                    (activity as? HomeActivity)?.handleMusicBottomBar(bundle)
                }
            }

        }, viewType = 1)
        binding.recentSearchRecyclerView.adapter = adapter

        lifecycleScope.launch {
            val recentTracks = searchRecent.fetchRecentSongs()
            tracks.data = recentTracks.data.toMutableList()
            adapter.notifyDataSetChanged()
        }

        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    binding.textRecent.visibility = View.GONE
                    binding.clearBtn.visibility = View.GONE
                    fetchTracks(query)
                } else {
                    binding.textRecent.visibility = View.VISIBLE
                    binding.clearBtn.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val recentTracks = searchRecent.fetchRecentSongs()
                        tracks.data = recentTracks.data.toMutableList()
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearBtn.setOnClickListener {
            lifecycleScope.launch {
                searchRecent.clear()
                tracks.data.clear()
                adapter.notifyDataSetChanged()
                updateClearButtonVisibility()
            }
        }

    }
    private fun fetchTracks(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = DeezerSearchApiHelper.fetchTracks(query)
                withContext(Dispatchers.Main) {
                    tracks.data = result.data.toMutableList()
                    adapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error fetching tracks: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun saveTrackToFirebase(track: TrackData, searchRecent: SearchRecent) {
        val idSong = track.id ?: 0
        val title = track.title ?: ""
        val artist = listOf(track.artist?.name ?: "Unknown Artist")
        val image = track.artist?.pictureBig ?: ""
        val audioUrl = track.preview ?: ""

        searchRecent.addSearchRecent(idSong, title, artist, image, audioUrl)

    }

    private fun updateClearButtonVisibility() {
        binding.clearBtn.visibility = if (tracks.data.isEmpty()) View.GONE else View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
