package top.e404.eclean.feature.trashcan

import top.e404.eclean.common.api.CommonPlayer

/**
 * Platform-agnostic trash-can service.
 */
interface TrashcanService {
    val enabled: Boolean
    fun open(player: CommonPlayer)
    fun entries(): List<TrashcanEntryView>
}