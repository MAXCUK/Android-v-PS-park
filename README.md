# XBoard Android Client

原生 Android 客户端工作目录。


## 打包准备

项目已经补好基础打包结构，后续可直接使用：

```bash
./scripts/check-project.sh
./scripts/build-debug.sh
```

如果要正式发布包，现在已支持通过 `keystore.properties` 接入签名；真实 sing-box 运行时仍需后续补入。
