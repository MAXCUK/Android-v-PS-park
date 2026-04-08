# XBoard Android Client 架构草案

## 第一阶段目标
- XBoard 登录
- 获取用户信息
- 拉取订阅/节点
- 内置 sing-box 连接 VLESS / Shadowsocks
- 使用 VpnService 实现应用内一键连接

## 模块
- `app`：入口与导航
- `core/model`：领域模型
- `core/network`：XBoard API 对接
- `core/database`：本地缓存
- `core/datastore`：偏好与登录态
- `core/proxy`：sing-box 配置生成与内核调度
- `core/vpn`：VPN 服务
- `feature/auth`：登录
- `feature/home`：连接首页
- `feature/nodes`：节点列表
- `feature/profile`：用户资料与套餐信息

## 下一步
1. 明确你的 XBoard 登录接口与鉴权方式
2. 增加真实 API 数据模型
3. 接入本地数据库缓存节点
4. 补 sing-box Android 集成层
5. 做连接状态机与通知栏前台服务
