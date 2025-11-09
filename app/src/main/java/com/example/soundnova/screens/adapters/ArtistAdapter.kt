package com.example.soundnova.screens.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnova.R
import com.example.soundnova.models.Artists
import android.view.ViewGroup
import android.view.LayoutInflater
import com.bumptech.glide.Glide

interface OnItemClickArtistListener {
    fun onItemClick(position: Int, artists: Artists)
}

class ArtistAdapter(private val listArtist: Artists, private val listener: OnItemClickArtistListener) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {
    inner class ArtistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition, artists = listArtist)
            }
        }
        val artistName: TextView = view.findViewById(R.id.textArtistName)
        val artistImage: ImageView = view.findViewById(R.id.imageArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = listArtist.data!![position]
        holder.artistName.text = artist.name
        Glide.with(holder.itemView.context).load(artist.pictureBig).into(holder.artistImage)
    }

    override fun getItemCount(): Int = listArtist.data!!.size
}
