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
import com.maxcuk.xboardclient.R

class XBoardVpnService : VpnService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> startForeground(NOTIFICATION_ID, buildNotification("正在连接代理"))
            ACTION_DISCONNECT -> stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun buildNotification(text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "XBoard VPN",
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
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_CONNECT = "com.maxcuk.xboardclient.action.CONNECT"
        const val ACTION_DISCONNECT = "com.maxcuk.xboardclient.action.DISCONNECT"
        private const val CHANNEL_ID = "xboard_vpn"
        private const val NOTIFICATION_ID = 1001
    }
}
