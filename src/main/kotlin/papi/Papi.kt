package top.e404.eclean.papi

import top.e404.eclean.feature.papi.ECleanPapiExpansion

/**
 * Papi扩展
 *
 * - `%eclean_before_next%` - 距离下一次清理的时间, 单位秒
 * - `%eclean_before_next_formatted%` - 距离下一次清理的时间, 格式化的时间
 * - `%eclean_last_drop%` - 上次清理的掉落物数量
 * - `%eclean_last_living%` - 上次清理的生物数量
 * - `%eclean_last_chunk%` - 上次清理的密集实体数量
 * - `%eclean_trashcan_countdown%` - 垃圾桶清理倒计时, 单位秒
 * - `%eclean_trashcan_countdown_formatted%` - 垃圾桶清理倒计时, 格式化的时间
 */
object Papi : ECleanPapiExpansion()
