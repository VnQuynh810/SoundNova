package com.example.soundnova

import android.content.Context
import android.util.Log
import com.example.soundnova.models.Album
import com.example.soundnova.models.Artist
import com.example.soundnova.models.TrackData
import com.example.soundnova.models.Tracks
import com.example.soundnova.service.DeezerApiHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchRecent(private val context: Context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    fun reorderDocumentIds() {
        val searchRecent = db.collection("search_recent")

        searchRecent.get()
            .addOnSuccessListener { documents ->
                val sortedDocuments = documents.sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
                var count = 1

                for (document in sortedDocuments) {
                    val data = document.data

                    searchRecent.document(count.toString())
                        .set(data)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Tài liệu mới với ID: $count được tạo thành công")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Lỗi khi tạo tài liệu mới với ID: $count", e)
                        }

                    count++
                }

                for (document in documents) {
                    val documentId = document.id
                    if (!documentId.matches(Regex("\\d+"))) {
                        searchRecent.document(documentId).delete()
                            .addOnSuccessListener {
                                Log.d("Firestore", "Tài liệu cũ với ID: $documentId đã được xóa")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Lỗi khi xóa tài liệu cũ với ID: $documentId", e)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Lỗi khi lấy dữ liệu", exception)
            }
    }

    fun addSearchRecent(
        idSong: Long,
        title: String,
        artist: List<String>,
        image: String,
        audioUrl: String,

        ) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        val recentCollection = db.collection("search_recent")

        recentCollection.whereEqualTo("title", title)
            .whereEqualTo("idUser", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val maxIdDeferred = CompletableDeferred<Int>()
                    recentCollection.get()
                        .addOnSuccessListener { allDocuments ->
                            val maxId = allDocuments.size()
                            maxIdDeferred.complete(maxId)
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Song", "Error fetching documents for ID generation", exception)
                            maxIdDeferred.complete(0)
                        }

                    maxIdDeferred.invokeOnCompletion {
                        val newId = maxIdDeferred.getCompleted() + 1
                        val newSong = SongData(
                            idSong = idSong,
                            idUser = userEmail,
                            title = title,
                            artist = artist,
                            image = image,
                            audioUrl = audioUrl,
                            id = newId
                        )

                        recentCollection.document()
                            .set(newSong)
                            .addOnSuccessListener {
                                Log.d("Song", "New song added with ID: $newId")
                                reorderDocumentIds() // Sắp xếp lại các ID tài liệu
                            }
                            .addOnFailureListener { exception ->
                                Log.w("Song", "Error adding new song", exception)
                            }
                    }
                } else {
                    Log.d("SearchRecent", "Song already exists in the recent list.")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("SearchRecent", "Error checking for existing song: ", exception)
            }
    }

    suspend fun fetchRecentSongs(): Tracks = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        if (userEmail == null) {
            Log.e("LibraryFragment", "User is not logged in.")
            return@withContext Tracks()
        }

        try {
            val documents = db.collection("search_recent")
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val recentSongs = Tracks()
            recentSongs.data = mutableListOf()

            for (document in documents) {
                val idUser = document.getString("idUser")
                if (idUser == userEmail) {

                    val song = document.toObject(SongData::class.java).apply {
                        id = document.id.toIntOrNull() ?: 0
                    }
                    val track = songToTrack(song)
                    recentSongs.data.add(track!!)

                    Log.d(
                        "HistoryFragment",
                        "Fetched song: ${track.title}, Artist: ${track.artist?.name}"
                    )
                }
            }

            Log.d("HistoryFragment", "Fetched song count: ${recentSongs.data.size}")

            return@withContext recentSongs

        } catch (exception: Exception) {
            Log.e("LibraryFragment", "Error fetching favorite songs: ", exception)
            throw exception
        }
    }


    suspend fun songToTrack(song: SongData): TrackData? {
        val trackId = song.idSong?.toString() ?: return null

        return try {
            val trackData = DeezerApiHelper.getTrack(trackId)

            TrackData(
                id = trackData.id,
                title = trackData.title,
                duration = trackData.duration ?: 0,
                artist = trackData.artist ?: null,
                album = trackData.album ?: null,
                preview = trackData.preview ?: null,
                isLiked = false
            )
        } catch (e: Exception) {
            Log.e("songToTrack", "Error fetching track details: ${e.message}", e)
            null
        }
    }

    fun clear() {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        if (userEmail == null) {
            Log.e("SearchRecent", "User is not logged in.")
            return
        }
        val searchRecent = db.collection("search_recent")

        searchRecent.whereEqualTo("idUser", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    searchRecent.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Tài liệu với ID: ${document.id} đã được xóa")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Lỗi khi xóa tài liệu với ID: ${document.id}", e)
                        }
                }
                Log.d("Firestore", "Đã xóa toàn bộ tài liệu trong 'search_recent'")
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Lỗi khi lấy dữ liệu để xóa", exception)
            }
    }

}