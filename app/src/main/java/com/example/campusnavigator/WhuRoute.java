package com.example.campusnavigator; // ⚠️注意：修改为你自己的项目实际包名
import android.graphics.Color;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

public class WhuRoute {
    public String m_strRoute;
    private Polyline routeGeometry;

    public WhuRoute(String strRoute) {
        try {
            m_strRoute = strRoute;
            JSONArray featuresArray = new JSONObject(strRoute).getJSONArray("features");

            // 初始化 osmdroid 的折线覆盖物
            routeGeometry = new Polyline();
            routeGeometry.getOutlinePaint().setColor(Color.GREEN);

            for (int i = 0; i < featuresArray.length(); i++) {
                JSONObject jfeature = featuresArray.getJSONObject(i);
                if (jfeature.has("geometry")) {
                    JSONObject geometryObject = jfeature.getJSONObject("geometry");
                    JSONArray pathsArray = geometryObject.getJSONArray("coordinates");

                    for (int j = 0; j < pathsArray.length(); j++) {
                        JSONArray pointArray = pathsArray.getJSONArray(j);

                        // 关键步骤：解析投影坐标并利用 WebMecator 反算为经纬度
                        GeoPoint gpt = WebMecator.webMecatorxy2bl(pointArray.getDouble(0), pointArray.getDouble(1));
                        routeGeometry.addPoint(gpt);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            m_strRoute = e.getMessage();
        }
    }

    public Polyline getRouteGeometry() {
        return routeGeometry;
    }
}