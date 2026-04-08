package com.maxcuk.xboardclient.core.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService

object VpnPermissionHelper {
    fun prepare(context: Context): Intent? = VpnService.prepare(context)
}
