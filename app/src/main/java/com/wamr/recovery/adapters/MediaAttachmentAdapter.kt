package com.wamr.recovery.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wamr.recovery.R
import com.wamr.recovery.database.MessageEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MediaAttachmentAdapter : ListAdapter<MessageEntity, MediaAttachmentAdapter.ViewHolder>(MediaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_attachment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        private val fileName: TextView = itemView.findViewById(R.id.fileName)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val btnDownload: TextView = itemView.findViewById(R.id.btnDownload)

        fun bind(message: MessageEntity) {
            fileName.text = message.mediaPath?.substringAfterLast("/") ?: "Media"
            timeText.text = formatTime(message.timestamp)

            message.mediaPath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    Glide.with(itemView.context)
                        .load(file)
                        .centerCrop()
                        .into(thumbnail)
                }

                btnDownload.setOnClickListener {
                    openMedia(file)
                }
            }
        }

        private fun openMedia(file: File) {
            val uri = FileProvider.getUriForFile(
                itemView.context,
                "${itemView.context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, getMimeType(file))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            itemView.context.startActivity(intent)
        }

        private fun getMimeType(file: File): String {
            return when (file.extension.lowercase()) {
                "jpg", "jpeg", "png", "gif", "webp" -> "image/*"
                "mp4", "mkv", "avi" -> "video/*"
                "mp3", "wav", "ogg" -> "audio/*"
                else -> "*/*"
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class MediaDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            return oldItem == newItem
        }
    }
}