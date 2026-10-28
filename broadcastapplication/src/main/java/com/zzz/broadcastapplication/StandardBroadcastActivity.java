package com.zzz.broadcastapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.broadcastapplication.receiver.StandardBroadcastReceiver;

public class StandardBroadcastActivity extends AppCompatActivity {

    private Button btn_send_broadcast;
    private StandardBroadcastReceiver standardBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_standard_broadcast);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_send_broadcast = findViewById(R.id.btn_send_broadcast);


        btn_send_broadcast.setOnClickListener(v -> {
            Intent intent = new Intent(StandardBroadcastReceiver.STANDARD_ACTION);
            // 发送广播
            sendBroadcast(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        standardBroadcastReceiver = new StandardBroadcastReceiver();
        // 创建一个意图过滤器，只处理STANDARD_ACTION的广播
        IntentFilter intentFilter = new IntentFilter(StandardBroadcastReceiver.STANDARD_ACTION);
        // 注册接收器，注册之后才能正常接受广播
        registerReceiver(standardBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 注销接收器，注销之后不再接受到广播
        unregisterReceiver(standardBroadcastReceiver);
    }
}