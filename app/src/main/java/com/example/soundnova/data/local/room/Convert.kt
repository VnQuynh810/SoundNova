package com.example.soundnova.data.local.room

import androidx.room.TypeConverter

class Convert {
    @TypeConverter
    fun fromListOfString(list: List<String>?): String? = list?.joinToString(", ")

    @TypeConverter
    fun fromStringToList(string: String?): List<String>? = string?.split(",")
}