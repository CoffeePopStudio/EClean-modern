# EClean Modern
> [!IMPORTANT] 
> 该插件为`EClean Modern`, 由`CoffeePopStudio`组织维护, 插件旨意完善原版`EClean`插件的功能, 解决原版插件在新版本中存在的兼容性问题, 并增加更多功能和配置选项

## Why
> [!NOTE]
> 原EClean插件[在Folia支持的issue](https://github.com/4o4E/EClean/issues/39)中
> 
> 明确说明`没有时间, 现在没有任何对folia的支持计划`, 而且Eplugin框架是基于旧版bukkit api开发, 而并非paper api
> 
> 这也就会导致很多api在paper中标记`Deprecated`甚至`Removed`, 这也就导致了原版插件可能在未来版本中无法使用
> 
> 该插件的开发目的就是为了在Folia环境下支持EClean插件的功能, 并且在原版插件的基础上增加更多功能和配置选项

## 指令

> 插件主命令为`/eclean`，包括缩写`/ecl`，如果`/ecl`与其他插件冲突，请使用`/eclean`

- `/eclean reload` 重载插件, 重载后计划清理的任务将重新开始计时
- `/eclean clean` 立刻执行一次清理(不显示清理前提示，在有玩家的服务器中慎用)
- `/eclean clean --preview` 预览一次清理，不真正删除实体
- `/eclean clean entity` 立刻执行一次实体清理(不显示清理前提示)
- `/eclean clean entity <世界名>` 立刻在指定世界执行一次实体清理(不显示清理前提示)
- `/eclean clean drop` 立刻执行一次掉落物清理(不显示清理前提示)
- `/eclean clean drop <世界名>` 立刻在指定世界执行一次掉落物清理(不显示清理前提示)
- `/eclean clean chunk` 立刻执行一次密集实体清理(不显示清理前提示)
- `/eclean clean chunk <世界名>` 立刻在指定世界执行一次密集实体清理(不显示清理前提示)
- `/eclean entity <实体名>` 统计当前世界每个区块的指定实体
- `/eclean entity <实体名> <世界名>` 统计指定世界每个区块的指定实体
- `/eclean entity <实体名> <世界名> <纳入统计所需数量>` 统计指定世界每个区块的指定实体并隐藏不超过指定数量的内容
- `/eclean entity <实体名> <世界名> <区块X> <区块Z>` 查看指定区块内该类型的实体列表
- `/eclean stats` 统计当前所在世界的实体和区块统计
- `/eclean stats <世界名>` 统计实体和区块统计
- `/eclean stats gui [世界名]` 打开统计 GUI
- `/eclean status all` 统计全服所有世界的实体/区块
- `/eclean status <世界名>` 查看单个世界状态
- `/eclean history [数量]` 查看最近清理记录
- `/eclean top entity [数量] [世界名]` 查看实体数量最多的类型
- `/eclean top chunk [数量] [世界名]` 查看实体数量最多的区块
- `/eclean trash` 打开垃圾桶
- `/eclean trash stats` 查看垃圾桶统计信息(每个聚合条目的类型/数量/剩余时间)
- `/eclean show` 打开密集实体统计信息菜单
- `/eclean tp <世界名> <x> <y> <z>` 直接传送到指定坐标(管理员)

> 统计功能支持聊天栏点击：
> - `/eclean stats` 里的实体类型可点击，查看该类型在各个区块的分布
> - `/eclean entity` 里的区块行可点击，查看该区块内该类型的所有实体
> - 实体坐标可点击，点击后直接传送

## 权限

- `eclean.admin` 使用插件指令
- `eclean.trash` 打开垃圾桶

## PlaceholderAPI

- `%eclean_before_next%` - `距离下一次清理的时间, 单位秒`
- `%eclean_before_next_formatted%` - `距离下一次清理的时间, 格式化的时间`
- `%eclean_last_drop%` - `上次清理的掉落物数量`
- `%eclean_last_living%` - `上次清理的生物数量`
- `%eclean_last_chunk%` - `上次清理的密集实体数量`
- `%eclean_trashcan_countdown%` - `最早到期条目的剩余时间(条目永不过期时为空桶, 为0), 单位秒`
- `%eclean_trashcan_countdown_formatted%` - `最早到期条目的剩余时间, 格式化的时间`
- `%eclean_total_entities%` - `全服实体总数`
- `%eclean_total_chunks%` - `全服已加载区块总数`
- `%eclean_world_<世界名>_entities%` - `指定世界的实体总数`

## 配置

插件默认配置已拆分到 `src/main/resources/config/` 目录下的多文件模板中，配置项均带注释描述用法和含义

## 下载
- [最新版](https://github.com/CoffeePopStudio/EClean-modern/releases/latest)

## 更新记录
详见 [ChangeLog](https://github.com/CoffeePopStudio/EClean-modern/blob/modern/docs/Changelog.md)

