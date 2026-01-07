package com.wamr.recovery

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.wamr.recovery.adapters.MessageDetailAdapter
import com.wamr.recovery.adapters.MediaAttachmentAdapter
import com.wamr.recovery.database.AppDatabase
import kotlinx.coroutines.launch

class MessagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageDetailAdapter
    private lateinit var mediaAdapter: MediaAttachmentAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var titleText: TextView
    private lateinit var packageName: String
    private lateinit var sender: String

    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        packageName = intent.getStringExtra("PACKAGE_NAME") ?: return finish()
        sender = intent.getStringExtra("SENDER") ?: return finish()

        initViews()
        setupTabs()
        setupRecyclerView()
        loadMessages()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewMessages)
        tabLayout = findViewById(R.id.tabLayout)
        titleText = findViewById(R.id.titleText)
        titleText.text = sender

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Notifications"))
        tabLayout.addTab(tabLayout.newTab().setText("Attachments"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadMessages()
                    1 -> loadMediaAttachments()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageDetailAdapter()
        mediaAdapter = MediaAttachmentAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadMessages() {
        recyclerView.adapter = messageAdapter
        lifecycleScope.launch {
            database.messageDao().getMessagesBySenderAndApp(sender, packageName).collect { messages ->
                messageAdapter.submitList(messages)
            }
        }
    }

    private fun loadMediaAttachments() {
        recyclerView.adapter = mediaAdapter
        lifecycleScope.launch {
            database.messageDao().getMediaMessagesBySender(sender, packageName).collect { media ->
                mediaAdapter.submitList(media)
            }
        }
    }
}