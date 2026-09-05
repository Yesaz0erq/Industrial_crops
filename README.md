# 尖端工业化农作物 / Industrial Crops

Minecraft 1.20.1 · Forge 47.4.23 · Java 17

工业农业与自动化模组，包含作物遗传、加工机器、物流储存和能源系统。
同一 JAR 内包含 Industrial Crops、Carrote 和 Carrote Curios 三个模组。

- **Industrial Crops**：工业作物、育种、加工、储存与能源设备。
- **Carrote**：胡罗贝、悖钢、稳态物质、拟态与复制设备。
- **Carrote Curios**：十种胡罗贝饰品，支持主手、副手和 Curios 饰品栏。

## 版本

| Minecraft | 加载器 | Java | 源码 |
|---|---|---|---|
| 1.21.1 | NeoForge | 21 | [main](https://github.com/Yesaz0erq/Industrial_crops/tree/main) |
| 1.20.1 | Forge | 17 | [forge-1.20.1](https://github.com/Yesaz0erq/Industrial_crops/tree/forge-1.20.1) |

## 依赖

GeckoLib 4 为必需依赖。JEI 为可选配方查看支持。
Curios 5.14.1+1.20.1 为可选饰品栏支持；未安装时，饰品仍可通过主手或副手使用。
兼容版本范围见 `gradle.properties` 和 `src/main/resources/META-INF` 中的模组元数据。

## 构建

安装 Java 17，在本目录执行：

```powershell
.\gradlew.bat build
```

Linux / macOS：

```sh
./gradlew build
```

成品位于 `build/libs/industrial-crops-forge-1.0-K1.jar`。
Gradle 会下载构建依赖，首次构建需要联网。
开发客户端使用 `runClient`；可添加 `-PwithoutCurios=true` 检查无 Curios 环境。

## 反馈与许可

提交问题时请附上游戏版本、加载器版本、日志和复现步骤。
[问题反馈](https://github.com/Yesaz0erq/Industrial_crops/issues) · [MIT 许可](LICENSE)
