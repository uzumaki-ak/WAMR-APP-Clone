package com.wamr.recovery.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wamr.recovery.R
import com.wamr.recovery.models.MediaItem
import java.io.File

class MediaAdapter(
    private val onItemClick: (MediaItem) -> Unit
) : ListAdapter<MediaItem, MediaAdapter.MediaViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MediaViewHolder(
        itemView: View,
        private val onItemClick: (MediaItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        private val fileName: TextView = itemView.findViewById(R.id.fileName)
        private val fileSize: TextView = itemView.findViewById(R.id.fileSize)

        fun bind(media: MediaItem) {
            fileName.text = media.fileName
            fileSize.text = formatFileSize(media.fileSize)

            val file = File(media.filePath)
            if (file.exists()) {
                Glide.with(itemView.context)
                    .load(file)
                    .centerCrop()
                    .into(thumbnail)
            }

            itemView.setOnClickListener {
                onItemClick(media)
            }
        }

        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        }
    }

    class MediaDiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
            return oldItem == newItem
        }
    }
}