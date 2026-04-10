package com.maxcuk.xboardclient.core.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.core.app.NotificationCompat
import com.maxcuk.xboardclient.MainActivity
import com.maxcuk.xboardclient.core.proxy.ProxyRuntimeManager

class XBoardVpnService : VpnService() {

    private val runtimeManager: ProxyRuntimeManager by lazy {
        ProxyRuntimeManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                startForeground(NOTIFICATION_ID, buildNotification("正在连接代理"))
                val configPath = intent.getStringExtra(EXTRA_CONFIG_PATH)
                val started = runtimeManager.start(
                    configPath?.let { java.io.File(it) } ?: runtimeManager.runtimeStatus().configPath.let { java.io.File(it) }
                )
                if (!started) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
            ACTION_DISCONNECT -> {
                runtimeManager.stop()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onRevoke() {
        runtimeManager.stop()
        super.onRevoke()
    }

    override fun onDestroy() {
        runtimeManager.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "星隧互联 VPN",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("星隧互联")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_CONNECT = "com.xingsuihulian.app.action.CONNECT"
        const val ACTION_DISCONNECT = "com.xingsuihulian.app.action.DISCONNECT"
        const val EXTRA_CONFIG_PATH = "com.xingsuihulian.app.extra.CONFIG_PATH"
        private const val CHANNEL_ID = "xingsuihulian_vpn"
        private const val NOTIFICATION_ID = 1001
    }
}
