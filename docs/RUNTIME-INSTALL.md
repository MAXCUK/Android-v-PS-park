# sing-box 运行时接入位

## 已完成
- 已创建 `assets/singbox/` 资源目录
- 已创建 `jniLibs/arm64-v8a` / `armeabi-v7a` / `x86_64` 目录
- 已加入 `SingBoxRuntimeBridge`
- 首页会显示当前运行时检测状态
- 连接前会检查运行时是否已安装

## 当前预期
后续需要把 sing-box Android 对应的 native 产物接入项目。

## 目录说明
- `app/src/main/assets/singbox/`：geo 数据和运行资源
- `app/src/main/jniLibs/<abi>/`：native bridge / 可执行文件占位
- `files/singbox-runtime/`：运行期配置与日志输出

## 下一步
1. 放入可用的 sing-box Android 运行时
2. 补真正的启动桥接代码
3. 将连接日志输出到 `singbox.log`
