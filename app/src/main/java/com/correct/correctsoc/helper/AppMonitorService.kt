package com.correct.correctsoc.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.correct.correctsoc.MainActivity
import com.correct.correctsoc.R
import com.correct.correctsoc.helper.Constants.PACKAGE
import com.correct.correctsoc.room.UsersDB
import com.correct.correctsoc.ui.home.HomeViewModel
import com.correct.correctsoc.ui.lock.LockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.SortedMap
import java.util.TreeMap

class AppMonitorService : Service() {

    companion object {
        private const val CHANNEL_ID = "AppMonitorChannel"
        private const val NOTIFICATION_ID = 1
    }
    private var lastPackageName: String? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var viewModel: HomeViewModel
    private lateinit var notificationManager: NotificationManager

    // handler to change isAllowed value every 1 minute
    private val lockAppsHandler = Handler(Looper.getMainLooper())
    private val lockAppRunnable = object : Runnable {
        override fun run() {
            serviceScope.launch {
                val usersDB = UsersDB.getDBInstance(applicationContext)
                val dao = usersDB.appDao()
                dao.lockAppsAfterOpen()
            }
            lockAppsHandler.postDelayed(this, 60000)
        }
    }

    // handler to check if opened app is locked or not
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            val currentApp = getForegroundAppName()
            // Handle the app name (e.g., log it, send it to a server, etc.)
            if (lastPackageName != null) {
                if (lastPackageName != currentApp) {
                    println("Current app: $currentApp and last app is $lastPackageName")
                    serviceScope.launch {
                        val usersDB = UsersDB.getDBInstance(applicationContext)
                        val dao = usersDB.appDao()
                        val lockedApp = dao.getLockedApp()
                        if (lockedApp != null) {
                            // if currentApp in locked packages
                            val lockedPackages = lockedApp.map { it.packageName }
                            if (currentApp in lockedPackages) {
                                val isAllowed = currentApp?.let { dao.getAppData(it)?.isAllowed }
                                if (isAllowed != null) {
                                    if (isAllowed == 0) {
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val intent =
                                                Intent(
                                                    this@AppMonitorService,
                                                    LockActivity::class.java
                                                )
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            intent.putExtra(PACKAGE, currentApp)
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            lastPackageName = currentApp
            handler.postDelayed(this, 1000) // Check every second
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(HomeViewModel::class.java)

        viewModel.getNotificationMessage()

        createNotificationChannel()

        viewModel.notificationMessage.observeForever {
            if (it.isSuccess) {
                if (it.result != null) {
                    updateNotification(it.result)
                } else {
                    updateNotification("Null result")
                }
            } else {
                updateNotification("Correctsoc Applocker")
            }
        }
        val initialNotification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("Service running...")
            .setOngoing(true)
            .setSound(null)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.notification_transparent_icon)
            .build()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            startForeground(NOTIFICATION_ID, initialNotification)
        } else {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING
            )
        }

        //startForeground(NOTIFICATION_ID, getNotification())
        handler.post(runnable)
        lockAppsHandler.post(lockAppRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Correctsoc Service"
            val descriptionText = "Correctsoc Applocker Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            channel.setSound(null,null)
            channel.setShowBadge(false)
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(content: String) {
        val notification: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(content)
                .setSmallIcon(R.drawable.notification_transparent_icon)
                .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getForegroundAppName(): String? {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            currentTime - 1000 * 1000,
            currentTime
        )
        if (usageStats.isEmpty()) return null
        val sortedMap: SortedMap<Long, UsageStats> = TreeMap()
        for (stat in usageStats) {
            sortedMap[stat.lastTimeUsed] = stat
        }
        val recentStats = sortedMap[sortedMap.lastKey()] ?: return null
        return recentStats.packageName
    }
}