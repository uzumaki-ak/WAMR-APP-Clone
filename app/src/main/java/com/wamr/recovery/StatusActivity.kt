package com.wamr.recovery

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wamr.recovery.adapters.StatusAdapter
import com.wamr.recovery.utils.StatusManager
import kotlinx.coroutines.launch

class StatusActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var emptyText: TextView
    private val statusManager by lazy { StatusManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        initViews()
        setupRecyclerView()
        loadStatuses()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewStatus)
        emptyText = findViewById(R.id.emptyText)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        statusAdapter = StatusAdapter { statusItem ->
            statusManager.downloadStatus(statusItem.filePath) { success ->
                if (success) {
                    Toast.makeText(this, "Status downloaded to Downloads/WAMR_Status", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@StatusActivity, 2)
            adapter = statusAdapter
        }
    }

    private fun loadStatuses() {
        lifecycleScope.launch {
            val statuses = statusManager.getAllStatuses()
            if (statuses.isEmpty()) {
                emptyText.visibility = TextView.VISIBLE
                emptyText.text = "No statuses found.\n\nMake sure you've viewed some WhatsApp statuses first!"
            } else {
                emptyText.visibility = TextView.GONE
            }
            statusAdapter.submitList(statuses)
        }
    }

    override fun onResume() {
        super.onResume()
        loadStatuses()
    }
}