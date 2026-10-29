package com.example.campusnavigator;

import org.osmdroid.util.GeoPoint;

public class WebMecator {
    // WGS84 基准面的长半轴，也是 Web 墨卡托投影所采用的地球半径 (对应 MATLAB 中的 R = 6378137.0)
    private static final double R = 6378137.0;

    /**
     * 经纬度转 Web 墨卡托投影 (正算)
     * 对应 geo2mercator.m
     *
     * @param gpt 包含经纬度的 GeoPoint 对象
     * @return 长度为 2 的 double 数组，[0] 为 x (米), [1] 为 y (米)
     */
    public static double[] webMecatorbl2xy(GeoPoint gpt) {
        double[] xy = new double[2];

        // 1. 度转弧度
        double lambda = gpt.getLongitude() * Math.PI / 180.0; // 经度弧度
        double phi = gpt.getLatitude() * Math.PI / 180.0;     // 纬度弧度

        // 2. 正算公式
        double x = R * lambda;
        double y = R * Math.log(Math.tan(Math.PI / 4.0 + phi / 2.0));

        xy[0] = x;
        xy[1] = y;
        return xy;
    }

    /**
     * Web 墨卡托投影转经纬度 (反算)
     * 对应 mercator2geo.m
     *
     * @param x 墨卡托投影 X 坐标 (米)
     * @param y 墨卡托投影 Y 坐标 (米)
     * @return 转换后的 GeoPoint 对象 (纬度, 经度)
     */
    public static GeoPoint webMecatorxy2bl(double x, double y) {
        // 1. 经度反算 (弧度 -> 度)
        double lambda = x / R;
        double lon = lambda * 180.0 / Math.PI;

        // 2. 纬度反算 (弧度 -> 度)
        double phi = 2.0 * Math.atan(Math.exp(y / R)) - Math.PI / 2.0;
        double lat = phi * 180.0 / Math.PI;

        return new GeoPoint(lat, lon);
    }
}