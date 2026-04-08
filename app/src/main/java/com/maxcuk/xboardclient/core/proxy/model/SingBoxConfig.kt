package com.maxcuk.xboardclient.core.proxy.model

data class SingBoxConfig(
    val log: Map<String, Any?>,
    val inbounds: List<Map<String, Any?>>,
    val outbounds: List<Map<String, Any?>>,
    val route: Map<String, Any?>
)
