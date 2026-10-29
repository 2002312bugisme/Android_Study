package com.zzz.serviceapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * 可被「其他应用」启动的服务（对应文章「隐式启动」一节）。
 * 对应布局：activity_implicit_start.xml
 * 涉及 API：startService()、setPackage()、PackageManager.queryIntentServices()
 * <p>
 * <b>为什么这个类必须 exported="true"</b><br>
 * 清单里没写 {@code android:exported} 时，默认值取决于有没有 intent-filter：
 * 有 intent-filter 就是 true，没有就是 false。本类为了演示跨应用启动声明了 intent-filter，
 * 所以必须显式写 exported="true"（targetSdk 31+ 不写会直接编译失败）。
 * <p>
 * <b>安全提醒</b><br>
 * exported="true" 意味着设备上任何应用都能启动它。真实项目里必须同时声明
 * {@code android:permission} 做访问控制，否则等于把入口敞开给所有人。
 * <p>
 * <b>隐式启动的两个时代</b><br>
 * Android 4.4 及以前：Intent 里 component 和 package 都为空时，系统只在日志里打一条
 * {@code Log.w("Implicit intents with startService are not safe")} 就放行；<br>
 * Android 5.0 起：同样条件下直接抛 {@code IllegalArgumentException: Service Intent must be explicit}。
 * 两段源码的差异就在 {@code ContextImpl.validateServiceIntent()} 里那个版本判断。
 */
public class RemoteService extends Service {

    /** 日志 TAG */
    private static final String TAG = "ZZZ";

    /**
     * 自定义 action，供调用方做隐式启动。
     * 惯例是写成「包名 + 用途」，避免与其他应用的 action 撞名。
     */
    public static final String ACTION_REMOTE_START = "com.zzz.service.ACTION_REMOTE_START";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "[Remote] onCreate");
    }

    /**
     * 无论调用方用显式还是隐式方式启动，最终都走到这里。
     *
     * @param intent 调用方传入的 Intent
     * @param flags  启动请求的附加信息
     * @param startId 本次启动在当前实例内的序号
     * @return 不需要自动重建
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // getAction() 能看出调用方走的是哪条路：显式启动时为 null，隐式启动时为 ACTION_REMOTE_START
        Log.i(TAG, "[Remote] onStartCommand, action="
                + (intent == null ? "null" : intent.getAction()) + ", startId=" + startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[Remote] onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
