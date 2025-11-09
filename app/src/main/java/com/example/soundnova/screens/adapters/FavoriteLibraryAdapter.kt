package com.example.soundnova.screens.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.soundnova.R
import com.example.soundnova.models.Album
import com.example.soundnova.models.TrackData

sealed class FavoriteLibraryItem {
    data class SongItem(val track: TrackData, val position: Int) : FavoriteLibraryItem()
    data class AlbumItem(val album: Album) : FavoriteLibraryItem()
}

interface OnFavoriteItemClickListener {
    fun onItemClick(item: FavoriteLibraryItem)
}

class FavoriteLibraryAdapter(
    private val items: List<FavoriteLibraryItem>,
    private val listener: OnFavoriteItemClickListener
) : RecyclerView.Adapter<FavoriteLibraryAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.textSongName)
        private val subtitle: TextView = view.findViewById(R.id.textSongArtist)
        private val image: ImageView = view.findViewById(R.id.imageSong)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items[position])
                }
            }
        }

        fun bind(item: FavoriteLibraryItem) {
            when (item) {
                is FavoriteLibraryItem.SongItem -> {
                    val track = item.track
                    title.text = track.title ?: ""
                    subtitle.text = track.artist?.name ?: ""
                    val imageUrl = track.album?.coverBig
                        ?: track.album?.coverMedium
                        ?: track.artist?.pictureBig
                    Glide.with(image.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(image)
                }
                is FavoriteLibraryItem.AlbumItem -> {
                    val album = item.album
                    title.text = album.title ?: ""
                    subtitle.text = album.artist?.name ?: ""
                    Glide.with(image.context)
                        .load(album.coverBig ?: album.coverMedium ?: album.coverSmall)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(image)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_vertical, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
