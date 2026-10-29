package com.zzz.serviceapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.ForegroundService;

/**
 * 前台服务的调用入口（对应文章第 6 章）。
 * 对应布局：activity_foreground.xml（启动 / 停止两个按钮）
 * 涉及 API：Context.startForegroundService()、Context.startService()、POST_NOTIFICATIONS 运行时权限
 * <p>
 * 关键点：启动前台服务必须用 startForegroundService()，且服务要在 5 秒内调用 startForeground()；
 * 而撤销前台状态恰恰不能走这个入口，否则 5 秒内没调 startForeground() 会被系统判违规。
 */
public class ForegroundActivity extends AppCompatActivity {

    /** 日志 TAG，与其他 demo 保持一致 */
    private static final String TAG = "ZZZ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_foreground);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 先要通知权限，否则后面启动的前台服务通知不会显示
        requestNotificationPermission();

        Button btnStart = (Button) findViewById(R.id.startForeground);
        Button btnStop = (Button) findViewById(R.id.stopForeground);

        btnStart.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            serviceIntent.putExtra("cmd", 0);//0,开启前台服务,1,关闭前台服务
            // minSdk 是 28，已经 ≥ 26，直接用 startForegroundService。
            // 用旧的 startService 启动前台服务，在 App 退到后台时会抛 IllegalStateException
            startForegroundService(serviceIntent);
            Log.d(TAG, "startForegroundService -> cmd=0");
        });


        btnStop.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, ForegroundService.class);
            serviceIntent.putExtra("cmd", 1);//0,开启前台服务,1,关闭前台服务
            // 这里必须用 startService 而不是 startForegroundService：
            // 后者要求 5 秒内必须调用 startForeground()，而 cmd=1 正是要撤销前台状态
            startService(serviceIntent);
            Log.d(TAG, "startService -> cmd=1");
        });
    }

    /**
     * API 33 起，前台服务的通知也需要用户授权。
     * 没授权时服务仍会进入前台状态，但通知栏看不到任何东西，很容易误判成"没生效"。
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            // 请求码只用一次，随便给个常量值即可
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }
    }
}
