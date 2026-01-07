package com.wamr.recovery.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wamr.recovery.R
import com.wamr.recovery.models.ChatGroup
import java.text.SimpleDateFormat
import java.util.*

class ChatGroupAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<ChatGroup, ChatGroupAdapter.ViewHolder>(ChatGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_group, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val senderName: TextView = itemView.findViewById(R.id.senderName)
        private val messagePreview: TextView = itemView.findViewById(R.id.messagePreview)
        private val messageCount: TextView = itemView.findViewById(R.id.messageCount)
        private val lastTime: TextView = itemView.findViewById(R.id.lastTime)

        fun bind(chatGroup: ChatGroup) {
            senderName.text = chatGroup.sender
            messagePreview.text = chatGroup.lastMessage
            messageCount.text = "(${chatGroup.messageCount} messages)"
            lastTime.text = formatTime(chatGroup.lastTimestamp)

            itemView.setOnClickListener {
                onItemClick(chatGroup.sender)
            }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class ChatGroupDiffCallback : DiffUtil.ItemCallback<ChatGroup>() {
        override fun areItemsTheSame(oldItem: ChatGroup, newItem: ChatGroup): Boolean {
            return oldItem.sender == newItem.sender
        }

        override fun areContentsTheSame(oldItem: ChatGroup, newItem: ChatGroup): Boolean {
            return oldItem == newItem
        }
    }
}