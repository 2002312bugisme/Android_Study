package com.zzz.serviceapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.SimpleService;

public class SimpleActivity extends AppCompatActivity implements View.OnClickListener {

    /** 日志 TAG，与 Service 分开，便于在 logcat 里看清「谁先谁后」 */
    private static final String TAG = "SimpleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_simple);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_startService = findViewById(R.id.btn_startService);
        Button btn_stopService = findViewById(R.id.btn_stopService);
        Button btn_gotoBind = findViewById(R.id.btn_gotoBind);
        btn_startService.setOnClickListener(this);
        btn_stopService.setOnClickListener(this);
        btn_gotoBind.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SimpleService.class);
        if (v.getId() == R.id.btn_startService) {
            Log.d(TAG, "点击 startService");
            startService(intent);
        } else if (v.getId() == R.id.btn_stopService) {
            Log.d(TAG, "点击 stopService");
            stopService(intent);
        } else if (v.getId() == R.id.btn_gotoBind) {
            // 绑定态单独一页，避免和启动态的两个按钮混在一起
            Log.d(TAG, "进入绑定态演示");
            startActivity(new Intent(this, BindActivity.class));
        }
    }
}