package com.example.soundnova.screens.home_screen

import androidx.lifecycle.ViewModel
import com.example.soundnova.models.Albums
import com.example.soundnova.models.Artists
import com.example.soundnova.models.Tracks


class HomeViewModel : ViewModel() {
    var tracks: Tracks? = null
    var albums: Albums? = null
    var artists: Artists? = null
}



