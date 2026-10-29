package com.example.campusnavigator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;
import org.osmdroid.wms.WMSTileSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MapView mapView = null;
    private Button m_btnRoute, btnClear, btnSettings;
    private EditText etStart, etEnd;
    private FloatingActionButton fabMyLocation;
    private LocationManager locationManager;

    private GeoPoint startPoint = null;
    private GeoPoint endPoint = null;
    private GeoPoint currentGPSPoint = null;
    private Marker tempStartMarker = null;
    private Marker tempEndMarker = null;
    private Polyline currentRouteLine = null;

    private Map<String, GeoPoint> poiDatabase = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPoiDatabase();

        mapView = findViewById(R.id.osmMapView);
        m_btnRoute = findViewById(R.id.m_btnRoute);
        btnClear = findViewById(R.id.btn_clear);
        btnSettings = findViewById(R.id.btn_settings); // 绑定设置按钮
        etStart = findViewById(R.id.et_start);
        etEnd = findViewById(R.id.et_end);
        fabMyLocation = findViewById(R.id.fab_mylocation);

        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {});
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        mapView.setMultiTouchControls(true);
        WMSTileSource wmsTileSource = new WMSTileSource(
                "OGC:WMS",
                new String[]{"http://59.175.92.18:40083/geoserver/whu10/wms?service=WMS"},
                "whu10:whu10", "1.1.1", "EPSG:900913", "", 256
        );
        mapView.setTileSource(wmsTileSource);
        mapView.getController().setCenter(new GeoPoint(30.538, 114.3618));
        mapView.setMinZoomLevel(15.0);
        mapView.setMaxZoomLevel(20.0);
        mapView.getController().setZoom(16.0);

        // 🌟 添加可点击的地图标记 (Marker)
//        addClickableStaticMarkers();

        initLocationService();
        initUIEvents();
        setupMapClickListener();
    }

    private void initPoiDatabase() {
        poiDatabase.put("信部食堂", new GeoPoint(30.53526, 114.35985));
        poiDatabase.put("食堂", new GeoPoint(30.53526, 114.35985));
        poiDatabase.put("信息学部图书馆", new GeoPoint(30.53612, 114.36110));
        poiDatabase.put("图书馆", new GeoPoint(30.53612, 114.36110));
        poiDatabase.put("测绘学院", new GeoPoint(30.53717, 114.34880));
    }

    // 🌟 扩展功能：添加可点击的 Marker 图层
    private void addClickableStaticMarkers() {
        Marker libMarker = new Marker(mapView);
        libMarker.setPosition(new GeoPoint(30.53612, 114.36110));
        libMarker.setTitle("信息学部图书馆");
        libMarker.setSnippet("武大信息学部核心自习与阅读空间。点击气泡可设为终点！");
        // 点击标记自带气泡弹出，用户体验极佳
        mapView.getOverlays().add(libMarker);

        Marker teachingBuilding = new Marker(mapView);
        teachingBuilding.setPosition(new GeoPoint(30.53700, 114.36000));
        teachingBuilding.setTitle("信息学部教学楼");
        teachingBuilding.setSnippet("计算机、测绘等专业的主要授课点。");
        mapView.getOverlays().add(teachingBuilding);
    }

    private void initLocationService() {
        try {
            SimpleLocationOverlay overlay = new SimpleLocationOverlay(((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.location)).getBitmap());
            overlay.setLocation(new GeoPoint(30.538, 114.3618));
            mapView.getOverlays().add(overlay);

            LocationListener locationListener = location -> {
                if (location != null) {
                    currentGPSPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    overlay.setLocation(currentGPSPoint);
                }
            };

            // NMEA 监听并传入手写解析器
            OnNmeaMessageListener nmeaListener = (message, timestamp) -> NmeaParser.parse(message);

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 读取用户设置的频率
                SharedPreferences prefs = getSharedPreferences("WHU_Settings", MODE_PRIVATE);
                int freq = prefs.getInt("gpsFreq", 5000);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, freq, 1, locationListener);
                locationManager.addNmeaListener(nmeaListener);
            }
        } catch (Exception e) {}

        fabMyLocation.setOnClickListener(v -> {
            if (currentGPSPoint != null) {
                mapView.getController().animateTo(currentGPSPoint);
            }
        });
    }

    private void initUIEvents() {
        // 跳转到设置界面
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });

        btnClear.setOnClickListener(v -> {
            startPoint = null; endPoint = null;
            etStart.setText(""); etEnd.setText("");
            if(tempStartMarker != null) mapView.getOverlays().remove(tempStartMarker);
            if(tempEndMarker != null) mapView.getOverlays().remove(tempEndMarker);
            if(currentRouteLine != null) mapView.getOverlays().remove(currentRouteLine);
            mapView.invalidate();
        });

        m_btnRoute.setOnClickListener(v -> {
            String startText = etStart.getText().toString().trim();
            String endText = etEnd.getText().toString().trim();

            startPoint = searchPoiOrCoordinates(startText, startPoint, "起点");
            endPoint = searchPoiOrCoordinates(endText, endPoint, "终点");

            // 防空拦截
            if (startPoint == null || endPoint == null) {
                Toast.makeText(this, "坐标缺失！请在地图上点击，或输入存在的地名", Toast.LENGTH_SHORT).show();
                return;
            }

            updateCustomMarker(true, startPoint);
            updateCustomMarker(false, endPoint);

            if (currentRouteLine != null) mapView.getOverlays().remove(currentRouteLine);

            WhuRouteTask whuRouteTask = new WhuRouteTask("http://59.175.92.18:40083/geoserver/whuXX", "whuXX:route", route -> runOnUiThread(() -> {
                try {
                    currentRouteLine = route.getRouteGeometry();
                    mapView.getOverlays().add(currentRouteLine);
                    List<GeoPoint> points = currentRouteLine.getActualPoints();

                    if (points != null && points.size() > 0) {
                        updateCustomMarker(true, points.get(0));
                        updateCustomMarker(false, points.get(points.size() - 1));

                        // 🌟 扩展功能：生成并弹出详细文字导航
                        generateNavigationInstructions(points);
                    }
                    mapView.invalidate();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "未找到连通路径", Toast.LENGTH_SHORT).show();
                }
            }));

            whuRouteTask.setStops(startPoint, endPoint);
            whuRouteTask.solveRouteAsync();
        });
    }

    // 🌟 扩展功能：生成文字导航指令 (BottomSheet)
    private void generateNavigationInstructions(List<GeoPoint> points) {
        SharedPreferences prefs = getSharedPreferences("WHU_Settings", MODE_PRIVATE);
        if (!prefs.getBoolean("showNavDetail", true)) return; // 用户关闭了播报

        // 读取用户设定的身高来推算步长 (身高 * 0.45 = 步长)
        float userHeightCm = prefs.getFloat("userHeight", 170.0f);
        float stepLengthMeters = (userHeightCm / 100.0f) * 0.45f;

        StringBuilder navText = new StringBuilder("🚶 智能步态导航指令：\n\n");
        double totalDistance = 0;

        // 对路线进行简单的方向切片与步数计算
        for (int i = 0; i < points.size() - 1; i += 3) {
            int nextIdx = Math.min(i + 3, points.size() - 1);
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get(nextIdx);

            double dist = p1.distanceToAsDouble(p2);
            totalDistance += dist;
            double bearing = p1.bearingTo(p2);

            // 将角度转为自然语言方位
            String direction = getDirectionString(bearing);
            int steps = (int) (dist / stepLengthMeters);

            navText.append("🔹 向 ").append(direction).append(" 走约 ")
                    .append(steps).append(" 步 (").append(String.format("%.1f", dist)).append(" 米)\n");

            if (nextIdx == points.size() - 1) break;
        }
        navText.append("\n🎯 到达目的地！总里程: ").append(String.format("%.1f", totalDistance)).append(" 米");

        // 使用 Material BottomSheetDialog 弹出
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        ScrollView scrollView = new ScrollView(this);
        TextView tv = new TextView(this);
        tv.setText(navText.toString());
        tv.setTextSize(16f);
        tv.setPadding(50, 50, 50, 50);
        scrollView.addView(tv);
        dialog.setContentView(scrollView);
        dialog.show();
    }

    private String getDirectionString(double bearing) {
        if (bearing >= -45 && bearing < 45) return "北";
        if (bearing >= 45 && bearing < 135) return "东";
        if (bearing >= 135 || bearing < -135) return "南";
        return "西";
    }

    // 优化后的地名解析（加入 Toast 反馈）
    private GeoPoint searchPoiOrCoordinates(String text, GeoPoint tapFallback, String type) {
        if (text.isEmpty()) return tapFallback;

        for (String key : poiDatabase.keySet()) {
            if (key.contains(text) || text.contains(key)) {
                Toast.makeText(this, type + " 匹配到地名: " + key, Toast.LENGTH_SHORT).show();
                return poiDatabase.get(key);
            }
        }

        try {
            String[] parts = text.replace("，", ",").split(",");
            if (parts.length >= 2) {
                return new GeoPoint(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
            }
        } catch (Exception e) {}

        return tapFallback; // 没匹配到也没输入坐标，退回到上次点击的坐标
    }

    private void setupMapClickListener() {
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (startPoint == null || etStart.getText().toString().isEmpty()) {
                    startPoint = p;
                    etStart.setText(String.format(java.util.Locale.US, "%.5f, %.5f", p.getLatitude(), p.getLongitude()));
                    updateCustomMarker(true, p);
                } else {
                    endPoint = p;
                    etEnd.setText(String.format(java.util.Locale.US, "%.5f, %.5f", p.getLatitude(), p.getLongitude()));
                    updateCustomMarker(false, p);
                }
                return true;
            }
            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        };
        mapView.getOverlays().add(new MapEventsOverlay(receiver));
    }

    private void updateCustomMarker(boolean isStart, GeoPoint p) {
        int iconWidth = 15;
        int iconHeight = 15;

        if (isStart) {
            if (tempStartMarker != null) mapView.getOverlays().remove(tempStartMarker);
            tempStartMarker = new Marker(mapView);
            tempStartMarker.setPosition(p);
            Drawable startIcon = ContextCompat.getDrawable(this, R.drawable.ic_start_marker);
            if (startIcon != null) {
                android.graphics.Bitmap bitmap = ((BitmapDrawable) startIcon).getBitmap();
                tempStartMarker.setIcon(new BitmapDrawable(getResources(), android.graphics.Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, true)));
            } else {
                tempStartMarker.setTextIcon("S");
            }
            mapView.getOverlays().add(tempStartMarker);
        } else {
            if (tempEndMarker != null) mapView.getOverlays().remove(tempEndMarker);
            tempEndMarker = new Marker(mapView);
            tempEndMarker.setPosition(p);
            Drawable endIcon = ContextCompat.getDrawable(this, R.drawable.ic_end_marker);
            if (endIcon != null) {
                android.graphics.Bitmap bitmap = ((BitmapDrawable) endIcon).getBitmap();
                tempEndMarker.setIcon(new BitmapDrawable(getResources(), android.graphics.Bitmap.createScaledBitmap(bitmap, iconWidth, iconHeight, true)));
            } else {
                tempEndMarker.setTextIcon("E");
            }
            mapView.getOverlays().add(tempEndMarker);
        }
        mapView.invalidate();
    }
}