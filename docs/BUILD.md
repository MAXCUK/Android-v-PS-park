# 打包说明

## 现在已经准备好的内容
- Android 项目基础结构
- Gradle Wrapper 配置文件
- Debug / Release 打包脚本
- local.properties 示例
- sing-box 运行时资源目录

## 后续直接打包前需要确认
1. 机器已安装 Android SDK
2. 项目根目录存在 `local.properties`
3. 如需 Release 正式包，后续补签名配置
4. 如需真正连接节点，后续放入可用的 sing-box Android 运行时

## 常用命令
```bash
./scripts/check-project.sh
./scripts/build-debug.sh
./scripts/build-release.sh
```

## 输出目录
- Debug APK: `app/build/outputs/apk/debug/`
- Release APK: `app/build/outputs/apk/release/`
