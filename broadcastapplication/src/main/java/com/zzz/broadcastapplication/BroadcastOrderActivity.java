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

import com.zzz.broadcastapplication.receiver.OrderReceiverA;
import com.zzz.broadcastapplication.receiver.OrderReceiverB;

public class BroadcastOrderActivity extends AppCompatActivity {

    private Button btn_send_broadcast_order;

    public static final String ORDER_ACTION = "111";
    private OrderReceiverA orderReceiverA;
    private OrderReceiverB orderReceiverB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_broadcast_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_send_broadcast_order = findViewById(R.id.btn_send_broadcast_order);


        btn_send_broadcast_order.setOnClickListener(v -> {
            Intent intent = new Intent(ORDER_ACTION);
            // 发送广播
            sendOrderedBroadcast(intent, null);
        });

    }


    @Override
    protected void onStart() {
        super.onStart();


        // 多个接收器处理有序广播的顺序规则为
        // 1、优先级越大的接收器，越早收到有序广播
        // 2、优先级相同的时候，越早注册的接收器越早收到广播
        orderReceiverA = new OrderReceiverA();
        // 创建一个意图过滤器，只处理STANDARD_ACTION的广播
        IntentFilter intentFilterA = new IntentFilter(ORDER_ACTION);
        intentFilterA.setPriority(8);
        // 注册接收器，注册之后才能正常接受广播
        registerReceiver(orderReceiverA, intentFilterA);

        orderReceiverB = new OrderReceiverB();
        // 创建一个意图过滤器，只处理STANDARD_ACTION的广播
        IntentFilter intentFilterB = new IntentFilter(ORDER_ACTION);
        intentFilterB.setPriority(10);
        // 注册接收器，注册之后才能正常接受广播
        registerReceiver(orderReceiverB, intentFilterB);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 注销接收器，注销之后不再接受到广播
        unregisterReceiver(orderReceiverA);
        unregisterReceiver(orderReceiverB);

    }
}