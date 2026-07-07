# EClean Modern
> [!IMPORTANT] 
> 该插件为`EClean Modern`, 由`CoffeePopStudio`组织维护, 插件旨意完善原版`EClean`插件的功能, 解决原版插件在新版本中存在的兼容性问题, 并增加更多功能和配置选项

## 支持设置

- 清理间隔
- 清理前通知
- 忽略的世界
- 生物/实体/掉落物类型匹配(支持正则)
- 设置拴绳拴住/乘骑中/捡起物品的实体是否清理
- 密集实体检测

## 指令

> 插件主命令为`/eclean`，包括缩写`/ecl`，如果`/ecl`与其他插件冲突，请使用`/eclean`

- `/eclean reload` 重载插件, 重载后计划清理的任务将重新开始计时
- `/eclean clean` 立刻执行一次清理(不显示清理前提示，在有玩家的服务器中慎用)
- `/eclean clean entity` 立刻执行一次实体清理(不显示清理前提示)
- `/eclean clean entity <世界名>` 立刻在指定世界执行一次实体清理(不显示清理前提示)
- `/eclean clean drop` 立刻执行一次掉落物清理(不显示清理前提示)
- `/eclean clean drop <世界名>` 立刻在指定世界执行一次掉落物清理(不显示清理前提示)
- `/eclean clean chunk` 立刻执行一次密集实体清理(不显示清理前提示)
- `/eclean clean chunk <世界名>` 立刻在指定世界执行一次密集实体清理(不显示清理前提示)
- `/eclean entity <实体名>` 统计当前世界每个区块的指定实体
- `/eclean entity <实体名> <世界名>` 统计指定世界每个区块的指定实体
- `/eclean entity <实体名> <世界名> <纳入统计所需数量>` 统计指定世界每个区块的指定实体并隐藏不超过指定数量的内容
- `/eclean stats` 统计当前所在世界的实体和区块统计
- `/eclean stats <世界名>` 统计实体和区块统计
- `/eclean trash` 打开垃圾桶
- `/eclean show` 打开密集实体统计信息菜单

## 权限

- `eclean.admin` 使用插件指令
- `eclean.trash` 打开垃圾桶

## PlaceholderAPI

- `%eclean_before_next%` - `距离下一次清理的时间, 单位秒`
- `%eclean_before_next_formatted%` - `距离下一次清理的时间, 格式化的时间`
- `%eclean_last_drop%` - `上次清理的掉落物数量`
- `%eclean_last_living%` - `上次清理的生物数量`
- `%eclean_last_chunk%` - `上次清理的密集实体数量`
- `%eclean_trashcan_countdown%` - `垃圾桶清理倒计时, 单位秒`
- `%eclean_trashcan_countdown_formatted%` - `垃圾桶清理倒计时, 格式化的时间`

## 配置

插件默认配置已拆分到 `src/main/resources/config/` 目录下的多文件模板中，配置项均带注释描述用法和含义

## 下载
- [最新版](https://github.com/CoffeePopStudio/EClean-modern/releases/latest)

## 更新记录
详见 [release](https://github.com/CoffeePopStudio/EClean-modern/releases)

