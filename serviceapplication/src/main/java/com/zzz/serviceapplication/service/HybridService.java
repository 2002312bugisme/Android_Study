package com.zzz.serviceapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 「启动 + 绑定」结合体演示（对应文章第 5 章）。
 * 对应布局：activity_hybrid.xml
 * 涉及 API：startService()、bindService()、stopService()、unbindService()
 * <p>
 * 这个服务同时实现了启动态和绑定态需要的全部回调，用来观察两种方式混用时的生命周期顺序。
 * 核心结论（本该由实测验证）：
 * 1. 整个系统里只存在一个 Service 实例，start 和 bind 操作的是同一个对象；
 * 2. 只要还有客户端绑着，stopService()/stopSelf() 都不会真正销毁服务；
 * 3. 反过来，只要服务曾被 startService() 启动过，全部解绑也不会销毁它。
 */
public class HybridService extends Service {

    /** 日志 TAG，与其他 demo 保持一致 */
    private static final String TAG = "ZZZ";

    /** 绑定态的通行证，与 onBind() 返回的必须是同一个对象 */
    private final HybridBinder binder = new HybridBinder();

    /**
     * Binder 子类，客户端通过 getService() 拿回 Service 实例。
     */
    public class HybridBinder extends Binder {
        /**
         * @return 当前 Service 实例
         */
        public HybridService getService() {
            return HybridService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // hashCode 用来证明「start 和 bind 拿到的是同一个实例」
        Log.i(TAG, "[Hybrid] onCreate  hash=" + hashCode());
    }

    /**
     * 每次 startService() 都会回调，绑定不会触发它。
     *
     * @param intent  启动方传入的 Intent
     * @param flags   启动请求的附加信息
     * @param startId 本次启动在当前实例内的序号
     * @return 服务被杀后的重建策略
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "[Hybrid] onStartCommand  startId=" + startId);
        // 结合体不适合自动重建：重建后既没有客户端绑着、也没有新的 start 请求，语义会乱
        return START_NOT_STICKY;
    }

    /**
     * 客户端 bindService() 时回调，多次绑定只有第一次触发。
     *
     * @param intent 客户端绑定时传入的 Intent
     * @return Binder 实例
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[Hybrid] onBind");
        return binder;
    }

    /**
     * 所有客户端解绑时回调。
     * 注意：此时服务不一定销毁 —— 如果它还被 startService() 启动着，会继续运行。
     *
     * @param intent 最后一次绑定传入的 Intent
     * @return false 表示重新绑定时不会回调 onRebind()，本例不需要
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "[Hybrid] onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[Hybrid] onDestroy  hash=" + hashCode());
    }
}
