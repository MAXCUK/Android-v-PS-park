package io.nekohasekai.sagernet.xboard

import io.nekohasekai.sagernet.GroupType
import io.nekohasekai.sagernet.database.GroupManager
import io.nekohasekai.sagernet.database.ProxyGroup
import io.nekohasekai.sagernet.database.SagerDatabase
import io.nekohasekai.sagernet.database.SubscriptionBean
import io.nekohasekai.sagernet.group.GroupUpdater
import io.nekohasekai.sagernet.ktx.applyDefaultValues

object XBoardSyncManager {
    private const val DEFAULT_GROUP_NAME = "星隧互联"
    private const val SUBSCRIPTION_UA = "v2rayNG/1.8.0"

    suspend fun loginAndSync(
        email: String,
        password: String,
        panelName: String = DEFAULT_GROUP_NAME,
        baseUrl: String = XBoardApiClient.DEFAULT_BASE_URL
    ): XBoardSyncResult {
        val client = XBoardApiClient(baseUrl)
        val auth = client.login(email.trim(), password.trim())
        val userInfo = client.getUserInfo(auth)
        val subscription = client.getSubscribe(auth)
        val subscribeUrl = subscription.subscribe_url?.takeIf { it.isNotBlank() }
            ?: error("未获取到订阅链接")

        val finalPanelName = panelName.ifBlank { DEFAULT_GROUP_NAME }
        val group = findOrCreateGroup(finalPanelName, subscribeUrl)
        GroupUpdater.startUpdate(group, true)
        val planName = userInfo.plan_name?.takeIf { it.isNotBlank() }
            ?: userInfo.plan_id.takeIf { it > 0 }?.let { "套餐 #$it" }
            ?: "未识别套餐"
        return XBoardSyncResult(
            groupId = group.id,
            panelName = finalPanelName,
            email = userInfo.email ?: email,
            usedTraffic = userInfo.usedTraffic,
            totalTraffic = userInfo.transfer_enable,
            remainingTraffic = userInfo.remainingTraffic,
            expiredAt = userInfo.expired_at,
            planName = planName
        )
    }

    private suspend fun findOrCreateGroup(panelName: String, subscribeUrl: String): ProxyGroup {
        val existing = SagerDatabase.groupDao.subscriptions().firstOrNull {
            it.subscription?.link == subscribeUrl || it.name == panelName
        }
        if (existing != null) {
            val updated = existing.apply {
                name = panelName
                subscription = (subscription ?: SubscriptionBean()).apply {
                    link = subscribeUrl
                    customUserAgent = SUBSCRIPTION_UA
                    autoUpdate = true
                    autoUpdateDelay = 30
                    deduplication = true
                    forceResolve = false
                    updateWhenConnectedOnly = false
                    applyDefaultValues()
                }
            }
            GroupManager.updateGroup(updated)
            return updated
        }

        return GroupManager.createGroup(
            ProxyGroup(
                name = panelName,
                type = GroupType.SUBSCRIPTION,
                subscription = SubscriptionBean().apply {
                    link = subscribeUrl
                    customUserAgent = SUBSCRIPTION_UA
                    autoUpdate = true
                    autoUpdateDelay = 30
                    deduplication = true
                    forceResolve = false
                    updateWhenConnectedOnly = false
                    applyDefaultValues()
                }
            ).applyDefaultValues()
        )
    }
}
