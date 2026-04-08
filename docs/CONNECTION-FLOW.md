# 一键连接流程

## 已完成骨架
1. 用户登录 XBoard
2. 自动从面板拉取用户信息和节点
3. 用户在节点页选择节点
4. 首页点击“一键连接”
5. 生成本地 sing-box 配置
6. 启动 `XBoardVpnService`

## 当前状态
- 连接按钮、状态流、VPN Service 占位、配置落盘都已串起来
- 目前还差真正的 sing-box 内核启动

## 下一步
- 请求 `VpnService.prepare()` 权限
- 接入真实 sing-box 运行时
- 将节点字段完整映射到 sing-box 配置
