# 节点字段映射进度

## 当前已映射
### VLESS
- server / host
- port
- uuid
- flow
- security / tls
- sni
- network(ws/grpc)
- path
- host header
- grpc service_name
- reality public_key / short_id / spider_x

### Shadowsocks
- server / host
- port
- method(cipher)
- password

## 当前策略
- 优先兼容常见 XBoard 字段名
- 接口字段不一致时，保留 `rawJson` 方便后续补映射
- 节点刷新时尽量保留已选节点

## 下一步
- 接真实面板响应样例做字段校准
- 增加 ws + tls / reality 的更多边缘参数
- 做订阅链接解析兜底
