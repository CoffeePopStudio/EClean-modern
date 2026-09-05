package top.e404.eclean.common.util

import top.e404.eclean.common.api.Platform

/**
 * Common utilities for checking online players.
 * These delegate to the platform's [ServerInfo].
 */
fun Platform.hasOnlinePlayers(): Boolean =
    serverInfo.hasOnlinePlayers
