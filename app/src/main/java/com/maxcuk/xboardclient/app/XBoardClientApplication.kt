package com.maxcuk.xboardclient.app

import android.app.Application
import com.maxcuk.xboardclient.core.work.RefreshScheduler

class XBoardClientApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        RefreshScheduler.schedule(this)
    }
}
