package com.example.soundnova.screens.music_player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.soundnova.R
import com.example.soundnova.databinding.PlayerActivityBinding
import android.view.animation.LinearInterpolator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.SeekBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.navigateUp
import com.example.soundnova.FavoriteLibrary
import com.example.soundnova.History
import kotlinx.coroutines.launch

class MusicPlayerFragment : Fragment() {

    private lateinit var history: History
    private lateinit var fav: FavoriteLibrary
    private lateinit var binding: PlayerActivityBinding
    private val viewModel: MusicPlayerViewModel by activityViewModels()
    private lateinit var rotationAnimator: ObjectAnimator
    private var isRotating = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = PlayerActivityBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = PlayerActivityBinding.bind(view)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPlaying.collect { isPlaying ->
                    if (isPlaying) {
                        startRotationAnimator()
                        binding.playPause.setImageResource(R.drawable.icon_pause)
                    } else {
                        stopRotationAnimator()
                        binding.playPause.setImageResource(R.drawable.icon_play)
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shuffleBoolean.collect { shuffleBoolean ->
                    val shuffleIcon =
                        if (shuffleBoolean) R.drawable.icon_shuffle_on else R.drawable.icon_shuffle_off
                    binding.idShuffle.setImageResource(shuffleIcon)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.repeatBoolean.collect { repeatBoolean ->
                    val repeatIcon =
                        if (repeatBoolean) R.drawable.icon_repeat_on else R.drawable.icon_repeat
                    binding.idRepeat.setImageResource(repeatIcon)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentSongIndex.collect { index ->
                    if (index != -1) {
                        val song = viewModel.tracks.value.data[index]

                        fav = FavoriteLibrary(requireContext())
                        fav.checkFavSong(song.title!!) { isFavorite ->
                            val heartIcon = if (isFavorite) R.drawable.icon_heart_on else R.drawable.icon_heart
                            binding.heartBtn.setImageResource(heartIcon)
                        }

                        binding.songName.text = song.title
                        binding.songName.isSelected = true
                        binding.songArtist.text = song.artist!!.name
                        binding.songArtist.isSelected = true
                        Glide.with(this@MusicPlayerFragment).load(song.artist!!.pictureBig).circleCrop()
                            .into(binding.coverArt)

                        binding.seekBar.max = 30000
                        binding.seekBar.progress = viewModel.seekBarProgress.value

                        binding.durationPlayed.text = formatDuration(viewModel.seekBarProgress.value)
                        binding.durationTotal.text = formatDuration(30000)

                        val id = song.id
                        val sharedPreferences = requireContext().getSharedPreferences("MusicPlayerPrefs", Context.MODE_PRIVATE)
                        val savedTranscription = sharedPreferences.getString("transcription_$id", null)
                        if (savedTranscription != null) {
                            binding.previewLyrics.text = savedTranscription
                            binding.showFullLyricsButton.visibility = View.VISIBLE
                        } else {
                            binding.previewLyrics.text = "This song does not support lyrics"
                            binding.showFullLyricsButton.visibility = View.GONE
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.seekBarProgress.collect { progress ->
                    binding.seekBar.progress = progress
                    binding.durationPlayed.text = formatDuration(progress)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPreColor.collect { color ->
                    val curBackgroundPreLyrics = binding.previewLayout.background as GradientDrawable
                    curBackgroundPreLyrics.setColor(color)
                }
            }
        }

        rotationAnimator = ObjectAnimator.ofFloat(binding.coverArt, "rotation", 0f, 360f).apply {
            duration = 30000L
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.durationPlayed.text = formatDuration(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                viewModel.stopSeekBarUpdate()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    viewModel.mediaPlayer.seekTo(it.progress)
                    viewModel.startSeekBarUpdate()
                }
            }
        })

        binding.heartBtn.setOnClickListener {
            val newHeartState = !viewModel.heartBoolean.value
            viewModel.updateHeartState(newHeartState)

            fav = FavoriteLibrary(requireContext())
            if (viewModel.heartBoolean.value) {
                val currentSong = viewModel.tracks.value.data[viewModel.currentSongIndex.value]
                fav.addFavSong(
                    currentSong.id!!,
                    currentSong.title!!,
                    currentSong.artist!!.name!!.split(","),
                    currentSong.artist!!.pictureBig!!,
                    currentSong.preview!!
                )
                binding.heartBtn.setImageResource(R.drawable.icon_heart_on)
            } else {
                fav.removeFavSong(viewModel.tracks.value.data[viewModel.currentSongIndex.value].title!!)
                binding.heartBtn.setImageResource(R.drawable.icon_heart)
            }
        }

        binding.playPause.setOnClickListener {
            if (viewModel.mediaPlayer.isPlaying) {
                viewModel.mediaPlayer.pause()
                viewModel.updateIsPlaying(false)
                viewModel.stopSeekBarUpdate()
                stopRotationAnimator()
            } else {
                viewModel.mediaPlayer.start()
                viewModel.updateIsPlaying(true)
                viewModel.startSeekBarUpdate()
                startRotationAnimator()
            }
        }

        binding.idShuffle.setOnClickListener {
            val newShuffleState = !viewModel.shuffleBoolean.value
            viewModel.updateShuffleState(newShuffleState)
        }

        binding.idRepeat.setOnClickListener {
            val newRepeatState = !viewModel.repeatBoolean.value
            viewModel.updateRepeatState(newRepeatState)
        }

        viewModel.mediaPlayer.setOnCompletionListener {
            if (viewModel.repeatBoolean.value) {
                viewModel.mediaPlayer.seekTo(0)
                viewModel.mediaPlayer.start()
                viewModel.startSeekBarUpdate()
                startRotationAnimator()
            } else {
                playNextSong()
            }
        }

        binding.idNext.setOnClickListener {
            playNextSong()
        }

        binding.idPrev.setOnClickListener {
            playPreviousSong()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.showFullLyricsButton.setOnClickListener {
            findNavController().navigate(R.id.action_musicPlayerFragment_to_lyricsFragment)
        }

        binding.karaokeBtn.setOnClickListener {
            if (viewModel.mediaPlayer.isPlaying) {
                viewModel.mediaPlayer.pause()
                viewModel.stopSeekBarUpdate()
            }
            findNavController().navigate(R.id.action_music_to_record)
        }

    }

    private fun playNextSong() {
        val newIndex = if (viewModel.shuffleBoolean.value) {
            (0 until viewModel.tracks.value.data.size).random()
        } else {
            (viewModel.currentSongIndex.value + 1) % viewModel.tracks.value.data.size
        }
        viewModel.updateCurrentSongIndex(newIndex)
        playSong(newIndex)

        fav = FavoriteLibrary(requireContext())
        fav.checkFavSong(viewModel.tracks.value.data[viewModel.currentSongIndex.value].title!!) { isFavorite ->
            if (isFavorite) {
                binding.heartBtn.setImageResource(R.drawable.icon_heart_on)
            } else {
                binding.heartBtn.setImageResource(R.drawable.icon_heart)
            }
        }
    }

    private fun playPreviousSong() {
        val newIndex = if (viewModel.shuffleBoolean.value) {
            (0 until viewModel.tracks.value.data.size).random()
        } else {
            if (viewModel.currentSongIndex.value == 0) {
                viewModel.tracks.value.data.size - 1
            } else {
                viewModel.currentSongIndex.value - 1
            }
        }
        viewModel.updateCurrentSongIndex(newIndex)
        playSong(newIndex)

        fav = FavoriteLibrary(requireContext())
        fav.checkFavSong(viewModel.tracks.value.data[viewModel.currentSongIndex.value].title!!) { isFavorite ->
            if (isFavorite) {
                binding.heartBtn.setImageResource(R.drawable.icon_heart_on)
            } else {
                binding.heartBtn.setImageResource(R.drawable.icon_heart)
            }
        }
    }

    private fun playSong(index: Int) {
        viewModel.stopSeekBarUpdate()

        val song = viewModel.tracks.value.data[index]

        fav = FavoriteLibrary(requireContext())
        fav.checkFavSong(viewModel.tracks.value.data[viewModel.currentSongIndex.value].title!!) { isFavorite ->
            if (isFavorite) {
                binding.heartBtn.setImageResource(R.drawable.icon_heart_on)
            } else {
                binding.heartBtn.setImageResource(R.drawable.icon_heart)
            }
        }

        history = History(requireContext())
        history.addHistorySong(song.id!!,song.title!!, song.artist!!.name!!.split(","), song.artist!!.pictureBig!!, song.preview!!)

        val curBackgroundPreLyrics = binding.previewLayout.background as GradientDrawable
        val currentPreColor = getRandomColor()
        viewModel.updateCurrentPreColor(currentPreColor)
        curBackgroundPreLyrics.setColor(viewModel.currentPreColor.value)

        val id = song.id
        val sharedPreferences = requireContext().getSharedPreferences("MusicPlayerPrefs", Context.MODE_PRIVATE)
        val savedTranscription = sharedPreferences.getString("transcription_$id", null)
        if (savedTranscription != null) {
            binding.previewLyrics.text = savedTranscription
            binding.showFullLyricsButton.visibility = View.VISIBLE
        } else {
            binding.previewLyrics.text = "This song does not support lyrics"
            binding.showFullLyricsButton.visibility = View.GONE
        }

        viewModel.mediaPlayer.reset()
        viewModel.mediaPlayer.setDataSource(song.preview)
        viewModel.mediaPlayer.prepare()

        if (!viewModel.mediaPlayer.isPlaying) {
            viewModel.mediaPlayer.start()
            viewModel.updateIsPlaying(true)
            viewModel.startSeekBarUpdate()
            startRotationAnimator()
        }
    }

    private fun startRotationAnimator() {
        if (!isRotating) {
            rotationAnimator.start()
            isRotating = true
        } else {
            rotationAnimator.resume()
        }
    }

    private fun stopRotationAnimator() {
        rotationAnimator.pause()
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return String.format("%2d:%02d", minutes, seconds)
    }

    private fun getRandomColor(): Int {
        val random = java.util.Random()

        val red = random.nextInt(100) + 50
        val green = random.nextInt(100) + 50
        val blue = random.nextInt(100) + 50

        return Color.rgb(red, green, blue)
    }

//        private fun sendRequest(songUrl: String, ngrokUrl: String, id: Long): Pair<String, String?>   {
//            var transcription: String? = null
//            var karaokePath: String? = null
//
//            lifecycleScope.launch {
//                    val result = sendSongUrlToServer(songUrl, ngrokUrl)
//                    Log.e("MusicPlayerFragment", "Result: $result")
//                    if (result != null) {
//                        transcription = result.first
//                        val zipFileUrl = result.second
//                        Toast.makeText(requireContext(), "Transcription: $transcription", Toast.LENGTH_SHORT).show()
//                        zipFileUrl?.let {
//                            karaokePath = downloadAndSaveZipFile(it, requireContext(), id)
//                            if (karaokePath != null) {
//                                Toast.makeText(requireContext(), "Karaoke track saved at: $karaokePath", Toast.LENGTH_LONG).show()
//                            } else {
//                                Toast.makeText(requireContext(), "Failed to save karaoke track", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    } else {
//                        Toast.makeText(requireContext(), "Failed to process audio", Toast.LENGTH_SHORT).show()
//                    }
//            }
//            return Pair(transcription ?: "", karaokePath)
//        }

    override fun onDestroyView() {
        super.onDestroyView()
        stopRotationAnimator()
    }
}