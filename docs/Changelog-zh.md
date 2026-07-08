## 更新日志

本文件记录此项目的所有重要变更。

格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
并遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/) 版本规范。

## [0.1.3] - 未发布

### 新增
- 在 `plugin.yml` 中添加了 Folia 支持声明。
- 新增基于 `Kaml` 与 `kotlinx.serialization` 的现代多文件配置系统。
- 新增独立的配置模型、配置快照加载、运行时应用入口与重载流程。
- 新增面向 Folia 优先运行时边界的 `RuntimePlatform` 与 `ExecutionGateway` 初始抽象。
- 新增 `chunk-density` 快照与策略定向测试，用于锁定新工作流行为。
- 新增 `drop` 与 `living` 清理策略测试，用于锁定拆分后的保护规则与匹配行为。
- 新增聚焦 `TemporaryReturnService` 的定向测试，覆盖延时回传、覆盖重置与退出回传场景。

### 变更
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

### 说明
- 此版本当前尚未发布。
