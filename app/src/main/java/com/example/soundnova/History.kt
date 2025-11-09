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

class History(private val context: Context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    fun reorderDocumentIds() {
        val historyCollection = db.collection("history")

        historyCollection.get()
            .addOnSuccessListener { documents ->
                val sortedDocuments = documents.sortedBy { it.id.toIntOrNull() ?: Int.MAX_VALUE }
                var count = 1

                for (document in sortedDocuments) {
                    val data = document.data

                    historyCollection.document(count.toString())
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
                        historyCollection.document(documentId).delete()
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

    fun addHistorySong(
        idSong: Long,
        title: String,
        artist: List<String>,
        image: String,
        audioUrl: String,

    ) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        val historyCollection = db.collection("history")

        val maxIdDeferred = CompletableDeferred<Int>()
        historyCollection.get()
            .addOnSuccessListener { documents ->
                val maxId = documents.size()
                maxIdDeferred.complete(maxId)
            }
            .addOnFailureListener { exception ->
                Log.w("Song", "Error fetching documents for ID generation", exception)
                maxIdDeferred.complete(0)
            }


        maxIdDeferred.invokeOnCompletion {
            historyCollection.get()
                .addOnSuccessListener { documents ->

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

                    historyCollection.document()
                        .set(newSong)
                        .addOnSuccessListener {
                            Log.d("Song", "DocumentSnapshot added with ID: $newId")
                            reorderDocumentIds()
                        }
                        .addOnFailureListener { exception ->
                            Log.w("Song", "Error adding document", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.w("Song", "Error fetching documents for ID generation", exception)
                }
        }
    }

    suspend fun fetchHistorySongs(): Tracks = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        if (userEmail == null) {
            Log.e("LibraryFragment", "User is not logged in.")
            return@withContext Tracks()
        }

        try {
            val documents = db.collection("history")
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val recentSongs = Tracks()
            recentSongs.data = mutableListOf()

            val seenSongs = HashSet<Long>()
            var count = 0

//            for (document in documents) {
//                val idUser = document.getString("idUser")
//                if (idUser == userEmail) {
//
//                    val song = document.toObject(SongData::class.java).apply {
//                        id = document.id.toIntOrNull() ?: 0
//                    }
//                    val track = songToTrack(song)
//                    recentSongs.data.add(track!!)
//
//                    Log.d(
//                        "HistoryFragment",
//                        "Fetched song: ${track.title}, Artist: ${track.artist?.name}"
//                    )
//                }
//            }
            for (document in documents) {
                if (count >= 5) break

                val idUser = document.getString("idUser")
                if (idUser == userEmail) {
                    val song = document.toObject(SongData::class.java).apply {
                        id = document.id.toIntOrNull() ?: 0
                    }

                    if (!seenSongs.contains(song.idSong)) {
                        val track = songToTrack(song)
                        if (track != null) {
                            recentSongs.data.add(track)
                            seenSongs.add(song.idSong!!)
                            count++
                        }
                    }
                }
            }
            Log.d("HistoryFragment1", "Fetched song count: ${recentSongs.data.size}")

            return@withContext recentSongs

        } catch (exception: Exception) {
            Log.e("LibraryFragment", "Error fetching favorite songs: ", exception)
            throw exception
        }
    }

//    fun songToTrack(song: SongData): TrackData {
//        return TrackData(
//            id = song.idSong ?: 0,
//            title = song.title,
//            duration = 20000,
//            artist = Artist(
//                id = 123456789,
//                name = song.artist!!.getOrNull(0),
//                pictureBig = song.image
//            ),
//            album = Album(1,"1","1","1","1", Artist(
//                id = 123456789,
//                name = song.artist!!.getOrNull(0),
//                pictureBig = song.image
//            ),null),
//            preview = song.audioUrl,
//            isLiked = true
//        )
//    }
    suspend fun songToTrack(song: SongData): TrackData? {
        val trackId = song.idSong?.toString() ?: return null // Đảm bảo trackId không null

        return try {
            val trackData = DeezerApiHelper.getTrack(trackId) // Gọi API Deezer để lấy track theo trackId

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

}