package com.zzz.serviceapplication.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 双服务互拉保活的 B 端（与 A 完全对称）。
 * 对应布局：activity_keep_alive.xml
 * <p>
 * onCreate() 里顺带启动 A；监听 {@link KeepAliveServiceA#ACTION_A_DIED}，A 死了就去拉起 A；
 * onDestroy() 里发 {@link KeepAliveServiceA#ACTION_B_DIED}，通知 A 来拉自己。
 * <p>
 * 失效原因见 {@link KeepAliveServiceA} 的类注释，两端是同一套问题。
 */
public class KeepAliveServiceB extends Service {

    /** 日志 TAG */
    private static final String TAG = "ZZZ";

    /** 动态注册的接收器，必须在 onDestroy 里反注册 */
    private BroadcastReceiver deathReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "[KeepAliveB] onCreate");

        // 接收「A 已死亡」的通知，收到后立刻把 A 拉起来
        deathReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "[KeepAliveB] 收到 A 的死讯，尝试拉起 A");
                startService(new Intent(KeepAliveServiceB.this, KeepAliveServiceA.class));
            }
        };
        ContextCompat.registerReceiver(this, deathReceiver,
                new IntentFilter(KeepAliveServiceA.ACTION_A_DIED), ContextCompat.RECEIVER_NOT_EXPORTED);

        // 对称地保证 A 也在跑。同样只能放在 onCreate()，
        // 放到 onStartCommand() 里就会和 A 形成无限互相唤醒。
        startService(new Intent(this, KeepAliveServiceA.class));
    }

    /**
     * @param intent  启动方传入的 Intent
     * @param flags   附加信息
     * @param startId 本次启动的序号
     * @return 不需要自动重建
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 与 A 一样：这里不启动任何服务，避免死循环
        Log.i(TAG, "[KeepAliveB] onStartCommand, startId=" + startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[KeepAliveB] onDestroy，发出死讯请 A 拉起我");
        Intent died = new Intent(KeepAliveServiceA.ACTION_B_DIED);
        died.setPackage(getPackageName());
        sendBroadcast(died);

        if (deathReceiver != null) {
            unregisterReceiver(deathReceiver);
            deathReceiver = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
