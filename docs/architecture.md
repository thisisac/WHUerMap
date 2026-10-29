# 技术说明

## 数据流

```text
Android + osmdroid
        │
        ├── WMS：加载校园底图
        └── WFS：提交起终点（Web Mercator）
                         │
                 GeoServer SQL View
                         │
              PostgreSQL/PostGIS/pgRouting
                         │
                  GeoJSON 路线结果
```

客户端把 WGS84 经纬度转换为 EPSG:3857 平面坐标，格式化为普通十进制字符串后拼入 WFS `viewparams`。服务端返回 GeoJSON 后，`WhuRoute` 再将坐标反算为经纬度并构造 osmdroid `Polyline`。

## 客户端模块

- `MainActivity`：初始化地图、请求运行时定位权限、处理地图点击、发起路径规划和显示导航指令。
- `WhuRouteTask`：在线程池中发起 HTTP GET，避免阻塞 Android 主线程。
- `WhuRoute`：读取 `features[].geometry.coordinates` 并生成路线折线。
- `WebMecator`：实现 WGS84 与 Web Mercator 的双向转换。
- `NmeaParser`：解析 `$GPGGA` 与 `$GPRMC` 语句并写入 Logcat。
- `SettingsActivity`：通过 `SharedPreferences` 保存身高、定位频率和导航开关。

## 服务端契约

路线服务需要支持以下参数：

```text
x1=<起点 Web Mercator X>
y1=<起点 Web Mercator Y>
x2=<终点 Web Mercator X>
y2=<终点 Web Mercator Y>
```

返回值至少应包含一个 GeoJSON `FeatureCollection`，其中每个要素的 `geometry.coordinates` 是 `[x, y]` 坐标对。坐标系和图层名称必须与客户端配置一致。

## 已知限制

- POI 数据目前是客户端内置的少量示例，不是完整校园地名库。
- 路线服务地址仍是示例配置，公开部署需要替换为自己的服务。
- 当前导航文字按四个主方向和估算步长生成，不等同于车载/行人级 turn-by-turn 导航。
- GPS、WMS 和 WFS 都依赖网络及设备权限；室内或弱信号环境下定位会明显变差。

