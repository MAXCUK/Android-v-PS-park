package com.maxcuk.xboardclient.core.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object RefreshScheduler {
    private const val WORK_NAME = "xboard_node_refresh"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<NodeRefreshWorker>(30, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
