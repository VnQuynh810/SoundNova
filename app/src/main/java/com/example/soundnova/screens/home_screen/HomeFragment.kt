package com.example.soundnova.screens.home_screen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnova.History
import com.example.soundnova.HomeActivity
import com.example.soundnova.R
import com.example.soundnova.databinding.HomeActivityBinding
import com.example.soundnova.models.Albums
import com.example.soundnova.models.Artists
import com.example.soundnova.models.Tracks
import com.example.soundnova.screens.adapters.AlbumAdapter
import com.example.soundnova.screens.adapters.ArtistAdapter
import com.example.soundnova.screens.adapters.OnItemClickAlbumListener
import com.example.soundnova.screens.adapters.OnItemClickArtistListener
import com.example.soundnova.screens.adapters.OnItemClickTrackListener
import com.example.soundnova.screens.adapters.SongAdapter
import com.example.soundnova.service.DeezerApiHelper
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {
    private lateinit var binding: HomeActivityBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapterSong: SongAdapter
    private lateinit var adapterAlbum: AlbumAdapter
    private lateinit var adapterArtist: ArtistAdapter
    private lateinit var adapterRecent: SongAdapter

    private lateinit var history: History

    companion object {
        const val REQUEST_CODE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = HomeActivityBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = HomeActivityBinding.bind(view)
//        binding.recyclerViewTabsSongs.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        history = History(requireContext())

        lifecycleScope.launch {

            if (viewModel.tracks == null || viewModel.albums == null || viewModel.artists == null) {
                val tracks = DeezerApiHelper.fetchPopularTracks()
                viewModel.tracks = tracks
                Log.d("HomeFragment", "Fetched popular tracks: $tracks")
                binding.recyclerViewRecommendSongs.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapterSong = SongAdapter(tracks, object : OnItemClickTrackListener {
                    override fun onItemClick(position: Int, tracks: Tracks) {
                        val bundle = Bundle().apply {
                            putParcelable("tracks", tracks)
                            putInt("position", position)
                        }
                        (activity as? HomeActivity)?.handleMusicBottomBar(bundle)
                    }
                }, 0)
                binding.recyclerViewRecommendSongs.adapter = adapterSong

                val albums = DeezerApiHelper.fetchPopularAlbums()
                viewModel.albums = albums
                binding.recyclerViewPopularAlbums.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapterAlbum = AlbumAdapter(albums, object : OnItemClickAlbumListener {
                    override fun onItemClick(position: Int, albums: Albums) {
                        findNavController().navigate(
                            R.id.action_homeFragment_to_albumPlayerFragment,
                            Bundle().apply {
                                putParcelable("albums", albums)
                                putInt("position", position)
                            }
                        )
                    }
                })
                binding.recyclerViewPopularAlbums.adapter = adapterAlbum

                val artists = DeezerApiHelper.fetchPopularArtists()
                viewModel.artists = artists
                binding.recyclerViewFavoriteArtists.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapterArtist = ArtistAdapter(artists, object : OnItemClickArtistListener {
                    override fun onItemClick(position: Int, artists: Artists) {
                        findNavController().navigate(
                            R.id.action_homeFragment_to_artistPlayerFragment,
                            Bundle().apply {
                                putParcelable("artists", artists)
                                putInt("position", position)
                            }
                        )
                    }
                })
                binding.recyclerViewFavoriteArtists.adapter = adapterArtist
            } else {
                val tracks = viewModel.tracks!!
                Log.d("HomeFragment", "Fetched popular tracks: $tracks")
                binding.recyclerViewRecommendSongs.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapterSong = SongAdapter(tracks, object : OnItemClickTrackListener {
                    override fun onItemClick(position: Int, tracks: Tracks) {
                        val bundle = Bundle().apply {
                            putParcelable("tracks", tracks)
                            putInt("position", position)
                        }
                        (activity as? HomeActivity)?.handleMusicBottomBar(bundle)
                    }
                }, 0)
                binding.recyclerViewRecommendSongs.adapter = adapterSong

                val albums = viewModel.albums!!
                binding.recyclerViewPopularAlbums.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapterAlbum = AlbumAdapter(albums, object : OnItemClickAlbumListener {
                    override fun onItemClick(position: Int, albums: Albums) {
                        findNavController().navigate(
                            R.id.action_homeFragment_to_albumPlayerFragment,
                            Bundle().apply {
                                putParcelable("albums", albums)
                                putInt("position", position)
                            }
                        )
                    }
                })
                binding.recyclerViewPopularAlbums.adapter = adapterAlbum

                val artists = viewModel.artists!!
                binding.recyclerViewFavoriteArtists.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapterArtist = ArtistAdapter(artists, object : OnItemClickArtistListener {
                    override fun onItemClick(position: Int, artists: Artists) {
                        findNavController().navigate(
                            R.id.action_homeFragment_to_artistPlayerFragment,
                            Bundle().apply {
                                putParcelable("artists", artists)
                                putInt("position", position)
                            }
                        )
                    }
                })
                binding.recyclerViewFavoriteArtists.adapter = adapterArtist
            }

            try {
                val historyTracks = history.fetchHistorySongs()
                Log.d("HistoryFragment", "History tracks: $historyTracks")

                if (historyTracks.data.isNotEmpty()) {
                    binding.recyclerViewRecentSongs.layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    adapterRecent = SongAdapter(historyTracks, object : OnItemClickTrackListener {
                        override fun onItemClick(position: Int, tracks: Tracks) {
                            val trackId = tracks.data[position].id.toString()

                            val bundle = Bundle().apply {
                                putParcelable("tracks", tracks)
                                putInt("position", position)
                            }
                            (activity as? HomeActivity)?.handleMusicBottomBar(bundle)

                        }
                    }, 0)
                    binding.recyclerViewRecentSongs.adapter = adapterRecent
                } else {
                    Log.d("HomeFragment", "No recent songs found.")
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching history songs", e)
            }
        }
    }
}

