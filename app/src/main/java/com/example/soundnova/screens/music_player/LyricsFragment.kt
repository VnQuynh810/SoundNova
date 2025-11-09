package com.example.soundnova.screens.music_player

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.soundnova.FavoriteLibrary
import com.example.soundnova.History
import com.example.soundnova.R
import com.example.soundnova.databinding.LyricsFragmentBinding
import kotlinx.coroutines.launch

class LyricsFragment : Fragment() {

    private lateinit var history: History
    private lateinit var fav: FavoriteLibrary
    private lateinit var binding: LyricsFragmentBinding
    private val viewModel: MusicPlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = LyricsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val binding = LyricsFragmentBinding.bind(view)


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPlaying.collect { isPlaying ->
                    if (isPlaying) {
                        binding.playPause.setImageResource(R.drawable.icon_pause)
                    } else {
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
                            binding.idHeart.setImageResource(heartIcon)
                        }
                        val id = song.id
                        val sharedPreferences = requireContext().getSharedPreferences("MusicPlayerPrefs", Context.MODE_PRIVATE)
                        val savedTranscription = sharedPreferences.getString("transcription_$id", null)
                        if (savedTranscription != null) {
                            binding.lyricsContent.text = savedTranscription
                        } else {
                            binding.lyricsContent.text = "This song does not support lyrics"
                        }
                        binding.songName.text = song.title
                        binding.songName.isSelected = true
                        binding.songArtist.text = song.artist!!.name
                        binding.songArtist.isSelected = true
                        binding.seekBar.max = 30000
                        binding.seekBar.progress = viewModel.seekBarProgress.value
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.seekBarProgress.collect { progress ->
                    binding.seekBar.progress = progress
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPreColor.collect { color ->
                    binding.layoutController.setBackgroundColor(color)
                }
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

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

        binding.idHeart.setOnClickListener {
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
                binding.idHeart.setImageResource(R.drawable.icon_heart_on)
            } else {
                fav.removeFavSong(viewModel.tracks.value.data[viewModel.currentSongIndex.value].title!!)
                binding.idHeart.setImageResource(R.drawable.icon_heart)
            }
        }

        binding.playPause.setOnClickListener {
            if (viewModel.mediaPlayer.isPlaying) {
                viewModel.mediaPlayer.pause()
                viewModel.updateIsPlaying(false)
                viewModel.stopSeekBarUpdate()
            } else {
                viewModel.mediaPlayer.start()
                viewModel.updateIsPlaying(true)
                viewModel.startSeekBarUpdate()
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

        binding.idKaraoke.setOnClickListener {
            viewModel.mediaPlayer.stop()
            findNavController().navigate(R.id.action_music_to_record)
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack(R.id.musicPlayerFragment, false)
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
                binding.idHeart.setImageResource(R.drawable.icon_heart_on)
            } else {
                binding.idHeart.setImageResource(R.drawable.icon_heart)
            }
        }
    }

    private fun playPreviousSong() {
        val newIndex = if (!viewModel.shuffleBoolean.value) {
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
                binding.idHeart.setImageResource(R.drawable.icon_heart_on)
            } else {
                binding.idHeart.setImageResource(R.drawable.icon_heart)
            }
        }
    }

    private fun playSong(index: Int) {
        viewModel.stopSeekBarUpdate()

        val song = viewModel.tracks.value.data[index]
        fav = FavoriteLibrary(requireContext())
        fav.checkFavSong(viewModel.tracks.value.data[viewModel.currentSongIndex.value].title!!) { isFavorite ->
            if (isFavorite) {
                binding.idHeart.setImageResource(R.drawable.icon_heart_on)
            } else {
                binding.idHeart.setImageResource(R.drawable.icon_heart)
            }
        }

        history = History(requireContext())
        history.addHistorySong(song.id!!,song.title!!, song.artist!!.name!!.split(","), song.artist!!.pictureBig!!, song.preview!!)

        val currentPreColor = getRandomColor()
        viewModel.updateCurrentPreColor(currentPreColor)
        binding.layoutController.setBackgroundColor(viewModel.currentPreColor.value)

        viewModel.mediaPlayer.reset()
        viewModel.mediaPlayer.setDataSource(song.preview)
        viewModel.mediaPlayer.prepare()

        if (!viewModel.mediaPlayer.isPlaying) {
            viewModel.mediaPlayer.start()
            viewModel.updateIsPlaying(true)
            viewModel.startSeekBarUpdate()
        }
    }

    private fun getRandomColor(): Int {
        val random = java.util.Random()

        val red = random.nextInt(100) + 50
        val green = random.nextInt(100) + 50
        val blue = random.nextInt(100) + 50

        return Color.rgb(red, green, blue)
    }
}