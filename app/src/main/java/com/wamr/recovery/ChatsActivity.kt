package com.wamr.recovery


import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wamr.recovery.adapters.ChatGroupAdapter
import com.wamr.recovery.database.AppDatabase
import kotlinx.coroutines.launch

class ChatsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatGroupAdapter: ChatGroupAdapter
    private lateinit var titleText: TextView
    private lateinit var packageName: String

    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        packageName = intent.getStringExtra("PACKAGE_NAME") ?: return finish()

        initViews()
        setupRecyclerView()
        loadChats()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewChats)
        titleText = findViewById(R.id.titleText)
        titleText.text = getAppName(packageName)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        chatGroupAdapter = ChatGroupAdapter { sender ->
            openMessagesActivity(sender)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatsActivity)
            adapter = chatGroupAdapter
        }
    }

    private fun loadChats() {
        lifecycleScope.launch {
            database.messageDao().getChatsWithMessageCount(packageName).collect { chats ->
                chatGroupAdapter.submitList(chats)
            }
        }
    }

    private fun openMessagesActivity(sender: String) {
        val intent = Intent(this, MessagesActivity::class.java).apply {
            putExtra("PACKAGE_NAME", packageName)
            putExtra("SENDER", sender)
        }
        startActivity(intent)
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
}