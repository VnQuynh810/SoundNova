package com.example.soundnova.file

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream


suspend fun sendSongUrlToServer(songUrl: String, ngrokUrl: String): Pair<String?, String?>? {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val requestBody = mapOf(
        "url" to songUrl,
        "ngrok_url" to ngrokUrl
    )
    val jsonBody = Gson().toJson(requestBody)
    val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), jsonBody)
    val request = Request.Builder()
        .url("$ngrokUrl/process-audio")
        .post(body)
        .build()

    return withContext(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val jsonResponse = Gson().fromJson(responseBody, Map::class.java)
                val transcription = jsonResponse["transcription"] as? String
                val zipFileUrl = jsonResponse["zip_file"] as? String
                Pair(transcription, zipFileUrl)
            } else {
                null
            }
        } catch (e : Exception) {
            e.printStackTrace()
            null
        }
    }
}


suspend fun playKaraokeTrack(karaokeTrack: ByteArray) {
    val mediaPlayer = MediaPlayer()
    try {
        val tempFile = File.createTempFile("karaoke_track", ".wav")
        tempFile.deleteOnExit()

        val outputStream = FileOutputStream(tempFile)
        outputStream.write(karaokeTrack)
        outputStream.close()

        mediaPlayer.setDataSource(tempFile.absolutePath)
        mediaPlayer.prepare()
        mediaPlayer.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun getNgrokPublicUrl(): String? {
    return try {
        val client = OkHttpClient()
        val request = Request.Builder().url("http://localhost:4040/api/tunnels").build()
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val jsonResponse = JSONObject(responseBody)
            Log.e("Ngrok", "Response: $jsonResponse")
            val tunnels = jsonResponse.getJSONArray("tunnels")
            if (tunnels.length() > 0) {
                tunnels.getJSONObject(0).getString("public_url")
            } else {
                Log.e("Ngrok", "No tunnels found")
                null
            }
        } else {
            Log.e("Ngrok", "Failed to get public URL")
            null
        }
    } catch (e: Exception) {
        Log.e("Ngrok", "Error getting public URL: ${e.message}")
        null
    }
}

suspend fun downloadAndSaveZipFile(
    zipFileUrl: String,
    context: Context,
    id: Long
): String? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(zipFileUrl).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val contentLength = response.body?.contentLength() ?: 0
                val zipFilePath = File(context.getExternalFilesDir(null), "karaoke_output_$id.zip")
                response.body?.byteStream()?.use { inputStream ->
                    FileOutputStream(zipFilePath).use { outputStream ->
                        val buffer = ByteArray(8 * 1024) // Buffer 8 KB
                        var bytesRead: Int
                        var totalBytesRead = 0L
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                        }
                        if (totalBytesRead != contentLength) {
                            Log.e(
                                "DownloadAndSaveZipFile",
                                "Downloaded size ($totalBytesRead) does not match expected size ($contentLength)"
                            )
                            return@withContext null
                        }
                    }
                }
                val outputDir = File(context.getExternalFilesDir(null), "karaoke_files_$id")
                if (!outputDir.exists()) {
                    outputDir.mkdirs()
                }
                ZipInputStream(zipFilePath.inputStream()).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val fileNameWithId = entry.name
                        val file = File(outputDir, fileNameWithId)
                        file.outputStream().use { fos ->
                            zis.copyTo(fos)
                        }
                        entry = zis.nextEntry
                    }
                }
                Log.e("DownloadAndSaveZipFile", "Success to download zip file: ${response.code}")
                File(outputDir, "karaoke_track.wav").absolutePath
            } else {
                Log.e("DownloadAndSaveZipFile", "Failed to download zip file")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DownloadAndSaveZipFile", "Error downloading zip file: ${e.message}")
            null
        }
    }
}




