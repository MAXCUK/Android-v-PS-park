# 真实接口对接笔记

## 已探到的线上信息
- `ax.ty666.help` 当前是站点别名，实际虚拟主机命中：`z.aoixx.com`
- Nginx / 面板配置里能确认该别名存在
- 服务器上未在 `/www/wwwroot` 常规 Laravel 目录浅层直接发现 XBoard 项目文件，说明部署路径可能不是默认结构，或者走了其它发布方式

## 已做的兼容
- 增加 `GET /api/v1/guest/comm/config` 预加载站点配置
- 登录 token 同时兼容：
  - `data.auth_data`
  - `data.token`
- 用户信息/节点拉取同时兼容两种 Authorization 头：
  - 直接传 token
  - `Bearer <token>`

## 还差的最后校准
- 真实节点 JSON 样例
- 真实登录响应样例
- 真实站点部署目录（若要继续从服务器源码反查）
