package io.nekohasekai.sagernet.xboard

data class XBoardLoginResponse(
    val data: XBoardAuthData? = null,
    val message: String? = null,
    val status: String? = null,
    val error: String? = null
)

data class XBoardAuthData(
    val auth_data: String? = null,
    val token: String? = null
)

data class XBoardEnvelope<T>(
    val data: T? = null,
    val message: String? = null,
    val status: String? = null,
    val error: String? = null
)

data class XBoardSubscriptionInfo(
    val token: String? = null,
    val subscribe_url: String? = null,
    val email: String? = null,
    val uuid: String? = null
)

data class XBoardUserInfo(
    val email: String? = null,
    val transfer_enable: Long = 0,
    val u: Long = 0,
    val d: Long = 0,
    val expired_at: Long = 0,
    val uuid: String? = null,
    val plan_id: Long = 0
) {
    val usedTraffic: Long get() = u + d
    val remainingTraffic: Long get() = (transfer_enable - usedTraffic).coerceAtLeast(0)
}

data class XBoardSyncResult(
    val groupId: Long,
    val panelName: String,
    val email: String,
    val usedTraffic: Long,
    val totalTraffic: Long,
    val remainingTraffic: Long,
    val expiredAt: Long,
    val planName: String
)
: Long
)
