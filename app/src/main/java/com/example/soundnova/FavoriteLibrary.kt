package com.example.soundnova

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class FavoriteAlbumData(
    val id: Long = 0,
    val idUser: String = "",
    val title: String = "",
    val artistId: Long = 0,
    val artistName: String = "",
    val coverUrl: String = "",
    val type: String = "album"
)

class FavoriteLibrary(private val context: Context) {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    fun reorderDocumentIds() {
        val favCollection = db.collection("favorite_library")

        favCollection.get()
            .addOnSuccessListener { documents ->
                val sortedDocuments = documents.sortedBy { it.id }
                var count = 1

                for (document in sortedDocuments) {
                    val data = document.data

                    favCollection.document(count.toString())
                        .set(data)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Document with ID: $count created successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error creating document with ID: $count", e)
                        }

                    count++
                }

                for (document in documents) {
                    val documentId = document.id
                    if (!documentId.matches(Regex("\\d+"))) {
                        favCollection.document(documentId).delete()
                            .addOnSuccessListener {
                                Log.d("Firestore", "Old document with ID: $documentId deleted")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error deleting old document with ID: $documentId", e)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error fetching data", exception)
            }
    }

    fun addFavSong(
        idSong: Long,
        title: String,
        artist: List<String>,
        image: String,
        audioUrl: String,
    ) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email
        val newSong = hashMapOf(
            "idSong" to idSong,
            "idUser" to userEmail,
            "title" to title,
            "artist" to artist,
            "image" to image,
            "audioUrl" to audioUrl,
            "type" to "song"
        )
        db.collection("favorite_library")
            .add(newSong)
            .addOnSuccessListener { documentReference ->
                Log.d("Song", "DocumentSnapshot added with ID: ${documentReference.id}")
                reorderDocumentIds()
            }
            .addOnFailureListener { exception ->
                Log.w("Song", "Error adding document", exception)
            }
    }

    fun addFavAlbum(
        id: Long,
        title: String,
        artistId: Long,
        artistName: String,
        coverUrl: String,
    ) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email ?: return

        val newAlbum = hashMapOf(
            "id" to id,
            "idUser" to userEmail,
            "title" to title,
            "artistId" to artistId,
            "artistName" to artistName,
            "coverUrl" to coverUrl,
            "type" to "album"
        )

        db.collection("favorite_library")
            .add(newAlbum)
            .addOnSuccessListener { documentReference ->
                Log.d("Album", "Album added with ID: ${documentReference.id}")
                reorderDocumentIds()
            }
            .addOnFailureListener { exception ->
                Log.w("Album", "Error adding album", exception)
            }
    }

    fun removeFavSong(title: String) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        db.collection("favorite_library")
            .whereEqualTo("type", "song")
            .whereEqualTo("title", title)
            .whereEqualTo("idUser", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Document deleted: ${document.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error deleting document: ${document.id}", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching data: ", e)
            }
    }

    fun removeFavAlbum(albumId: Long) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        db.collection("favorite_library")
            .whereEqualTo("type", "album")
            .whereEqualTo("id", albumId)
            .whereEqualTo("idUser", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("Firestore", "Album deleted: ${document.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error deleting album: ${document.id}", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching data: ", e)
            }
    }

    fun checkFavSong(title: String, callback: (Boolean) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        db.collection("favorite_library")
            .whereEqualTo("type", "song")
            .whereEqualTo("title", title)
            .whereEqualTo("idUser", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                callback(!documents.isEmpty)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking favorite song: ", e)
                callback(false)
            }
    }

    fun checkFavAlbum(albumId: Long, callback: (Boolean) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        val userEmail = currentUser?.email

        db.collection("favorite_library")
            .whereEqualTo("type", "album")
            .whereEqualTo("id", albumId)
            .whereEqualTo("idUser", userEmail)
            .get()
            .addOnSuccessListener { documents ->
                callback(!documents.isEmpty)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking favorite album: ", e)
                callback(false)
            }
    }
}