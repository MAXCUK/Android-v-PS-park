# 可编译清理

## 本轮已处理
- 补上 Compose plugin 配置
- 调整 NetworkFactory，自动规范 baseUrl
- 修正节点详情页 ViewModel 的重复 collect 风险
- 首页增加运行库 / 日志路径展示
- 统一登录页到当前兼容接口逻辑

## 接下来仍建议实机验证
- 跑一次 `assembleDebug`
- 检查 WorkManager / Room / Compose 版本组合
- 检查 Android 14+ VPN 前台服务行为
