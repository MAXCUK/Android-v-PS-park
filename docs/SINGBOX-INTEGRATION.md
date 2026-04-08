# sing-box 集成进度

## 已完成
- 已加入 sing-box 配置生成器骨架
- 已支持按选中节点生成 `config.json`
- 已预留代理运行时管理器 `ProxyRuntimeManager`
- 已为后续 VPN 一键连接做准备

## 当前状态
目前先生成基础配置，方便后续接入真正的 sing-box Android 内核。

## 下一步
1. 对齐 XBoard 返回的真实 VLESS / Shadowsocks 字段
2. 生成完整 TLS / Reality / WS / gRPC 配置
3. 接入 sing-box Android binary 或 service bridge
4. 从 `VpnService` 启动与停止代理
