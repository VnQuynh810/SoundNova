//package com.example.soundnova.models
//
//import android.os.Parcelable
//import com.google.gson.annotations.SerializedName
//import kotlinx.parcelize.Parcelize
//
//@Parcelize
//data class Artists(
//    @SerializedName("data")
//    val data: List<Artist>,
//) : Parcelable
//
//@Parcelize
//data class Artist(
//    @SerializedName("id")
//    val id: Long,
//
//    @SerializedName("name")
//    val name: String,
//
//    @SerializedName("picture_big")
//    val pictureBig: String,
//) : Parcelable

package com.example.soundnova.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artists(
    @SerializedName("data")
    val data: List<Artist>?,
) : Parcelable

@Parcelize
data class Artist(
    @SerializedName("id")
    var id: Long?,

    @SerializedName("name")
    var name: String?,

    @SerializedName("picture_big")
    var pictureBig: String?,
) : Parcelable