## 更新日志

本文件记录此项目的所有重要变更。

格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
并遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/) 版本规范。

## [0.1.8] - 未发布

### 变更
- 用直接 `JavaPlugin` 继承替换 `EPlugin` 基类——所有 EPlugin 功能（debugPrefix、prefix、debug、debuggers、bstats）在 EClean 中自实现。
- 删除 `MLangHost`——EPlugin 的 `langManager` 类型要求已不存在，lang 模块中最后一个 eplugin import 消除。
- 优化配置重载为 section 级 diff——仅在 `cleanup`/`perWorld` 变更时重启清理 ticker，在 `trashcan` 变更时重启垃圾桶 ticker，不再全量重建。

### 新增
- DropCleanupService、LivingCleanupService、ChunkDensityScanner 集成测试（MockBukkit，端到端流水线验证）。

#### 零 eplugin 依赖
插件 `src/main/kotlin/` 中已无任何 `import top.e404.eplugin` 语句。

## [0.1.7]

### 变更
- 提取独立 `ui/` 菜单框架（UiMenu、UiButton、UiPager、UiDisplayable、util），替换所有 `eplugin.menu` 依赖。
- 将 DenseMenu 和 TrashcanMenu 从 ChestMenu/MenuButton/MenuButtonZone 迁移至 UiMenu/UiButton/UiPager。
- 用独立 Bukkit `Listener` 注册替换基于 `EMenuManager` 的 MenuManager。
- 用原生 `me.clip.placeholderapi.expansion.PlaceholderExpansion` 替换 eplugin PlaceholderAPI Hook（EHookManager、PlaceholderAPIHook、PapiExpansion）。
- 将 `MLang` 与 `ELangManager` 解耦——现使用独立 YAML 加载，通过薄层 `MLangHost` shim 仅保证 EPlugin 兼容性。
- eplugin import 从 31 条减至 3 条（EPlugin ×2、ELangManager ×1）。

## [0.1.6]

### 新增
- 新增 Adventure MiniMessage 消息系统，支持从旧版 `&c` 颜色代码自动迁移——`LegacyLangMigrator` 在首次载入时检测 `&` 代码，重命名为 `lang.old.yml`，转换为 MiniMessage 格式后删除备份文件。
- 新增 `/ecl clean --preview` dry-run 预览模式——仅汇报将清理的内容，不实际移除任何实体。
- 新增按世界独立 `per-world.yml` 配置——每个世界可覆盖 `intervalSeconds` 或设置 `enabled: false` 禁用该世界清理。

### 变更
- 用独立 `MLang` 单例替换基于 `ELangManager` 的 `Lang.kt`（消息加载零 eplugin 依赖）。
- `MessageService.send()` 和 `.broadcast()` 改为通过 `MiniMessage.deserialize()` 输出 Adventure `Component`。
- `CleanupTickService` 从单个全局 ticker 扩展为按世界 `Map<String, ScheduledTask>`。
- 配置消息默认值（倒计时、完成提示）转为 MiniMessage 格式。
- 移除 `String.color` 和 `String.removeColor()` 扩展函数——MiniMessage 不再使用 `§` 颜色代码。

## [0.1.5]

### 变更
- 用单个原生 Bukkit `CommandExecutor` + `TabCompleter`（`Commands.kt`）替换 `ECommand`/`ECommandManager` 框架，8 个子命令（debug, reload, clean, stats, entity, trash, players, show）改为内联 private handler 方法。
- 移除 `SchedulerFacade`/`FoliaSchedulerFacade`/`PaperSchedulerFacade` 抽象层，改为 `Schedulers` 单例——直接封装 Bukkit 的 Folia 兼容调度器 API（`GlobalRegionScheduler`、`RegionScheduler`、`AsyncScheduler`、`EntityScheduler`），Paper 1.21+ 已内置 polyfill。
- `SchedulerHandle` 全面替换为 `io.papermc.paper.threadedregions.scheduler.ScheduledTask`。
- `RuntimeServices` 不再创建 scheduler 实例；`isSchedulerReady` 守卫已移除。

## [0.1.4]

### 新增
- 新增 `xyz.jpenilla.run-paper` Gradle 插件，提供 `runFolia` 和 `runServer` 任务，一键启动本机集成测试服务器。

### 变更
- 将 `parseSecondAsDuration` 从 eplugin 迁移为 `util/Text` 中的独立 `Long` 扩展函数。
- 移除 `EListener` 依赖：`DespawnListener` 和 `Trashcan` 改为直接实现 `Listener`，由 `EClean.onEnable` 通过 `Bukkit.getPluginManager().registerEvents` 注册。
- 移除 `AbstractDebugCommand` 依赖：`Debug` 改为普通 `ECommand`，内联 debugger 管理逻辑。
- 移除 `EUpdater` 依赖：`Update` 改为使用 `java.net.http.HttpClient` + `JsonParser` 自实现 GitHub releases API 检查，通过 `AsyncScheduler` 定时调度。
- 将 `run/` 目录加入 `.gitignore`（`gradlew runFolia` / `runServer` 生成的测试服务器产物）。

### 修复
- 修复 Folia 上 `chunk.isForceLoaded` 在 region 线程读取导致的 `IllegalStateException`——强制加载计数现改为在 global tick 线程收集。
- 修复 Folia 26.1 (`Moonrise`) 下 `Thread failed main thread check: Async chunk retrieval`——改为在 region 线程阶段捕获 live `Chunk` 引用，global 线程不再调用 `getChunkAt`。
- 将所有控制台输出（`info`、`debug`、`buildDebug`、`warn`）改为通过 `plugin.logger` 发出，使日志正确写入 `logs/latest.log` 并遵循服务器日志级别（替换 `Bukkit.getConsoleSender().sendMessage()`）。
- 将 `debug`/`info`/`warn` 中所有硬编码中文字符串替换为英文，避免不支持 UTF-8 的终端出现乱码。
- `broadcast()` 不再将玩家公告同步推送到控制台日志。

## [0.1.3]

### 新增
- 在 `plugin.yml` 中添加了 Folia 支持声明。
- 新增基于 `Kaml` 与 `kotlinx.serialization` 的现代多文件配置系统。
- 新增独立的配置模型、配置快照加载、运行时应用入口与重载流程。
- 新增面向 Folia 优先运行时边界的 `RuntimePlatform` 与 `ExecutionGateway` 初始抽象。
- 新增 `chunk-density` 快照与策略定向测试，用于锁定新工作流行为。
- 新增 `drop` 与 `living` 清理策略测试，用于锁定拆分后的保护规则与匹配行为。
- 新增 `WorldStatsService`、`WorldStatsCollector`、`WorldStatsResult` 统计组件，基于 `ChunkTaskCoordinator` 实现 Folia-safe chunk-by-chunk 分布收集与聚合。
- 新增聚焦 `TemporaryReturnService` 的定向测试，覆盖延时回传、覆盖重置与退出回传场景。
- 新建独立的 `util/Text`（`color`, `formatAsConst`, `placeholder` 扩展函数）和 `app/MessageService`（消息、广播、debug、日志门面），替换 20+ 个业务文件中对 `EPlugin.Companion.*` 和 `PL.sendMsgWithPrefix/debug/info/warn` 的直接依赖。

### 变更
- 将项目与插件标识从 `EClean` 统一更名为 `EClean-Modern`。
- 将插件目标 API 更新为 Paper API `26.1.2`。
- 将默认配置模板拆分为 `src/main/resources/config/` 下的多文件结构。
- 更新了测试与 Gradle 配置，以适配新的配置工作流。
- 更新了 `MockBukkit` 与相关测试依赖，使其与当前 Paper API 版本线对齐。
- 修复了测试套件，使 `gradlew test` 再次通过。
- 更新了 Shadow 插件与构建生命周期配置，使基于 Java 25 的 `shadowJar` 打包重新通过。
- 更新了 `README`、Folia 适配设计 spec 与实现计划文档，补充当前 `Folia` 优先运行定位和实现状态说明。

### 重构
- 移除了基于 `eplugin config` 的旧配置层，改为项目内部的配置系统。
- 将清理逻辑、垃圾桶、命令、监听器与更新检查中的运行时配置读取迁移到新的配置门面。
- 重构运行时启动流程，使 `RuntimeServices` 注入平台感知的执行服务。
- 将清理与垃圾桶的运行时生命周期收拢到 `RuntimeServices`，使插件启用、重载、停用统一复用同一组启动、重载与关闭入口。
- 精简 `CleanupCoordinator` 注入，只保留其实际拥有的依赖，并让配置运行时应用流程不再直接耦合旧的 `Clean.schedule()` 调用。
- 将 `FoliaSchedulerFacade` 从反射式调度器查找切换为直接的 Paper/Folia 调度 API，同时继续通过 `SchedulerFacade` 统一插件任务取消入口。
- 将 `chunk-density` 清理重构为 `planner`、`snapshotter`、`policy`、`cleaner`、`report` 小组件，同时保持现有入口兼容。
- 将 `drop` 与 `living` 清理重构为 `planner`、`collector`、`policy`、`executor`、`report` 小组件，同时保持现有入口兼容。
- 抽离 `PlayerTeleportService` 与 `TemporaryReturnService`，并让 `DenseZone` 与 `MenuManager` 改为依赖新服务，同时保持玩家侧菜单行为不变。
- 将 `ChunkTaskCoordinator` 从 `cleanup` 专属包迁移到 `platform/dispatch/`，供清理与统计功能共用。
- 将 `Planner` 层 (`*Planner.planWorlds`) 改为返回世界的 `String` 名称或 `ChunkRef` 列表，不再返回 live `World` / `Chunk` 对象，避免下游跨 region 持有。
- 为 `Collector` 层新增 `collectFromChunk(chunk)` 方法，使实体/物品收集从「整世界遍历」改为「单 chunk 收集」。
- 将 `DropCleanupService`、`LivingCleanupService`、`ChunkDensityScanner` 改为注入 `SchedulerFacade`，通过 `ChunkTaskCoordinator` 做 region-safe 的逐 chunk 清理分发。
- 重写 `clean/*.kt` 清理入口：移除 `Bukkit.getWorlds()` 重复逻辑，改为委托 Service 层；对外保留简单重载但内部为异步回调式，适配 Folia 的 chunk-by-chunk 模型。
- 将 `CleanupCoordinator.cleanNow()` 改为链式异步回调，确保 drop → living → chunk 顺序完成。
- 将 `DenseZone` 内的 chunk 实体遍历与 remove 操作包进 `regionScheduler`，并将 `getHighestBlockYAt` 迁入对应 chunk region。
- 将 `command/check.kt` 中的 `world.entities` / `world.loadedChunks` 统计收集与结果发送收口到 `globalRegionScheduler`。
- 将 `command/Players.kt` 中的玩家位置读取改为按玩家 entity scheduler 分发收集，使用异步聚合。
- 将 `command/Clean.kt` 按世界清理改为每次新建 transient Service 实例，并通过回调返回清理结果。
- 将 `command/Show.kt` 的 `scanDenseEntries` 改为注入 `SchedulerFacade` 的异步回调版本。
- 向 `RuntimeServices` 增加 `isSchedulerReady` 属性，供各清理入口安全校验 `SchedulerFacade` 是否已注入。

