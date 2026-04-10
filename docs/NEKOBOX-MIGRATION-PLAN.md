# NekoBoxForAndroid 迁移接入方案（XBoard 专属客户端）

## 目标
把当前 `xboard-android-client` 从“自建轻量连接壳”调整为：

- **面板与业务层**：保留当前项目已有的 XBoard 专属逻辑
  - 固定面板 `https://ax.ty666.help`
  - 登录
  - `getSubscribe`
  - 自动同步节点
  - 专属品牌 UI
- **连接核心**：尽量复用 `NekoBoxForAndroid` 的成熟能力
  - `libcore`
  - `VpnService`
  - service / 流量 / 状态
  - sing-box 配置生成

## 已确认的关键事实

### 当前 XBoard 真实接口
- 登录：`POST /api/v1/passport/auth/login`
- 用户信息：`GET /api/v1/user/info`
- 节点摘要：`GET /api/v1/user/server/fetch`
- 订阅信息：`GET /api/v1/user/getSubscribe`

### 当前站点真实行为
- `auth_data` 自带 `Bearer ` 前缀
- `/api/v1/user/server/fetch` 只返回节点摘要，不包含完整连接参数
- `/api/v1/user/getSubscribe` 返回 `subscribe_url`
- `subscribe_url`（如 `/s/<token>`）会根据 `User-Agent` 返回不同格式
- 对 `v2rayNG/1.8.0` 会返回 **base64 原始节点链接列表**，适合作为客户端主解析来源

## NekoBox 关键架构结论

### 核心层
- `libcore/`：Go 核心
- `app/src/main/java/io/nekohasekai/sagernet/bg/proto/BoxInstance.kt`
  - 调用 `buildConfig(profile)` 生成 sing-box 配置
  - 再调用 `Libcore.newSingBoxInstance(config.config, LocalResolverImpl)`
- `app/src/main/java/io/nekohasekai/sagernet/bg/VpnService.kt`
  - 成熟 VPN 启动与路由逻辑
- `app/src/main/java/io/nekohasekai/sagernet/bg/BaseService.kt`
  - Service 生命周期与状态控制

### 数据模型层
NekoBox 不是简单把 URL 直接丢给核心，而是：
1. URL / 订阅 → 解析成各协议 Bean
2. Bean 放入 `ProxyEntity`
3. `buildConfig(profile)` 生成 sing-box 配置
4. `Libcore` 启动

关键实体：
- `ProxyEntity`
- `ShadowsocksBean`
- `VMessBean`（`alterId = -1` 表示 VLESS）

## 适合当前项目的集成路线

### 推荐：方案 B（基于 NekoBox 反向定制）
直接基于 NekoBox 的连接底座做专属 XBoard 客户端，比在当前项目里硬嵌核心更稳。

原因：
- NekoBox 已经有成熟 VPN/service/libcore/状态链
- 当前项目已经完成 XBoard 面板侧逻辑验证
- 后续只需要把“XBoard 专属订阅导入”喂给 NekoBox 的 `ProxyEntity + Bean` 模型

## 迁移阶段

### Phase 1：打通模型映射
把当前项目订阅解析结果映射到 NekoBox Bean：

#### SS -> `ShadowsocksBean`
当前可映射字段：
- `server/address` -> `serverAddress`
- `port`
- `method`
- `password`
- `name/remarks` -> `name`

#### VLESS -> `VMessBean` with `alterId = -1`
当前可映射字段：
- `uuid`
- `server/address`
- `port`
- `network`
- `host_header`
- `path`
- `security`
- `sni`
- `flow`
- `public_key`
- `short_id`

注意：
- 在 NekoBox 体系里，VLESS 仍使用 `VMessBean` 承载，`alterId = -1` 代表 VLESS
- 这比当前项目自己维护 `ServerRouteResponse` 再手搓 sing-box 配置更可靠

### Phase 2：做 XBoard 专属导入器
新增一个专属导入层：
- 登录 XBoard
- 调 `getSubscribe`
- 拉 `subscribe_url`
- 用 `v2rayNG/1.8.0` UA 获取原始订阅
- 交给 NekoBox 现成订阅链解析

当前确认的**最小侵入实现**：
- 创建 `ProxyGroup(type = SUBSCRIPTION)`
- 把 `subscribe_url` 写入 `SubscriptionBean.link`
- 把 `customUserAgent` 固定为 `v2rayNG/1.8.0`
- 调用 `GroupUpdater.startUpdate(group, true)`

这样可以直接复用：
- `SubscriptionUpdater`
- `RawUpdater.doUpdate(...)`
- `RawUpdater.parseRaw(...)`
- `ProxyEntity` 持久化
- `ProxyEntity -> buildConfig(profile) -> Libcore` 连接链

这意味着首版未必需要手写完整 `XBoardBeanMapper`，只要确认订阅原文能被 `RawUpdater.parseRaw(...)` 正确识别即可。

### Phase 3：专属壳 UI
保留/新增：
- 专属登录页
- 节点同步页
- 简化首页
- 连接按钮
- 状态页

删除/隐藏：
- 通用机场导入
- 手动填写各种协议表单
- 与 XBoard 专属逻辑无关的高级入口
- 非必要插件管理入口

### Phase 4：收口发布
- release 签名
- 应用名 / 包名 / 图标 / 文案品牌化
- 测试登录 → 同步 → 连接 → 切换节点 → 自动刷新

## 当前项目中建议保留的部分
- `AuthViewModel/AuthScreen` 的固定面板登录交互思路
- `XBoardRemoteDataSource` 的真实接口兼容逻辑
- `SubscriptionParser` 里对当前站点返回格式的理解
- `NodeRefreshWorker/RefreshScheduler` 的自动同步思路

## 当前项目中未来会被替代或弱化的部分
- 自己的 VPN/controller/runtime manager
- 自己维护的轻量节点数据库结构
- 自己手搓 sing-box 运行链

## 下一步实际落地动作
1. 在 NekoBox 仓库中继续定位：
   - group/subscription 存储模型
   - 导入器入口
   - profile 列表刷新机制
2. 选择一种最小侵入接法：
   - 新增 `XBoardSyncManager`
   - 新增 `XBoardLoginActivity/Fragment`
   - 同步后直接写入 `ProxyEntity`
3. 先支持两类节点：
   - Shadowsocks
   - VLESS
4. 首版目标：
   - 能登录
   - 能同步
   - 能显示节点
   - 能直接连接

## 结论
当前最优解不是继续在现有轻量客户端里补全整个连接核心，而是：

**把现有 XBoard 专属逻辑迁移/嫁接到 NekoBox 的成熟连接底座上。**

这条路线更快、更稳、也更接近真正可发布版本。
