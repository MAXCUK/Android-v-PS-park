# API 对接进度

## 已完成骨架
- 登录接口：`POST /api/v1/passport/auth/login`
- 用户信息：`GET /api/v1/user/info`
- 节点拉取：`GET /api/v1/user/server/fetch`
- Session 本地保存
- 节点本地缓存

## 当前假设
由于还没稳定读到服务器项目文件，当前先按常见 XBoard API 结构实现：
- 登录返回 `data.auth_data`
- 用户信息返回 `data`
- 节点列表返回 `data[]`

## 下一步
- 读取你的真实 XBoard 路由与字段名
- 对齐鉴权头格式（可能需要 `Bearer ` 前缀或自定义头）
- 对齐节点字段映射
- 加入订阅解析兜底逻辑


## 已固定的面板信息
- 默认面板域名：`https://ax.ty666.help`
- 客户端目标行为：登录后自动从面板拉取并更新节点
