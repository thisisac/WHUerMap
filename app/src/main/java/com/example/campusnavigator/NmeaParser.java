package com.example.campusnavigator;

import android.util.Log;

public class NmeaParser {
    // 手工解析 NMEA 0183 协议语句
    public static void parse(String nmea) {
        try {
            if (nmea.startsWith("$GPGGA")) {
                // GGA: 提取高精度海拔信息
                String[] parts = nmea.split(",");
                if (parts.length > 9 && !parts[9].isEmpty()) {
                    double altitude = Double.parseDouble(parts[9]);
                    Log.i("NMEA_PARSE", "GGA解析 -> 当前海拔高度: " + altitude + " 米");
                }
            } else if (nmea.startsWith("$GPRMC")) {
                // RMC: 提取速度与真北航向
                String[] parts = nmea.split(",");
                if (parts.length > 8 && !parts[7].isEmpty()) {
                    double speedKnots = Double.parseDouble(parts[7]);
                    double speedKmh = speedKnots * 1.852; // 节转 km/h
                    Log.i("NMEA_PARSE", "RMC解析 -> 当前速度: " + String.format("%.2f", speedKmh) + " km/h");
                }
                if (parts.length > 9 && !parts[8].isEmpty()) {
                    double heading = Double.parseDouble(parts[8]);
                    Log.i("NMEA_PARSE", "RMC解析 -> 当前航向: " + heading + " 度");
                }
            }
        } catch (Exception e) {
            Log.e("NMEA_PARSE", "解析异常: " + e.getMessage());
        }
    }
}