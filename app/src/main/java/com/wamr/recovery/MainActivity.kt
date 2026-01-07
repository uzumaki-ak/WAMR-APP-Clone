package com.wamr.recovery

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private lateinit var btnStatusDownloader: Button
    private lateinit var btnClearData: Button

    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PermissionUtils.hasStoragePermission(this)) {
            showFolderSelectionDialog()
        }

        initViews()
        setupRecyclerView()
        checkPermissions()
        loadAppsWithMessages()
        setupButtons()
    }

    private fun showFolderSelectionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("WAMR needs permission to save media files to a separate folder so they won't be deleted when you delete from WhatsApp.\n\nPlease grant storage permission.")
            .setPositiveButton("Grant Permission") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    PermissionUtils.requestAllFilesAccess(this)
                } else {
                    PermissionUtils.requestStoragePermission(this)
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        statusText = findViewById(R.id.statusText)
        btnEnableNotifications = findViewById(R.id.btnEnableNotifications)
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions)
        btnStartService = findViewById(R.id.btnStartService)
        btnStatusDownloader = findViewById(R.id.btnStatusDownloader)
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
        val storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PermissionUtils.hasAllFilesAccess()
        } else {
            PermissionUtils.hasStoragePermission(this)
        }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PermissionUtils.requestAllFilesAccess(this)
            } else {
                PermissionUtils.requestStoragePermission(this)
            }
        }

        btnStartService.setOnClickListener {
            startForegroundService()
        }

        btnStatusDownloader.setOnClickListener {
            startActivity(Intent(this, StatusActivity::class.java))
        }

        btnClearData.setOnClickListener {
            clearAllData()
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
        AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to delete all captured messages and media?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    database.messageDao().deleteAll()
                    Toast.makeText(this@MainActivity, "All data cleared", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
        loadAppsWithMessages()
    }
}