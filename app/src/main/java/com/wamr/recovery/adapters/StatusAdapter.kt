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
import com.wamr.recovery.models.StatusItem
import java.io.File

class StatusAdapter(
    private val onDownloadClick: (StatusItem) -> Unit
) : ListAdapter<StatusItem, StatusAdapter.ViewHolder>(StatusDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_status, parent, false)
        return ViewHolder(view, onDownloadClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onDownloadClick: (StatusItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val thumbnail: ImageView = itemView.findViewById(R.id.statusThumbnail)
        private val btnDownload: TextView = itemView.findViewById(R.id.btnDownload)
        private val videoIcon: ImageView = itemView.findViewById(R.id.videoIcon)

        fun bind(status: StatusItem) {
            val file = File(status.filePath)

            Glide.with(itemView.context)
                .load(file)
                .centerCrop()
                .into(thumbnail)

            videoIcon.visibility = if (status.isVideo) View.VISIBLE else View.GONE

            btnDownload.setOnClickListener {
                onDownloadClick(status)
            }
        }
    }

    class StatusDiffCallback : DiffUtil.ItemCallback<StatusItem>() {
        override fun areItemsTheSame(oldItem: StatusItem, newItem: StatusItem): Boolean {
            return oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(oldItem: StatusItem, newItem: StatusItem): Boolean {
            return oldItem == newItem
        }
    }
}