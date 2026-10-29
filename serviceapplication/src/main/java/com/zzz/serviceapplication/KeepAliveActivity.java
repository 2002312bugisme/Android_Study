package com.zzz.serviceapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.KeepAliveServiceA;
import com.zzz.serviceapplication.service.KeepAliveServiceB;

/**
 * 双服务互拉保活的实验台（对应文章「保证服务不被杀死的实现思路」）。
 * 对应布局：activity_keep_alive.xml
 * 涉及 API：startService()、stopService()、sendBroadcast()、registerReceiver()
 * <p>
 * 观察重点：
 * 1. 点「启动 A」后 A 和 B 都会出现（A 在 onStartCommand 里拉了 B）；
 * 2. 点「停掉 A」后，日志里依次出现 A.onDestroy → 发出死讯 → B 收到 → 重新 startService(A) → A.onCreate，
 *    A 会被拉回来；
 * 3. 但这个方案只在应用处于前台时有效。App 退到后台后，第 2 步的 startService() 会抛
 *    IllegalStateException；用户在设置里「强行停止」则连 onDestroy 都不会回调，广播根本发不出去。
 */
public class KeepAliveActivity extends AppCompatActivity {

    /** 日志 TAG */
    private static final String TAG = "ZZZ";

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_keep_alive);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvStatus = findViewById(R.id.tv_status);

        // 启动 A，A 的 onStartCommand 会顺带把 B 拉起来
        findViewById(R.id.btn_start_a).setOnClickListener(v -> {
            Log.d(TAG, "[Client] 启动 A");
            startService(new Intent(this, KeepAliveServiceA.class));
            tvStatus.setText("已启动 A（B 应被 A 自动拉起）");
        });

        // 停掉 A。A 在 onDestroy 里会发广播，B 收到后会把 A 重新拉起来
        findViewById(R.id.btn_stop_a).setOnClickListener(v -> {
            Log.d(TAG, "[Client] 停掉 A");
            stopService(new Intent(this, KeepAliveServiceA.class));
            tvStatus.setText("已请求停止 A，看日志里 A 是否被 B 拉回来");
        });

        // 对称操作，验证 B 端也有效
        findViewById(R.id.btn_stop_b).setOnClickListener(v -> {
            Log.d(TAG, "[Client] 停掉 B");
            stopService(new Intent(this, KeepAliveServiceB.class));
            tvStatus.setText("已请求停止 B，看日志里 B 是否被 A 拉回来");
        });
    }
}
