//package com.example.soundnova.models
//
//
//
//import com.google.gson.annotations.SerializedName
//
//data class GenresResponse(
//    @SerializedName("data")
//    val data: List<Genre>
//)
//
//data class Genre(
//    @SerializedName("id")
//    val id: Long,
//
//    @SerializedName("name")
//    val name: String,
//
//    /*    @SerializedName("picture")
//        val picture: String,
//
//        @SerializedName("picture_small")
//        val pictureSmall: String,
//
//        @SerializedName("picture_medium")
//        val pictureMedium: String,*/
//
//    @SerializedName("picture_big")
//    val pictureBig: String,
//
//    /*
//        @SerializedName("picture_xl")
//        val pictureXl: String,
//    */
//)


package com.example.soundnova.models



import com.google.gson.annotations.SerializedName

data class GenresResponse(
    @SerializedName("data")
    val data: List<Genre>?
)

data class Genre(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("name")
    val name: String?,

    /*    @SerializedName("picture")
        val picture: String,

        @SerializedName("picture_small")
        val pictureSmall: String,

        @SerializedName("picture_medium")
        val pictureMedium: String,*/

    @SerializedName("picture_big")
    val pictureBig: String?,

    /*
        @SerializedName("picture_xl")
        val pictureXl: String,
    */
)
