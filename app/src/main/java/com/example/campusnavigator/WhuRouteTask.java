package com.example.campusnavigator;

import org.osmdroid.util.GeoPoint;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WhuRouteTask implements Runnable {
    protected String m_strNAServer;
    protected String m_strLayerName;
    protected WhuRouteListner m_routelistner = null;
    protected GeoPoint startPoint;
    protected GeoPoint endPoint;
    protected String strUrl = "";
    protected ExecutorService executorService = Executors.newCachedThreadPool();

    public WhuRouteTask(String strNAServer, String layerName, WhuRouteListner routelistner) {
        m_strNAServer = strNAServer;
        m_strLayerName = layerName;
        m_routelistner = routelistner;
    }

    public void setStops(GeoPoint startGPT, GeoPoint endGPT) {
        startPoint = startGPT;
        endPoint = endGPT;
    }

    public void solveRouteAsync() {
        strUrl = m_strNAServer + "/ows?service=WFS";
        strUrl += "&version=1.0.0";
        strUrl += "&request=GetFeature";
        strUrl += "&maxFeatures=50";
        strUrl += "&typeName=" + m_strLayerName;
        strUrl += "&outputFormat=application/json";

        // 将经纬度转为墨卡托平面坐标
        double[] startxy = WebMecator.webMecatorbl2xy(startPoint);
        double[] endxy = WebMecator.webMecatorbl2xy(endPoint);

        // 强制保留2位小数，防止Java将其转为科学计数法(如 1.27E7)，导致GeoServer拒接服务！
        String x1 = String.format(java.util.Locale.US, "%.2f", startxy[0]);
        String y1 = String.format(java.util.Locale.US, "%.2f", startxy[1]);
        String x2 = String.format(java.util.Locale.US, "%.2f", endxy[0]);
        String y2 = String.format(java.util.Locale.US, "%.2f", endxy[1]);

        String strStops = "&viewparams=x1:" + x1 + ";y1:" + y1 + ";x2:" + x2 + ";y2:" + y2 + ";";
        strUrl += strStops;

        executorService.execute(this);
    }

    @Override
    public void run() {
        StringBuffer resultBuffer = new StringBuffer();
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(strUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.connect();

            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;
            String tempLine = null;

            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }

            try {
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }
            } finally {
                if (reader != null) reader.close();
                if (inputStreamReader != null) inputStreamReader.close();
                if (inputStream != null) inputStream.close();
            }
        } catch (Exception e) {
            resultBuffer.append("other:" + e.toString());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        if (m_routelistner != null) {
            m_routelistner.onRoute(new WhuRoute(resultBuffer.toString()));
        }
    }
}