package com.example.soundnova.screens.music_player

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.soundnova.R
import com.example.soundnova.databinding.KaraokeBinding
import com.example.soundnova.databinding.PlayerActivityBinding
import com.example.soundnova.databinding.RecordBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import com.example.soundnova.file.downloadAndSaveZipFile
import com.example.soundnova.file.sendSongUrlToServer
import com.example.soundnova.models.TrackData
import java.io.FileInputStream
import java.nio.ByteBuffer

class KaraokeFragment : Fragment() {

    private lateinit var binding: KaraokeBinding
    private val viewModel: MusicPlayerViewModel by activityViewModels()
    private val mediaPlayer : MediaPlayer = MediaPlayer()
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String = ""
    private lateinit var song: TrackData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = KaraokeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = KaraokeBinding.bind(view)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentSongIndex.collect { index ->
                    if (index != -1) {
                        song = viewModel.tracks.value.data[index]
                        binding.tvSongTitle.text = song.title
                        binding.tvSongTitle.isSelected = true
                        val id = song.id
                        val sharedPreferences = requireContext().getSharedPreferences("MusicPlayerPrefs", Context.MODE_PRIVATE)
                        val savedTranscription = sharedPreferences.getString("transcription_$id", null)
                        if (savedTranscription != null) {
                            binding.tvLyrics.text = savedTranscription
                        }
                    }
                }
            }
        }
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.stop()
            Log.e("KaraokeFragment", "MediaPlayer completed")
            stopRecording()
        }
        // Request permissions
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }

        outputFile = "${requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC)}/recording.3gp"

        // Set up button listeners
        binding.btnMuteMic.setOnClickListener {
            val karaokeFile = File(
                requireContext().getExternalFilesDir(null),
                "karaoke_files_${song.id}/karaoke_track.wav"
            )
            if (karaokeFile.exists()) {
                val karaokePath = karaokeFile.absolutePath
                startRecording()
                playKaraokeFromPath(karaokePath)
            } else {
                val ngrokUrl = "https://1c7d-123-30-177-118.ngrok-free.app"
                val songUrl = song.preview!!
                Log.e("KaraokeFragment", "Song URL: $songUrl")
                val id = song.id!!
                lifecycleScope.launch {
                    val result = sendSongUrlToServer(songUrl, ngrokUrl)
                    Log.e("KaraokeFragment", "Result: $result")
                    if (result != null) {
                        val transcription = result.first
                        Toast.makeText(requireContext(), "Transcription: $transcription", Toast.LENGTH_SHORT).show()
                        binding.tvLyrics.text = transcription
                        val sharedPreferences = requireContext().getSharedPreferences("MusicPlayerPrefs", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("transcription_$id", transcription)
                            apply()
                        }
                        val zipFileUrl = result.second
                        zipFileUrl?.let {
                            val karaokePath = downloadAndSaveZipFile(it, requireContext(), id)
                            if (karaokePath != null) {
                                startRecording()
                                playKaraokeFromPath(karaokePath)
                                Toast.makeText(requireContext(), "Karaoke track saved at: $karaokePath", Toast.LENGTH_LONG).show()
                                Log.e("KaraokeFragment", "Karaoke track saved at: $karaokePath")
                            } else {
                                Toast.makeText(requireContext(), "Failed to save karaoke track", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to process audio", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.btnStop.setOnClickListener {
            mediaPlayer.stop()
            stopRecording()
        }
        binding.btnPlayPause.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            } else {
                mediaPlayer.start()
            }

        }
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
            if (viewModel.isPlaying.value) {
                viewModel.mediaPlayer.start()
                viewModel.startSeekBarUpdate()
            }
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                prepare()
                start()
            }
            binding.btnMuteMic.isEnabled = false
            binding.btnStop.isEnabled = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        // Lưu file vào thư mục công khai
        //saveRecordingToPublicStorage()

        binding.btnMuteMic.isEnabled = true
        binding.btnStop.isEnabled = false

        try {
           // val currentSong = viewModel.tracks.value.data[viewModel.currentSongIndex.value]
            // Đường dẫn bài hát gốc và file ghi âm
            val songFilePath = File(
                requireContext().getExternalFilesDir(null),
                "karaoke_files_${song.id}/vocals.wav"
            )
            val recordingFilePath = outputFile

            // Chuyển đổi file sang dữ liệu PCM
            val songPcm = extractPCMData(songFilePath.toString())
            val recordingPcm = extractPCMData(recordingFilePath)

            if (songPcm != null && recordingPcm != null) {
                // Phân tích cao độ
                val songPitch = calculatePitch(songPcm, 44100)
                val recordingPitch = calculatePitch(recordingPcm, 44100)

                // So sánh cao độ và chấm điểm
                val similarity = comparePitch(songPitch, recordingPitch)
                val score = calculateScore(similarity)
                binding.score.text = score.toString()

                // Hiển thị điểm trong giao diện
               // binding.tvScore.text = "Score: $score" // TextView trong `KaraokeBinding`
            } else {
                //binding.tvScore.text = "Error: Could not process audio files"
            }
        } catch (e: Exception) {
            e.printStackTrace()
           // binding.tvScore.text = "Error: ${e.message}"
        }
    }


    private fun saveRecordingToPublicStorage() {
        val sourceFile = File(outputFile)

        // Android 10 trở lên: Sử dụng MediaStore
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "recording_${System.currentTimeMillis()}.3gp")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp")
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            }

            val audioUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (audioUri != null) {
                resolver.openOutputStream(audioUri)?.use { outputStream ->
                    sourceFile.inputStream().copyTo(outputStream)
                }
            }
        } else {
            // Android 9 trở xuống: Lưu trực tiếp vào thư mục công khai
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            if (!publicDir.exists()) publicDir.mkdirs()

            val destFile = File(publicDir, "recording_${System.currentTimeMillis()}.3gp")
            sourceFile.copyTo(destFile, overwrite = true)
        }
    }
    fun extractPCMData(audioFilePath: String): ByteArray? {
        val file = File(audioFilePath)
        if (!file.exists() || !file.canRead()) {
            throw IllegalArgumentException("Cannot access the file at $audioFilePath")
        }

        return if (audioFilePath.endsWith(".wav", ignoreCase = true)) {
            extractPCMFromWAV(file)
        } else if (audioFilePath.endsWith(".3gp", ignoreCase = true)) {
            extractPCMFrom3GP(audioFilePath)
        } else {
            throw UnsupportedOperationException("Unsupported file format: ${file.extension}")
        }
    }

    private fun extractPCMFromWAV(file: File): ByteArray? {
        val inputStream = FileInputStream(file)
        val buffer = inputStream.readBytes()
        inputStream.close()

        // WAV headers are typically the first 44 bytes, skip them to get raw PCM data
        return if (buffer.size > 44) buffer.copyOfRange(44, buffer.size) else null
    }

    private fun extractPCMFrom3GP(audioFilePath: String): ByteArray? {
        val extractor = MediaExtractor()
        extractor.setDataSource(audioFilePath)

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                extractor.selectTrack(i)

                val pcmData = mutableListOf<Byte>()
                val buffer = ByteBuffer.allocate(1024)

                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break

                    pcmData.addAll(buffer.array().take(sampleSize))
                    buffer.clear()
                    extractor.advance()
                }

                extractor.release()
                return pcmData.toByteArray()
            }
        }

        extractor.release()
        return null
    }

    fun calculatePitch(pcmData: ByteArray, sampleRate: Int): Double {
        val normalized = pcmData.map { it / 128.0 }.toDoubleArray()
        val maxLag = sampleRate / 50 // Giới hạn để phát hiện âm thanh tần số thấp
        val minLag = sampleRate / 500 // Giới hạn âm thanh tần số cao

        var maxCorrelation = 0.0
        var bestLag = -1

        for (lag in minLag..maxLag) {
            var correlation = 0.0
            for (i in lag until normalized.size) {
                correlation += normalized[i] * normalized[i - lag]
            }
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation
                bestLag = lag
            }
        }

        return if (bestLag > 0) sampleRate.toDouble() / bestLag else -1.0
    }

    fun comparePitch(songPitch: Double, userPitch: Double): Double {
        val dis = kotlin.math.abs(userPitch-songPitch)/10
        if (dis > songPitch) {
            return 5.0
        }
        return ((songPitch - dis) / songPitch)*100
    }

    fun calculateScore(pitchSimilarity: Double): Int {
        return pitchSimilarity.toInt()
    }

    fun playKaraokeFromPath(path: String) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(path)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaRecorder?.release()
        mediaRecorder = null
        mediaPlayer.release()
    }
}
