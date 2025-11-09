package com.example.soundnova.screens.music_player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundnova.HomeActivity
import com.example.soundnova.R
import com.example.soundnova.databinding.LibraryBinding
import com.example.soundnova.models.Album
import com.example.soundnova.models.Albums
import com.example.soundnova.models.Artist
import com.example.soundnova.models.TrackData
import com.example.soundnova.models.Tracks
import com.example.soundnova.screens.adapters.FavoriteLibraryAdapter
import com.example.soundnova.screens.adapters.FavoriteLibraryItem
import com.example.soundnova.screens.adapters.OnFavoriteItemClickListener
import com.example.soundnova.service.DeezerApiHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {

    private lateinit var binding: LibraryBinding
    private lateinit var adapter: FavoriteLibraryAdapter
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private var favoriteSongs: Tracks = Tracks()
    private var favoriteAlbums: Albums = Albums()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    private suspend fun fetchFavoriteAlbums(): Albums = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        if (userEmail == null) {
            Log.e("LibraryFragment", "User is not logged in.")
            return@withContext Albums()
        }

        try {
            val documents = db.collection("favorite_library")
                .whereEqualTo("type", "album")
                .whereEqualTo("idUser", userEmail)
                .get()
                .await()

            val favoriteAlbums = Albums()
            favoriteAlbums.data = mutableListOf()

            for (document in documents) {
                try {
                    val albumId = document.getLong("id")
                    val title = document.getString("title")
                    val artistId = document.getLong("artistId")
                    val artistName = document.getString("artistName")
                    val coverUrl = document.getString("coverUrl")

                    if (albumId != null) {
                        // Tạo Album object từ dữ liệu Firestore (không cần gọi API)
                        val album = Album(
                            id = albumId,
                            title = title,
                            coverBig = coverUrl,
                            coverMedium = coverUrl,
                            coverSmall = coverUrl,
                            artist = Artist(
                                id = artistId,
                                name = artistName,
                                pictureBig = null
                            ),
                            albumDetailsResponse = null,
                            isLiked = true
                        )

                        favoriteAlbums.data.add(album)
                        Log.d("LibraryFragment", "Fetched album: ${album.title}, Artist: ${artistName}")
                    }
                } catch (e: Exception) {
                    Log.e("LibraryFragment", "Error processing album document: ", e)
                }
            }

            Log.d("LibraryFragment", "Fetched album count: ${favoriteAlbums.data.size}")
            return@withContext favoriteAlbums

        } catch (exception: Exception) {
            Log.e("LibraryFragment", "Error fetching favorite albums: ", exception)
            return@withContext Albums()
        }
    }

    private suspend fun fetchFavoriteSongs(): Tracks = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        if (userEmail == null) {
            Log.e("LibraryFragment", "User is not logged in.")
            return@withContext Tracks()
        }

        try {
            val documents = db.collection("favorite_library")
                .whereEqualTo("type", "song")
                .whereEqualTo("idUser", userEmail)
                .get()
                .await()

            val favoriteSongs = Tracks()
            favoriteSongs.data = mutableListOf()

            for (document in documents) {
                try {
                    val idSong = document.getLong("idSong")
                    val title = document.getString("title")
                    val artist = document.get("artist") as? List<String> ?: emptyList()
                    val image = document.getString("image")
                    val audioUrl = document.getString("audioUrl")

                    if (idSong != null) {
                        // Fetch full track details from Deezer API
                        val trackData = DeezerApiHelper.getTrack(idSong.toString())

                        val track = TrackData(
                            id = trackData.id,
                            title = trackData.title ?: title,
                            duration = trackData.duration ?: 0,
                            artist = trackData.artist,
                            album = trackData.album,
                            preview = trackData.preview ?: audioUrl,
                            isLiked = true
                        )

                        favoriteSongs.data.add(track)
                        Log.d("LibraryFragment", "Fetched song: ${track.title}, Artist: ${track.artist?.name}")
                    }
                } catch (e: Exception) {
                    Log.e("LibraryFragment", "Error processing song document: ", e)
                }
            }

            Log.d("LibraryFragment", "Fetched song count: ${favoriteSongs.data.size}")
            return@withContext favoriteSongs

        } catch (exception: Exception) {
            Log.e("LibraryFragment", "Error fetching favorite songs: ", exception)
            return@withContext Tracks()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            try {
                // Fetch favorite songs
                val tracks = fetchFavoriteSongs()
                favoriteSongs = tracks
                Log.d("LibraryFragment", "Fetched favorite tracks: ${tracks.data.size}")

                // Fetch favorite albums
                val albums = fetchFavoriteAlbums()
                favoriteAlbums = albums
                Log.d("LibraryFragment", "Fetched favorite albums: ${albums.data.size}")

                // Setup RecyclerView
                binding.libraryRecyclerView.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

                val favoriteItems = mutableListOf<FavoriteLibraryItem>()
                tracks.data.forEachIndexed { index, track ->
                    favoriteItems.add(FavoriteLibraryItem.SongItem(track, index))
                }
                favoriteAlbums.data.forEach { album ->
                    favoriteItems.add(FavoriteLibraryItem.AlbumItem(album))
                }

                adapter = FavoriteLibraryAdapter(favoriteItems, object : OnFavoriteItemClickListener {
                    override fun onItemClick(item: FavoriteLibraryItem) {
                        when (item) {
                            is FavoriteLibraryItem.SongItem -> {
                                val bundle = Bundle().apply {
                                    putParcelable("tracks", favoriteSongs)
                                    putInt("position", item.position)
                                }
                                (activity as? HomeActivity)?.handleMusicBottomBar(bundle)
                            }
                            is FavoriteLibraryItem.AlbumItem -> {
                                val albumPosition = favoriteAlbums.data.indexOfFirst { it.id == item.album.id }
                                val position = if (albumPosition >= 0) albumPosition else 0
                                val bundle = Bundle().apply {
                                    putParcelable("albums", favoriteAlbums)
                                    putInt("position", position)
                                }
                                findNavController().navigate(R.id.albumPlayerFragment, bundle)
                            }
                        }
                    }
                })

                binding.libraryRecyclerView.adapter = adapter

            } catch (e: Exception) {
                Log.e("LibraryFragment", "Error in onViewCreated: ", e)
            }
        }
    }
}