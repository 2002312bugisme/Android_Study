package com.zzz.serviceapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * 启动态 Service 的生命周期演示（对应文章的「Service 启动服务」一节）。
 * 对应布局：activity_simple.xml
 * 涉及 API：startService()、stopService()、stopSelf()、onStartCommand()
 * <p>
 * <b>一、启动态的本质</b><br>
 * startService() 之后，服务与调用它的组件再无任何关联。调用方（Activity）销毁了，
 * 服务照样在后台跑，直到有人调用 stopService() 或服务自己 stopSelf() 为止。
 * 这也是它和绑定态最根本的区别：绑定态的生命由客户端数量决定，启动态的生命由停止指令决定。
 * <p>
 * <b>二、实测得到的调用顺序</b><br>
 * 首次 startService() → onCreate() → onStartCommand()<br>
 * 再次 startService() → 只有 onStartCommand()（onCreate 只走一次）<br>
 * stopService() → onDestroy()<br>
 * 服务销毁后重新 startService() → onCreate() 会再次执行（"只调一次"仅限同一个实例内）
 * <p>
 * <b>三、onStartCommand() 的三个参数</b>
 * <ul>
 *   <li><b>intent</b>：启动方传入的 Intent，可用来携带参数。服务被系统重建时可能为 null。</li>
 *   <li><b>flags</b>：本次启动请求的附加信息。常见取值有
 *       {@link android.app.Service#START_FLAG_REDELIVERY}（上次返回了 START_REDELIVER_INTENT，
 *       这次是把那个 Intent 重新投递过来）和
 *       {@link android.app.Service#START_FLAG_RETRY}（上一次 onStartCommand 迟迟没返回，系统重试）。</li>
 *   <li><b>startId</b>：本次启动的唯一序号，配合 stopSelfResult(int) 使用可以做到
 *       「只有最后一个启动请求完成时才停止服务」，避免误停。</li>
 * </ul>
 * <p>
 * <b>四、返回值的三种选择（本节最该记住的部分）</b>
 * <ul>
 *   <li><b>START_STICKY</b>：服务因内存不足被 kill 后，系统在内存空闲时重建它并回调
 *       onStartCommand()，但此时 intent 为 <b>null</b>。适合「不执行具体命令、只是长期待命」的服务
 *       （如音乐播放器）。⚠️ 正因为 intent 可能为 null，实现里必须判空。</li>
 *   <li><b>START_NOT_STICKY</b>：被杀后不重建，除非再次 startService()。最安全，
 *       避免系统在不需要的时候又把服务拉起来。</li>
 *   <li><b>START_REDELIVER_INTENT</b>：被杀后重建，并把<b>最后一个非空 Intent</b> 重新投递过来。
 *       适合必须恢复的作业（如文件下载）。</li>
 * </ul>
 * 本例返回的是基类默认值 {@code START_STICKY}，所以上面三种情况都可能在真机日志里遇到。
 */
public class SimpleService extends Service {

    /** 日志 TAG，只过滤本类输出：adb logcat -s SimpleService */
    private static final String TAG = "SimpleService";

    /**
     * 绑定服务时才会调用
     * 必须要实现的方法
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // 启动状态下系统不会调用此方法，这里打日志正好反证「它没有被调用」
        Log.d(TAG, "onBind()");
        return null;
    }

    public SimpleService() {
        super();
    }


    /**
     * 首次创建服务时，系统将调用此方法来执行一次性设置程序（在调用 onStartCommand() 或 onBind() 之前）。
     * 如果服务已在运行，则不会调用此方法。该方法只被调用一次
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 打印线程名：Service 默认跑在宿主进程的主线程，而不是新线程
        Log.d(TAG, "onCreate() thread=" + Thread.currentThread().getName());
    }

    /**
     * 服务销毁时的回调
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }


    /**
     * 每次通过startService()方法启动Service时都会被回调。
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // startId 每次启动都会递增，可用来区分「第几次 startService」
        Log.d(TAG, "onStartCommand() flags=" + flags + ", startId=" + startId);
        // 基类默认返回 START_STICKY，此处不改变行为
        return super.onStartCommand(intent, flags, startId);
    }
}
