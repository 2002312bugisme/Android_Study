package com.zzz.broadcastapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.KeyEventDispatcher;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.broadcastapplication.receiver.ShockReceiver;

public class BroadcastStaticActivity extends AppCompatActivity {

    private Button btn_send_broadcast_static;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_broadcast_static);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_send_broadcast_static = findViewById(R.id.btn_send_broadcast_static);

        btn_send_broadcast_static.setOnClickListener(v -> {
            // Android 8.0 之后，清单里注册的接收器收不到大部分「隐式广播」，
            // 要让静态注册的接收器还能收到，就得把广播变成「显式」的 —— 指定包名或组件。
            Intent intent = new Intent(ShockReceiver.ACTION);

            // ⚠️ ComponentName 的第二个参数是「完整类名」，不是包名。
            //    写成 "com.zzz.broadcastapplication.receiver" 会指向一个不存在的类，
            //    广播发出去没有任何接收者，日志里当然什么都没有。
            //    用 ShockReceiver.class 最稳：写错类名编译期就报错。
            ComponentName componentName = new ComponentName(this, ShockReceiver.class);
            intent.setComponent(componentName);
            sendBroadcast(intent);
        });
    }
}