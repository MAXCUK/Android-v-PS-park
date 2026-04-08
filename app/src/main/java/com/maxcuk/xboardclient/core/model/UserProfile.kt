package com.maxcuk.xboardclient.core.model

data class UserProfile(
    val email: String,
    val transferEnable: Long,
    val usedTransfer: Long,
    val expiredAt: String?
)
