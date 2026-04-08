package com.maxcuk.xboardclient.core.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maxcuk.xboardclient.app.XBoardClientApplication
import com.maxcuk.xboardclient.core.network.XBoardRemoteDataSource
import com.maxcuk.xboardclient.core.network.NetworkFactory

class NodeRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return runCatching {
            val container = (applicationContext as XBoardClientApplication).container
            val session = container.authRepository.currentSession() ?: return Result.retry()
            val remote = XBoardRemoteDataSource(NetworkFactory.create(session.baseUrl))
            val servers = remote.fetchServers(session.authToken)
            container.nodeRepository.replaceNodes(servers)
            Result.success()
        }.getOrElse { Result.retry() }
    }
}
