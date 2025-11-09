package com.example.soundnova

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.soundnova.databinding.ActivityMainBinding
import com.example.soundnova.models.TrackData
import com.example.soundnova.models.Tracks
import com.example.soundnova.screens.music_player.MusicPlayerFragment
import com.example.soundnova.screens.music_player.MusicPlayerViewModel
import com.example.soundnova.service.LyricsApiHelper
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MusicPlayerViewModel by viewModels()
    private lateinit var gestureDetector: GestureDetector
    private lateinit var fav: FavoriteLibrary
    private lateinit var history: History

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        fav = FavoriteLibrary(this)
        history = History(this)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.musicPlayerFragment, R.id.lyricsFragment, R.id.karaokeFragment, R.id.changePassword -> {
                    binding.bottomNavigationView.visibility = View.GONE
                    binding.musicBottomBar.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                            viewModel.isMusicPlayed.collect { isPlayed ->
                                binding.musicBottomBar.visibility = if (isPlayed) View.VISIBLE else View.GONE
                            }
                        }
                    }
                }
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    navController.navigate(R.id.homeFragment)
                    true
                }

                R.id.libraryFragment -> {
                    navController.navigate(R.id.libraryFragment)
                    true
                }

                R.id.searchFragment -> {
                    navController.navigate(R.id.searchFragment)
                    true
                }

                R.id.settingsFragment -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }

                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> binding.bottomNavigationView.menu.findItem(R.id.homeFragment).isChecked = true
                R.id.libraryFragment -> binding.bottomNavigationView.menu.findItem(R.id.libraryFragment).isChecked = true
                R.id.searchFragment -> binding.bottomNavigationView.menu.findItem(R.id.searchFragment).isChecked = true
                R.id.settingsFragment -> binding.bottomNavigationView.menu.findItem(R.id.settingsFragment).isChecked = true
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isPlaying.collect { isPlaying ->
                    val playPauseIcon =
                        if (isPlaying) R.drawable.icon_pause else R.drawable.icon_play
                    binding.playPause.setImageResource(playPauseIcon)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.heartBoolean.collect { heartBoolean ->
                    val heartIcon =
                        if (heartBoolean) R.drawable.icon_heart_on else R.drawable.icon_heart
                    binding.heartBtn.setImageResource(heartIcon)
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.seekBarProgress.collect { progress ->
                    binding.songSeekBar.progress = progress
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentSongIndex.collect { index ->
                    if (index != -1) {
                        val song = viewModel.tracks.value.data[index]
                        fav.checkFavSong(song.title!!) { isFavorite ->
                            runOnUiThread {
                                val heartIcon =
                                    if (isFavorite) R.drawable.icon_heart_on else R.drawable.icon_heart
                                binding.heartBtn.setImageResource(heartIcon)
                            }
                        }
                        binding.songName.text = song.title
                        binding.songName.isSelected = true
                        binding.songArtist.text = song.artist!!.name
                        binding.songArtist.isSelected = true
                        Glide.with(this@HomeActivity).load(song.artist!!.pictureBig).circleCrop()
                            .into(binding.coverArt)
                        binding.songSeekBar.max = 30000
                        viewModel.updateSeekBarProgress(0)
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPreColor.collect { color ->
                    val curBackgroundBottomBar = binding.musicBottomBar.background as GradientDrawable
                    curBackgroundBottomBar.setColor(color)
                }
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    fun handleMusicBottomBar(bundle: Bundle) {
        val currentSongIndex = bundle.getInt("position")
        if (currentSongIndex == viewModel.currentSongIndex.value) return;
        try {
            val tracks = bundle.getParcelable<Tracks>("tracks")!!
            viewModel.updateTracks(tracks)
            viewModel.updateCurrentSongIndex(currentSongIndex)
            playSong(currentSongIndex)
        } catch (e: Exception) {
            Log.e("MusicPlayerFragment", "Error retrieving tracks", e)
        }

        binding.musicBottomBar.setOnClickListener {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            navController.navigate(R.id.musicPlayerFragment)
        }

        binding.heartBtn.setOnClickListener {
            val currentSongIndex = viewModel.currentSongIndex.value
            if (currentSongIndex != -1) {
                val song = viewModel.tracks.value.data[currentSongIndex]

                fav.checkFavSong(song.title!!) { isFavorite ->
                    if (isFavorite) {
                        fav.removeFavSong(song.title!!)
                        runOnUiThread {
                            binding.heartBtn.setImageResource(R.drawable.icon_heart)
                        }
                    } else {
                        fav.addFavSong(
                            idSong = song.id!!,
                            title = song.title!!,
                            artist = listOf(song.artist!!.name!!),
                            image = song.artist!!.pictureBig!!,
                            audioUrl = song.preview!!
                        )
                        runOnUiThread {
                            binding.heartBtn.setImageResource(R.drawable.icon_heart_on)
                        }
                    }
                }
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

        viewModel.mediaPlayer.setOnCompletionListener {
            playNextSong()
        }

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffX = e2.x.minus(e1?.x ?: 0f)
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        playPreviousSong()
                    } else {
                        playNextSong()
                    }
                    return true
                }
                return false
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.musicPlayerFragment)
                return super.onSingleTapUp(e)
            }
        })

        binding.musicScrollView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun playNextSong() {
        val newIndex = (viewModel.currentSongIndex.value + 1) % viewModel.tracks.value.data.size
        viewModel.updateCurrentSongIndex(newIndex)
        playSong(newIndex)
    }

    private fun playPreviousSong() {
        val newIndex = if (viewModel.currentSongIndex.value == 0) {
            viewModel.tracks.value.data.size - 1
        } else {
            viewModel.currentSongIndex.value - 1
        }
        viewModel.updateCurrentSongIndex(newIndex)
        playSong(newIndex)
    }

    private fun playSong(index: Int) {
        viewModel.stopSeekBarUpdate()

        val song = viewModel.tracks.value.data[index]

        val curBackgroundBottomBar = binding.musicBottomBar.background as GradientDrawable
        val currentPreColor = getRandomColor()
        viewModel.updateCurrentPreColor(currentPreColor)
        curBackgroundBottomBar.setColor(currentPreColor)

        viewModel.mediaPlayer.reset()
        viewModel.mediaPlayer.setDataSource(song.preview)
        viewModel.mediaPlayer.prepare()
        history.addHistorySong(song.id!!,song.title!!, song.artist!!.name!!.split(","), song.artist!!.pictureBig!!, song.preview!!)

        if (!viewModel.mediaPlayer.isPlaying) {
            viewModel.mediaPlayer.start()
            viewModel.updateIsPlaying(true)
            viewModel.updateIsMusicPlayed(true)
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

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)

        val currentEntry = navController.currentBackStackEntry

        if (currentEntry?.destination?.id == R.id.homeFragment && navController.previousBackStackEntry == null) {
            super.onBackPressed()
        } else {
            navController.popBackStack()
        }
    }
}
