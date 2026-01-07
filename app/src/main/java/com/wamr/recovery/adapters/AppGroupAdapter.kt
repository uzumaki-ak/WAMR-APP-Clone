package com.wamr.recovery.adapters



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wamr.recovery.R
import com.wamr.recovery.models.AppGroup

class AppGroupAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<AppGroup, AppGroupAdapter.ViewHolder>(AppGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_group, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val messageCount: TextView = itemView.findViewById(R.id.messageCount)
        private val lastTime: TextView = itemView.findViewById(R.id.lastTime)

        fun bind(appGroup: AppGroup) {
            appName.text = getAppName(appGroup.packageName)
            messageCount.text = "${appGroup.messageCount} messages from ${appGroup.chatCount} chats"
            lastTime.text = formatTime(appGroup.lastTimestamp)

            itemView.setOnClickListener {
                onItemClick(appGroup.packageName)
            }
        }

        private fun getAppName(pkg: String): String {
            return when {
                pkg.contains("whatsapp") -> "WhatsApp"
                pkg.contains("telegram") -> "Telegram"
                pkg.contains("messenger") || pkg.contains("orca") -> "Messenger"
                pkg.contains("instagram") -> "Instagram"
                else -> pkg.substringAfterLast(".")
            }
        }

        private fun formatTime(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000}m ago"
                diff < 86400000 -> "${diff / 3600000}h ago"
                else -> "${diff / 86400000}d ago"
            }
        }
    }

    class AppGroupDiffCallback : DiffUtil.ItemCallback<AppGroup>() {
        override fun areItemsTheSame(oldItem: AppGroup, newItem: AppGroup): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppGroup, newItem: AppGroup): Boolean {
            return oldItem == newItem
        }
    }
}