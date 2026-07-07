package top.e404.eclean.feature.trashcan

import top.e404.eclean.menu.trashcan.TrashInfo

class TrashcanMenuModel(
    private val repository: TrashcanRepository,
) {
    fun entries(): MutableList<TrashInfo> = repository.trashValues
}
