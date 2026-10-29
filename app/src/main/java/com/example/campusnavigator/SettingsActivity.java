package com.example.campusnavigator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        EditText etHeight = findViewById(R.id.et_height);
        EditText etFreq = findViewById(R.id.et_gps_freq);
        Switch swNav = findViewById(R.id.switch_nav_detail);
        Button btnSave = findViewById(R.id.btn_save_settings);

        // 读取已有配置并显示
        SharedPreferences prefs = getSharedPreferences("WHU_Settings", MODE_PRIVATE);
        etHeight.setText(String.valueOf(prefs.getFloat("userHeight", 170.0f)));
        etFreq.setText(String.valueOf(prefs.getInt("gpsFreq", 5000)));
        swNav.setChecked(prefs.getBoolean("showNavDetail", true));

        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            try {
                editor.putFloat("userHeight", Float.parseFloat(etHeight.getText().toString()));
                editor.putInt("gpsFreq", Integer.parseInt(etFreq.getText().toString()));
                editor.putBoolean("showNavDetail", swNav.isChecked());
                editor.apply();
                Toast.makeText(this, "配置已生效！", Toast.LENGTH_SHORT).show();
                finish(); // 关闭页面返回主图
            } catch (Exception e) {
                Toast.makeText(this, "请输入合法的数字", Toast.LENGTH_SHORT).show();
            }
        });
    }
}