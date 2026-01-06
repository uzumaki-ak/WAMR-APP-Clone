package com.wamr.recovery.adapters

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

class MessageAdapter : ListAdapter<MessageEntity, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderText: TextView = itemView.findViewById(R.id.senderText)
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val appText: TextView = itemView.findViewById(R.id.appText)
        private val deletedBadge: TextView = itemView.findViewById(R.id.deletedBadge)

        fun bind(message: MessageEntity) {
            senderText.text = message.sender
            messageText.text = message.message
            timeText.text = formatTime(message.timestamp)
            appText.text = getAppName(message.packageName)

            deletedBadge.visibility = if (message.isDeleted) View.VISIBLE else View.GONE
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun getAppName(packageName: String): String {
            return when {
                packageName.contains("whatsapp") -> "WhatsApp"
                packageName.contains("telegram") -> "Telegram"
                packageName.contains("messenger") || packageName.contains("orca") -> "Messenger"
                packageName.contains("instagram") -> "Instagram"
                else -> packageName.substringAfterLast(".")
            }
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