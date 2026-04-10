package com.maxcuk.xboardclient.core.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.maxcuk.xboardclient.MainActivity
import com.maxcuk.xboardclient.core.proxy.RuntimeLogRepository
import io.nekohasekai.libbox.TunOptions

class XBoardVpnService : VpnService() {

    private val logRepository by lazy { RuntimeLogRepository(applicationContext) }
    private val boxRunner by lazy { BoxServiceRunner(applicationContext, this, logRepository) }
    private var tunConnection: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                startForeground(NOTIFICATION_ID, buildNotification("正在连接代理"))
                val configPath = intent.getStringExtra(EXTRA_CONFIG_PATH)
                if (configPath.isNullOrBlank()) {
                    logRepository.append("vpn start failed: empty config path")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
                val started = boxRunner.start(configPath)
                if (!started) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return START_NOT_STICKY
                }
            }
            ACTION_DISCONNECT -> {
                boxRunner.close()
                closeTun()
                stopSelf()
            }
        }
        return START_STICKY
    }

    fun startTun(options: TunOptions): Int {
        closeTun()
        val builder = Builder()
            .setSession("星隧互联")
            .setMtu(options.getMTU().takeIf { it > 0 } ?: 1500)

        addAddresses(builder, options)
        addRoutes(builder, options)
        addDns(builder, options)
        addAppRules(builder, options)

        tunConnection = builder.establish() ?: error("建立 VPN TUN 失败")
        logRepository.append("tun established fd=${tunConnection!!.fd}")
        return tunConnection!!.fd
    }

    private fun addAddresses(builder: Builder, options: TunOptions) {
        var added = false
        val ipv4 = options.getInet4Address()
        while (ipv4.hasNext()) {
            val item = ipv4.next()
            builder.addAddress(item.address(), item.prefix())
            added = true
        }
        val ipv6 = options.getInet6Address()
        while (ipv6.hasNext()) {
            val item = ipv6.next()
            builder.addAddress(item.address(), item.prefix())
            added = true
        }
        if (!added) {
            builder.addAddress("172.19.0.1", 30)
        }
    }

    private fun addRoutes(builder: Builder, options: TunOptions) {
        var added = false
        val ipv4 = options.getInet4RouteAddress()
        while (ipv4.hasNext()) {
            val item = ipv4.next()
            builder.addRoute(item.address(), item.prefix())
            added = true
        }
        val ipv6 = options.getInet6RouteAddress()
        while (ipv6.hasNext()) {
            val item = ipv6.next()
            builder.addRoute(item.address(), item.prefix())
            added = true
        }
        if (!added) {
            builder.addRoute("0.0.0.0", 0)
            builder.addRoute("::", 0)
        }
    }

    private fun addDns(builder: Builder, options: TunOptions) {
        val dns = options.dnsServerAddressOrNull()
        if (!dns.isNullOrBlank()) {
            builder.addDnsServer(dns)
        } else {
            builder.addDnsServer("1.1.1.1")
        }
    }

    private fun addAppRules(builder: Builder, options: TunOptions) {
        val includes = mutableListOf<String>()
        val includeIterator = options.getIncludePackage()
        while (includeIterator.hasNext()) includes += includeIterator.next()
        val excludes = mutableListOf<String>()
        val excludeIterator = options.getExcludePackage()
        while (excludeIterator.hasNext()) excludes += excludeIterator.next()

        if (includes.isNotEmpty()) {
            includes.distinct().filter { it != packageName }.forEach {
                runCatching { builder.addAllowedApplication(it) }
            }
        }
        if (excludes.isNotEmpty()) {
            excludes.distinct().forEach {
                runCatching { builder.addDisallowedApplication(it) }
            }
        }
    }

    private fun closeTun() {
        runCatching { tunConnection?.close() }
        tunConnection = null
    }

    override fun onRevoke() {
        boxRunner.close()
        closeTun()
        super.onRevoke()
    }

    override fun onDestroy() {
        boxRunner.close()
        closeTun()
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
