package com.wamr.recovery

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wamr.recovery.adapters.AppGroupAdapter
import com.wamr.recovery.database.AppDatabase
import com.wamr.recovery.services.ForegroundService
import com.wamr.recovery.services.NotificationListener
import com.wamr.recovery.utils.PermissionUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appGroupAdapter: AppGroupAdapter
    private lateinit var statusText: TextView
    private lateinit var btnEnableNotifications: Button
    private lateinit var btnRequestPermissions: Button
    private lateinit var btnStartService: Button
    private lateinit var btnClearData: Button

    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        checkPermissions()
        loadAppsWithMessages()
        setupButtons()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        statusText = findViewById(R.id.statusText)
        btnEnableNotifications = findViewById(R.id.btnEnableNotifications)
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions)
        btnStartService = findViewById(R.id.btnStartService)
        btnClearData = findViewById(R.id.btnClearData)
    }

    private fun setupRecyclerView() {
        appGroupAdapter = AppGroupAdapter { packageName ->
            openChatsActivity(packageName)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = appGroupAdapter
        }
    }

    private fun checkPermissions() {
        val notificationEnabled = NotificationListener.isEnabled(this)
        val storageGranted = PermissionUtils.hasStoragePermission(this)

        updateStatusText(notificationEnabled, storageGranted)
    }

    private fun updateStatusText(notificationEnabled: Boolean, storageGranted: Boolean) {
        val status = buildString {
            append("Status:\n")
            append("• Notification Access: ${if (notificationEnabled) "✓" else "✗"}\n")
            append("• Storage Permission: ${if (storageGranted) "✓" else "✗"}")
        }
        statusText.text = status
    }

    private fun setupButtons() {
        btnEnableNotifications.setOnClickListener {
            openNotificationSettings()
        }

        btnRequestPermissions.setOnClickListener {
            PermissionUtils.requestStoragePermission(this)
        }

        btnStartService.setOnClickListener {
            startForegroundService()
        }

        btnClearData.setOnClickListener {
            clearAllData()
        }
        findViewById<Button>(R.id.btnStatusDownloader).setOnClickListener {
            startActivity(Intent(this, StatusActivity::class.java))
        }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }

    private fun startForegroundService() {
        if (!NotificationListener.isEnabled(this)) {
            Toast.makeText(this, "Enable notification access first", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ForegroundService::class.java)
        startForegroundService(intent)
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show()
    }

    private fun loadAppsWithMessages() {
        lifecycleScope.launch {
            database.messageDao().getAppsWithMessageCount().collect { apps ->
                appGroupAdapter.submitList(apps)
            }
        }
    }

    private fun openChatsActivity(packageName: String) {
        val intent = Intent(this, ChatsActivity::class.java).apply {
            putExtra("PACKAGE_NAME", packageName)
        }
        startActivity(intent)
    }

    private fun clearAllData() {
        lifecycleScope.launch {
            database.messageDao().deleteAll()
            Toast.makeText(this@MainActivity, "All data cleared", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        loadAppsWithMessages()
    }
}