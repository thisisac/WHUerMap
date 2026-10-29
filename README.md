# WHUerMap

武汉大学校园导航电子地图 Android 客户端
Android campus navigation map for Wuhan University

WHUerMap 是一个面向校园步行导航的 Android 开源示例。客户端使用
`osmdroid` 渲染地图，使用 GeoServer WMS 获取校园底图，使用 GeoServer
WFS 获取服务端计算的 GeoJSON 路线，并在设备端完成定位、选点、路线绘制
和文字导航提示。

WHUerMap is an open-source Android client for pedestrian navigation in a
campus environment. It uses `osmdroid` for map rendering, GeoServer WMS for
the basemap, GeoServer WFS for route GeoJSON, and Android location APIs for
position updates.

## 中文说明

### 项目背景

通用城市地图通常无法完整表达校园内部的小路、台阶、教学楼入口和校园
POI。WHUerMap 将移动端地图显示、校园空间数据服务和步行路径规划组合起来，
用于学习和验证一套轻量级 GIS/LBS 应用的完整链路。

仓库只包含 Android 客户端代码和文档，不包含武汉大学校园空间数据库、
GeoServer 配置、课程报告原件或个人信息。运行路线功能需要自行部署并配置
兼容的 GeoServer 服务。

### 核心功能

- 加载 GeoServer WMS 校园底图，支持拖动、双指缩放和地图点击。
- 通过地图点击或 `纬度,经度` 文本选择起点和终点。
- 使用内置示例 POI 进行简单模糊匹配，例如“信部食堂”“图书馆”等。
- 使用 Android `LocationManager` 获取 GPS 位置，并在地图上更新定位点。
- 使用悬浮定位按钮快速回到当前 GPS 位置。
- 通过 WFS `GetFeature` 请求获取服务端计算的路线。
- 将 WGS84 经纬度转换为 Web Mercator（EPSG:3857）请求坐标。
- 解析 GeoJSON 路线并绘制绿色 `Polyline`。
- 解析 NMEA GGA/RMC 语句并将海拔、速度和航向写入 Logcat。
- 根据用户身高估算步长，在底部弹窗中显示方向、距离和步数。
- 保存身高、定位频率和详细导航开关等用户设置。

### 系统架构

```text
Android 客户端 / Android client
        │ WMS basemap       │ WFS route request
        ▼                   ▼
  GeoServer WMS       GeoServer WFS / SQL View
                              │
                  PostgreSQL + PostGIS + pgRouting
                              │
                       GeoJSON route result
```

一次路线规划的完整数据流：

1. 用户点击地图或输入经纬度，客户端得到起点和终点的 WGS84 坐标。
2. `WebMecator` 将两个点转换为 EPSG:3857 的米制坐标。
3. `WhuRouteTask` 将坐标格式化为普通十进制字符串，拼接到 WFS
   `viewparams=x1:...;y1:...;x2:...;y2:...;` 中。
4. GeoServer 将请求传给 SQL View；SQL View 使用 PostGIS/pgRouting
   的道路网络计算步行路径。
5. 服务端返回 GeoJSON `FeatureCollection`。
6. `WhuRoute` 读取 `features[].geometry.coordinates`，将投影坐标反算为
   经纬度并创建 osmdroid `Polyline`。
7. `MainActivity` 更新地图覆盖物，并根据路线点生成文字导航。

### 客户端模块

| 文件 | 职责 |
| --- | --- |
| `MainActivity.java` | 地图初始化、WMS 配置、权限、定位、地图选点、POI 匹配、路线显示和导航弹窗。 |
| `SettingsActivity.java` | 读取和保存身高、GPS 更新频率、导航详情开关。 |
| `WhuRouteTask.java` | 后台构造并发送 WFS HTTP GET 请求，完成网络读取。 |
| `WhuRoute.java` | 解析服务端 GeoJSON，将路线封装为 `Polyline`。 |
| `WhuRouteListner.java` | 路线请求完成后的回调接口。 |
| `WebMecator.java` | WGS84 与 Web Mercator 的正反算工具。 |
| `NmeaParser.java` | 解析 `$GPGGA` 和 `$GPRMC` 诊断语句。 |
| `activity_main.xml` | 地图、起终点输入框、操作按钮和定位悬浮按钮。 |
| `activity_settings.xml` | 用户配置页面。 |

### 关键实现细节

#### WMS 底图

`MainActivity` 使用 `WMSTileSource` 配置 GeoServer WMS 地址、图层名、
瓦片大小和投影。默认地图中心位于武汉大学附近，缩放范围为 15 到 20。
公开部署时应替换示例地址，并确认服务端支持客户端声明的坐标系。

#### 地图选点与 POI 匹配

`MapEventsOverlay` 捕获单击事件。第一次点击填入起点，第二次点击填入终点，
并分别显示起点、终点图标。文本输入优先尝试内置 POI 模糊匹配；无法匹配时，
再尝试解析 `纬度,经度` 格式。

#### 坐标转换

Web Mercator 正算为：

```text
x = R × λ
y = R × ln(tan(π / 4 + φ / 2))
R = 6378137.0 m
```

反算为：

```text
longitude = x / R × 180 / π
latitude  = (2 × atan(exp(y / R)) - π / 2) × 180 / π
```

路线请求中的大数值坐标使用 `Locale.US` 和 `%.2f` 格式化，避免 Java
默认输出科学计数法后导致服务端参数解析失败。

#### 异步路线请求

`WhuRouteTask` 实现 `Runnable`，通过 `ExecutorService` 执行网络请求，
设置连接和读取超时，并通过 `WhuRouteListner` 回调结果。路线解析完成后，
`MainActivity` 使用 `runOnUiThread` 更新地图，避免在 Android 主线程执行网络
访问和大量坐标处理。

#### 步态导航提示

路线点被按间隔采样，客户端根据相邻点距离和方位角生成“向北/东/南/西走
约 N 步”的文本。默认步长估算为：

```text
stepLength = heightInMeters × 0.45
```

该功能是路线摘要，不是高精度 turn-by-turn 导航，不应作为安全关键场景的
唯一导航依据。

#### NMEA 解析

`NmeaParser` 对 `$GPGGA` 提取海拔，对 `$GPRMC` 提取速度和航向，并写入
Logcat。它用于理解 GNSS 原始语句和调试，不替代 Android 系统定位融合。

### 构建与运行

#### 环境要求

- Android Studio 稳定版。
- JDK 11；Android Studio 自带 JDK 21 也可用于当前 Gradle 构建。
- Android SDK 36。
- Android 设备或 API 26 以上模拟器。
- 能访问 WMS/WFS 服务的网络环境。

#### 构建 Debug APK

```bash
git clone <your-repository-url>
cd WHUerMap

# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

APK 输出路径为 `app/build/outputs/apk/debug/app-debug.apk`。

#### Android Studio 运行

1. 用 Android Studio 打开仓库根目录。
2. 等待 Gradle 同步完成。
3. 连接 Android 设备或启动模拟器。
4. 运行 `app` 模块。
5. 首次启动时允许定位权限。

### 服务端配置

客户端依赖 WMS 校园底图和 WFS 路线服务。WFS 至少需要接受：

```text
x1=<起点 Web Mercator X>
y1=<起点 Web Mercator Y>
x2=<终点 Web Mercator X>
y2=<终点 Web Mercator Y>
```

返回结构至少需要包含 GeoJSON `FeatureCollection` 和
`features[].geometry.coordinates` 坐标对。服务地址和图层名目前写在
`MainActivity.java` 与 `WhuRouteTask.java` 中，适合课程演示。正式部署时
建议抽取到配置文件，并启用 HTTPS、访问控制、请求限流和服务端日志。

### 已知限制

- 仓库不包含校园道路数据库和 GeoServer 配置。
- 内置 POI 只是少量示例，不是完整校园地名搜索服务。
- 路线计算由外部服务完成，服务不可用时无法规划路线。
- 当前导航提示只使用四个主方向和估算步数。
- 室内、树荫、建筑密集区域可能出现 GPS 漂移。
- 示例代码仍包含课程实验时代的服务地址，公开部署前必须替换。

### 隐私与安全

应用使用定位权限更新设备位置；当前客户端没有账号系统，也不会把位置上传
到本仓库。路线请求会把起终点坐标发送到你配置的 GeoServer，因此部署服务时
应明确数据保留、访问控制和日志策略。

### 许可证

本项目使用 MIT License，详见 [`LICENSE`](LICENSE)。地图数据、底图服务、道路
网络和 POI 数据可能拥有独立的版权或使用条件，重新发布前请分别确认。

## English Documentation

### Overview

WHUerMap is an Android campus navigation client for pedestrian routing around
Wuhan University. The repository contains the mobile client and documentation.
It does not ship the campus spatial database, GeoServer workspace, course
reports, or personal information.

The project demonstrates a lightweight GIS/LBS workflow: map display, location
updates, interactive point selection, projected-coordinate conversion,
asynchronous WFS requests, GeoJSON parsing, route rendering, and walking
instructions.

### Architecture

The Android app is the presentation and interaction layer. GeoServer publishes
the basemap through WMS and exposes a route layer through WFS. The route layer
can be backed by PostgreSQL/PostGIS and pgRouting. The client sends projected
start/end coordinates, while the server performs network analysis and returns
GeoJSON.

The main components are:

- `MainActivity`: map lifecycle, permissions, location updates, point selection,
  POI matching, route rendering, and navigation text.
- `SettingsActivity`: persistent preferences through `SharedPreferences`.
- `WhuRouteTask`: background HTTP request and callback delivery.
- `WhuRoute`: GeoJSON route parsing and osmdroid polyline creation.
- `WebMecator`: WGS84 and EPSG:3857 conversion utility.
- `NmeaParser`: diagnostic parsing for GGA and RMC NMEA sentences.

### End-to-End Route Flow

1. The user selects or types two WGS84 points.
2. `WebMecator` projects them to Web Mercator meters.
3. `WhuRouteTask` builds a WFS `GetFeature` request and formats coordinates
   with fixed decimal notation.
4. GeoServer passes the request to a SQL View or equivalent route service.
5. PostgreSQL/PostGIS/pgRouting computes a path over the pedestrian network.
6. GeoServer returns a GeoJSON `FeatureCollection`.
7. `WhuRoute` converts coordinates back to WGS84 and creates a `Polyline`.
8. `MainActivity` updates the map and optionally displays walking instructions.

### Build and Run

Prerequisites are Android Studio, JDK 11 or the bundled Android Studio JDK 21,
Android SDK 36, Android API 26 or newer, and a reachable WMS/WFS backend.

```bash
git clone <your-repository-url>
cd WHUerMap

# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
Open the root directory in Android Studio to run the `app` module.

### Backend Contract

The route service must accept `x1`, `y1`, `x2`, and `y2` in Web Mercator meters
and return a GeoJSON feature collection containing coordinate pairs. Update the
service URL, layer names, projection settings, and SQL View parameter contract
before deploying outside the original course environment.

### Contributing and License

Please read [`CONTRIBUTING.md`](CONTRIBUTING.md) before opening an issue or pull
request. The project is released under the MIT License. External map, network,
and POI datasets may have separate licenses.
