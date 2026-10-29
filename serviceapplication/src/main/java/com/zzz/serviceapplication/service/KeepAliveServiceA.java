package com.zzz.serviceapplication.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Service;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * 双服务互拉保活的 A 端（对应文章「保证服务不被杀死的实现思路」一节）。
 * 对应布局：activity_keep_alive.xml
 * 涉及 API：registerReceiver()、sendBroadcast()、startService()
 * <p>
 * <b>文章的方案</b><br>
 * 服务在 onDestroy() 里发一条自定义广播，另一个服务收到后把死掉的那个重新 startService() 起来。
 * 两个服务互相监听，只要还有一个活着，另一个就能被拉回来。
 * <p>
 * <b>本类的分工</b><br>
 * onCreate() 里顺带启动 B，保证两端同时存在；<br>
 * 监听 {@link #ACTION_B_DIED}：B 死了就去把 B 拉起来；<br>
 * onDestroy() 里发 {@link #ACTION_A_DIED}：告诉 B「我死了，来拉我」。
 * <p>
 * <b>为什么这个方案现在基本失效</b><br>
 * 1. 用户在设置里点「强行停止」会直接把进程连根拔掉，<b>不会</b>回调 onDestroy()，
 *    广播根本发不出去；而且应用会被标记为 stopped 状态，连系统广播都收不到；
 * 2. Android 8.0 起，应用处于后台时调用 startService() 会抛 IllegalStateException，
 *    就算广播发出来、对方也收到了，拉活那一步照样失败；
 * 3. Android 12 起对前台服务的后台启动也有额外限制。
 * 也就是说文章这套写法只在「应用还在前台」时才跑得通，真正的保活要靠前台服务 + 通知，
 * 或者干脆改成 WorkManager 这类由系统调度的机制。
 */
public class KeepAliveServiceA extends Service {

    /** 日志 TAG */
    private static final String TAG = "ZZZ";

    /** A 死亡时发出的广播，由 B 接收并负责拉起 A */
    public static final String ACTION_A_DIED = "com.zzz.service.KEEP_ALIVE_A_DIED";

    /** B 死亡时发出的广播，由 A 接收并负责拉起 B */
    public static final String ACTION_B_DIED = "com.zzz.service.KEEP_ALIVE_B_DIED";

    /** 动态注册的接收器，必须在 onDestroy 里反注册 */
    private BroadcastReceiver deathReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "[KeepAliveA] onCreate");

        // 接收「B 已死亡」的通知，收到后立刻把 B 拉起来
        deathReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "[KeepAliveA] 收到 B 的死讯，尝试拉起 B");
                startService(new Intent(KeepAliveServiceA.this, KeepAliveServiceB.class));
            }
        };
        // RECEIVER_NOT_EXPORTED：只接收本应用发出的广播，避免被外部应用伪造
        ContextCompat.registerReceiver(this, deathReceiver,
                new IntentFilter(ACTION_B_DIED), ContextCompat.RECEIVER_NOT_EXPORTED);

        // 拉起 B，形成互保关系。
        // ⚠️ 这一步必须放在 onCreate()，不能放在 onStartCommand()：
        // onCreate 只在实例创建时执行一次，而 onStartCommand 每次 startService() 都会回调，
        // 若在 onStartCommand 里启动对方，对方也会在同一位置启动自己，就是 A→B→A 的死循环。
        startService(new Intent(this, KeepAliveServiceB.class));
    }

    /**
     * @param intent  启动方传入的 Intent
     * @param flags   附加信息
     * @param startId 本次启动的序号
     * @return 不需要自动重建，保活逻辑自己负责
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 这里刻意什么都不做：任何「启动对方」的调用都会引发两端互相唤醒的死循环
        Log.i(TAG, "[KeepAliveA] onStartCommand, startId=" + startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[KeepAliveA] onDestroy，发出死讯请 B 拉起我");
        // 先发广播再反注册：广播是异步投递的，顺序反了可能就发不出去了
        Intent died = new Intent(ACTION_A_DIED);
        // 限定包名，避免这条广播被其他应用收到
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
