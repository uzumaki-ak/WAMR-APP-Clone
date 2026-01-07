package com.wamr.recovery.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wamr.recovery.R
import com.wamr.recovery.database.MessageEntity
import java.text.SimpleDateFormat
import java.util.*

class MessageDetailAdapter : ListAdapter<MessageEntity, MessageDetailAdapter.ViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderText: TextView = itemView.findViewById(R.id.senderText)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val deletedBadge: TextView = itemView.findViewById(R.id.deletedBadge)

        fun bind(message: MessageEntity) {
            senderText.text = message.sender
            messageText.text = message.message
            timeText.text = formatTime(message.timestamp)

            if (message.isDeleted) {
                deletedBadge.visibility = View.VISIBLE
                itemView.setBackgroundColor(Color.parseColor("#1A8B0000"))
            } else {
                deletedBadge.visibility = View.GONE
                itemView.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageEntity>() {
        override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean {
            return oldItem == newItem
        }
    }
}