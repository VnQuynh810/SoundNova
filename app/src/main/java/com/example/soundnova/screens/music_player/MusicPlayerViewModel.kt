package com.example.soundnova.screens.music_player

import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundnova.models.Tracks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicPlayerViewModel : ViewModel() {

    private val _isMusicPlayed = MutableStateFlow(false)
    val isMusicPlayed: StateFlow<Boolean> = _isMusicPlayed


    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _heartBoolean = MutableStateFlow(false)
    val heartBoolean: StateFlow<Boolean> = _heartBoolean

    private val _shuffleBoolean = MutableStateFlow(false)
    val shuffleBoolean: StateFlow<Boolean> = _shuffleBoolean

    private val _repeatBoolean = MutableStateFlow(false)
    val repeatBoolean: StateFlow<Boolean> = _repeatBoolean

    private val _tracks = MutableStateFlow(Tracks())
    val tracks: StateFlow<Tracks> = _tracks

    private val _currentSongIndex = MutableStateFlow(-1)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex

    private val _currentPreColor = MutableStateFlow(0)
    val currentPreColor: StateFlow<Int> = _currentPreColor

    private val _currentSongLyrics = MutableStateFlow("")
    val currentSongLyrics: StateFlow<String> = _currentSongLyrics

    private val _seekBarProgress = MutableStateFlow(0)
    val seekBarProgress: StateFlow<Int> = _seekBarProgress

    val mediaPlayer: MediaPlayer = MediaPlayer()

    private var seekBarUpdateJob: Job? = null

    fun updateIsMusicPlayed(isPlayed: Boolean) {
        _isMusicPlayed.value = isPlayed
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun updateHeartState(isHearted: Boolean) {
        _heartBoolean.value = isHearted
    }

    fun updateShuffleState(isShuffled: Boolean) {
        _shuffleBoolean.value = isShuffled
    }

    fun updateRepeatState(isRepeated: Boolean) {
        _repeatBoolean.value = isRepeated
    }

    fun updateTracks(tracks: Tracks) {
        _tracks.value = tracks
    }

    fun updateCurrentSongIndex(index: Int) {
        _currentSongIndex.value = index
    }

    fun updateSeekBarProgress(progress: Int) {
        _seekBarProgress.value = progress
    }

    fun updateCurrentPreColor(color: Int) {
        _currentPreColor.value = color
    }

    fun updateCurrentSongLyrics(lyrics: String) {
        _currentSongLyrics.value = lyrics
    }

    fun startSeekBarUpdate() {
        seekBarUpdateJob = viewModelScope.launch {
            while (mediaPlayer.isPlaying) {
                val currentPosition = mediaPlayer.currentPosition
                updateSeekBarProgress(currentPosition)
                delay(1000L)
            }
        }
    }

    fun stopSeekBarUpdate() {
        seekBarUpdateJob?.cancel()
        seekBarUpdateJob = null
    }

    override fun onCleared() {
        super.onCleared()
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        mediaPlayer.release()
        stopSeekBarUpdate()
    }
}
