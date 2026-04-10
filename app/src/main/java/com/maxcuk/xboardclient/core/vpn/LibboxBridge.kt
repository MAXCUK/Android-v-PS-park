package com.maxcuk.xboardclient.core.vpn

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import com.maxcuk.xboardclient.core.proxy.RuntimeLogRepository
import com.maxcuk.xboardclient.core.proxy.SingBoxRuntimeBridge
import io.nekohasekai.libbox.BoxService
import io.nekohasekai.libbox.InterfaceUpdateListener
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.libbox.LocalDNSTransport
import io.nekohasekai.libbox.NetworkInterfaceIterator
import io.nekohasekai.libbox.Notification
import io.nekohasekai.libbox.PlatformInterface
import io.nekohasekai.libbox.RoutePrefix
import io.nekohasekai.libbox.RoutePrefixIterator
import io.nekohasekai.libbox.SetupOptions
import io.nekohasekai.libbox.StringBox
import io.nekohasekai.libbox.StringIterator
import io.nekohasekai.libbox.TunOptions
import io.nekohasekai.libbox.WIFIState
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object LibboxBridge {
    private val initialized = AtomicBoolean(false)

    fun ensureSetup(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            val bridge = SingBoxRuntimeBridge(context)
            val setup = SetupOptions().apply {
                setBasePath(bridge.runtimeDir().absolutePath)
                setWorkingPath(bridge.workingDir().absolutePath)
                setTempPath(File(context.cacheDir, "libbox-tmp").apply { mkdirs() }.absolutePath)
                setFixAndroidStack(true)
                setUsername("xingsuihulian")
                setIsTVOS(false)
            }
            Libbox.setup(setup)
            runCatching { Libbox.redirectStderr(bridge.logsFile().absolutePath) }
        }
    }
}

class XBoardPlatformInterface(
    private val context: Context,
    private val service: XBoardVpnService,
    private val logRepository: RuntimeLogRepository = RuntimeLogRepository(context)
) : PlatformInterface {

    override fun autoDetectInterfaceControl(fd: Int) {
        service.protect(fd)
    }

    override fun clearDNSCache() = Unit

    override fun closeDefaultInterfaceMonitor(listener: InterfaceUpdateListener) = Unit

    override fun findConnectionOwner(
        ipProtocol: Int,
        sourceAddress: String,
        sourcePort: Int,
        destinationAddress: String,
        destinationPort: Int
    ): Int = 0

    override fun getInterfaces(): NetworkInterfaceIterator = EmptyNetworkInterfaceIterator()

    override fun includeAllNetworks(): Boolean = false

    override fun localDNSTransport(): LocalDNSTransport? = null

    override fun openTun(options: TunOptions): Int {
        logRepository.append("libbox openTun requested")
        return service.startTun(options)
    }

    override fun packageNameByUid(uid: Int): String = if (uid <= 1000) "android" else context.packageName

    override fun readWIFIState(): WIFIState {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        return WIFIState(info.ssid ?: "", info.bssid ?: "")
    }

    override fun sendNotification(notification: Notification) {
        logRepository.append("libbox notification: ${notification.getTitle()} ${notification.getBody()}")
    }

    override fun startDefaultInterfaceMonitor(listener: InterfaceUpdateListener) = Unit

    override fun systemCertificates(): StringIterator = EmptyStringIterator()

    override fun uidByPackageName(packageName: String): Int = if (packageName == context.packageName) android.os.Process.myUid() else 0

    override fun underNetworkExtension(): Boolean = false

    override fun usePlatformAutoDetectInterfaceControl(): Boolean = true

    override fun useProcFS(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q

    override fun writeLog(message: String) {
        logRepository.append(message)
    }
}

class BoxServiceRunner(
    private val context: Context,
    private val service: XBoardVpnService,
    private val logRepository: RuntimeLogRepository = RuntimeLogRepository(context)
) {
    private var boxService: BoxService? = null

    fun start(configPath: String): Boolean {
        LibboxBridge.ensureSetup(context)
        close()
        return runCatching {
            logRepository.append("libbox newService config=$configPath")
            val created = Libbox.newService(configPath, XBoardPlatformInterface(context, service, logRepository))
            created.start()
            boxService = created
            logRepository.append("libbox service started")
            true
        }.getOrElse {
            logRepository.append("libbox service start failed: ${it.message}")
            false
        }
    }

    fun close() {
        runCatching {
            boxService?.close()
            if (boxService != null) logRepository.append("libbox service closed")
        }
        boxService = null
    }
}

private class EmptyStringIterator : StringIterator {
    override fun hasNext(): Boolean = false
    override fun len(): Int = 0
    override fun next(): String = throw NoSuchElementException()
}

private class EmptyNetworkInterfaceIterator : NetworkInterfaceIterator {
    override fun hasNext(): Boolean = false
    override fun next(): io.nekohasekai.libbox.NetworkInterface = throw NoSuchElementException()
}

class ListStringIterator(private val values: List<String>) : StringIterator {
    private var index = 0
    override fun hasNext(): Boolean = index < values.size
    override fun len(): Int = values.size
    override fun next(): String = values[index++]
}

fun TunOptions.dnsServerAddressOrNull(): String? = runCatching { getDNSServerAddress().getValue() }.getOrNull()?.takeIf { it.isNotBlank() }
