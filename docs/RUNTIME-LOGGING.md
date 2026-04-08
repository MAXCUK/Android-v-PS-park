# 运行时日志与状态

## 已完成
- 增加 `RuntimeStatus` 结构
- 增加 `RuntimeLogRepository`
- 连接准备 / 启动 / 停止会写入 `singbox.log`
- 首页可显示：
  - 运行时是否存在
  - 运行库路径
  - 日志路径
  - 最新日志行

## 作用
这样后面就算接入真实 sing-box 内核，排错也不用从零开始。

## 下一步
- 将真实 sing-box 启动输出重定向到 `singbox.log`
- 增加“查看完整日志”页面
