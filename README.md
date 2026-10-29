# WHUerMap

WHUerMap 是一个面向武汉大学校园路网的 Android 导航电子地图示例。它使用 osmdroid 渲染 GeoServer WMS 底图，通过 WFS/GeoJSON 获取服务端计算的步行路线，并提供实时定位、地图选点、POI 模糊检索和步态文字导航。

> 这是一个课程实验项目的开源整理版。仓库包含移动端客户端，不包含武汉大学校园空间数据库、GeoServer 工作空间或任何课程成绩/个人材料。

## 功能

- GeoServer WMS 校园底图与缩放、拖动
- 地图点击或“纬度,经度”文本选择起点和终点
- 内置少量校园 POI 示例（可在 `MainActivity.java` 中扩展）
- GPS 实时定位与一键回到当前位置
- 通过 WFS 获取 GeoJSON 路径并绘制路线
- Web Mercator（EPSG:3857）正反算
- NMEA GGA/RMC 日志解析
- 根据身高估算步数并显示文字导航指令
- 定位频率、身高和详细导航开关设置

## 运行环境

- Android Studio（建议使用稳定版）
- JDK 11
- Android SDK 36（或与项目配置兼容的 SDK）
- Android 设备/模拟器 API 26+

## 构建与安装

```bash
git clone <your-repository-url>
cd WHUerMap
./gradlew assembleDebug       # macOS/Linux
gradlew.bat assembleDebug    # Windows
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。也可以直接用 Android Studio 打开仓库根目录并运行 `app` 模块。

## 配置地图服务

客户端依赖外部 GeoServer 服务：

1. WMS 图层用于底图显示；
2. WFS 图层用于根据 `x1;y1;x2;y2` 参数返回 GeoJSON 路线；
3. 服务端通常由 PostgreSQL/PostGIS 与 pgRouting 提供空间数据和最短路径计算。

当前示例地址写在 `MainActivity.java` 中，适合课程演示。公开部署时请替换为你自己的 HTTPS GeoServer，并同步修改图层名、坐标系和 WFS `viewparams` 约定。若服务端不可用，地图底图和路径规划将无法正常工作，但应用仍可编译安装。

## 项目结构

```text
app/src/main/java/com/example/campusnavigator/
├── MainActivity.java       # 地图、交互、定位与导航指令
├── SettingsActivity.java   # 用户设置
├── WhuRouteTask.java       # 异步 WFS 请求
├── WhuRoute.java           # GeoJSON 路径解析
├── WebMecator.java         # WGS84/Web Mercator 转换
├── NmeaParser.java         # NMEA GGA/RMC 日志解析
└── WhuRouteListner.java    # 路径回调接口
```

更完整的技术说明见 [`docs/architecture.md`](docs/architecture.md)。课程报告中的个人信息、成绩表、截图和原始 Word/PDF 文件不纳入公开仓库。

## 开源说明

代码以 MIT License 发布。地图数据、GeoServer 服务、OSM 或武汉大学相关空间数据可能受各自许可约束，请在再发布前确认数据来源和使用范围。项目仅用于学习与研究，不保证导航结果适用于安全关键场景。

