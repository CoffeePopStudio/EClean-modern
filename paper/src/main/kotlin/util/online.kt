package top.e404.eclean.util

import top.e404.eclean.PL
import top.e404.eclean.common.util.hasOnlinePlayers
import top.e404.eclean.config.Config

// Re-export common utilities for compatibility
val noOnline: Boolean get() = !PL.services.commonPlatform.hasOnlinePlayers()
val noOnlineClean: Boolean get() = Config.current.cleanup.cleanWhenNoPlayers
val noOnlineMessage: Boolean get() = Config.current.cleanup.broadcastWhenNoPlayers
