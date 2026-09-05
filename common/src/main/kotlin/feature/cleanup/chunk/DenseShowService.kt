package top.e404.eclean.feature.cleanup.chunk

import top.e404.eclean.common.api.CommonPlayer

/**
 * Opens the dense-entity viewer menu for a player.
 */
interface DenseShowService {
    fun show(player: CommonPlayer)
}
