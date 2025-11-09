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
import com.example.soundnova.models.Albums
import com.example.soundnova.models.Tracks
import com.example.soundnova.screens.adapters.SongAdapter.SongViewHolder


interface OnItemClickAlbumListener {
    fun onItemClick(position: Int, albums : Albums)
}

class AlbumAdapter(private val listAlbum: Albums, private val listener: OnItemClickAlbumListener) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {
    inner class AlbumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition, albums = listAlbum)
            }
        }
        val albumName: TextView = view.findViewById(R.id.textAlbumName)
        val albumArtists: TextView = view.findViewById(R.id.textAlbumArtist)
        val albumImage: ImageView = view.findViewById(R.id.imageAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }
    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = listAlbum.data!![position]
        holder.albumName.text = album.title
        holder.albumArtists.text = "BLACKPINK"
        Glide.with(holder.itemView.context).load(album.coverBig).into(holder.albumImage)
    }

    override fun getItemCount(): Int = listAlbum.data!!.size
}