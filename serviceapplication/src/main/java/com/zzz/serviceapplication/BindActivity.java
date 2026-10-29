package com.zzz.serviceapplication;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.LocalService;

public class BindActivity extends AppCompatActivity {

    protected static final String TAG = "ZZZ";
    private Button btn_bind;
    private Button btn_unbind;
    private Button btn_show;

    /**
     * ServiceConnection代表与服务的连接，它只有两个方法，
     * onServiceConnected和onServiceDisconnected，
     * 前者是在操作者在连接一个服务成功时被调用，而后者是在服务崩溃或被杀死导致的连接中断时被调用
     */
    private ServiceConnection conn;
    private LocalService mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bind);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_bind = findViewById(R.id.btn_bind);
        btn_unbind = findViewById(R.id.btn_unbind);
        btn_show = findViewById(R.id.btn_show);

        //创建绑定对象
        final Intent intent = new Intent(this, LocalService.class);

        btn_bind.setOnClickListener(v -> {
            Log.d(TAG, "绑定调用：bindService");
            //调用绑定方法
            bindService(intent, conn, Service.BIND_AUTO_CREATE);
        });

        btn_unbind.setOnClickListener(v -> {
            Log.d(TAG, "解除绑定调用：unbindService");
            // 解除绑定
            if (mService != null) {
                mService = null;
                unbindService(conn);
            }
        });

        btn_show.setOnClickListener(v -> {
            if (mService != null) {
                // 通过绑定服务传递的Binder对象，获取Service暴露出来的数据

                Log.d(TAG, "从服务端获取数据：" + mService.getCount());
            } else {

                Log.d(TAG, "还没绑定呢，先绑定,无法从服务端获取数据");
            }
        });
        conn = new ServiceConnection() {
            /**
             * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
             * 通过这个IBinder对象，实现宿主和Service的交互。
             */
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "绑定成功调用：onServiceConnected");
                // 获取Binder
                LocalService.LocalBinder binder = (LocalService.LocalBinder) service;
                mService = binder.getService();
            }
            /**
             * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
             * 例如内存的资源不足时这个方法才被自动调用。
             */
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService=null;
            }
        };
    }
}