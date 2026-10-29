package com.zzz.serviceapplication.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * IntentService 演示（对应文章「Service 与 Thread 的区别」一节）。
 * 对应布局：activity_intent_service.xml
 * 涉及 API：IntentService、onHandleIntent()
 * <p>
 * <b>它是什么</b><br>
 * IntentService 是官方给「Service + 后台线程」这个固定套路做的封装：
 * Service 本身跑在主线程，不能做耗时操作；于是每来一个任务就丢进内部的一个工作线程，
 * 处理完自动 stopSelf()，省得你自己开关线程、自己停止服务。
 * <p>
 * <b>它的两条硬限制</b><br>
 * 1. 只有一个 worker thread —— 所有任务串行排队，前一个跑完才轮到下一个。
 *    所以它适合「一次一个」的场景（上传一张图、写一条日志），不适合并发下载。
 * 2. 任务提交后就无法取消，也没有进度回调，执行结果不能回传给调用方。
 * <p>
 * <b>为什么废弃</b><br>
 * Android 11（API 30）起 {@code android.app.IntentService} 被标记为 {@code @Deprecated}，
 * 官方给出的替代方案是 WorkManager（需要保证执行的任务）或
 * 普通 Service + 自己管理线程 / 协程（需要立即执行的任务）。
 * 废弃的直接原因是它依赖后台 Service，而 Android 8.0 起应用进入后台后
 * 系统会限制后台服务的启动与存活，IntentService 的「随时丢任务」前提不再成立；
 * 同时它在 Android 8.0 上对 startForegroundService() 的启动方式支持不好，
 * 容易触发「5 秒内未调用 startForeground」的 ANR。
 * <p>
 * 本类保留下来是为了观察它的执行特征，生产代码不应再使用。
 *
 * @deprecated API 30 起官方废弃，请改用 WorkManager 或 Service + 自行管理的线程。
 */
@Deprecated
public class MyIntentService extends IntentService {

    /** 日志 TAG，与其他 demo 保持一致 */
    private static final String TAG = "ZZZ";

    /** 任务编号，用于在日志里区分是第几个任务 */
    public static final String EXTRA_TASK_ID = "task_id";

    /**
     * 构造函数。
     * 参数只是内部工作线程的名字，会出现在 logcat 的线程名位置，
     * 也是排查问题时区分「哪个 IntentService 在跑」的依据。
     */
    public MyIntentService() {
        super("my-intent-service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "[IntentService] onCreate, thread=" + Thread.currentThread().getName());
    }

    /**
     * 每个任务的处理入口。
     * <p>
     * 关键：这个方法运行在<b>工作线程</b>，不是主线程，所以可以直接 sleep / 读写数据库。
     * 调用方每 startService() 一次，这里就会被调用一次，且严格按提交顺序串行执行。
     *
     * @param intent 调用方传进来的 Intent，可能为 null（服务被系统重建时）
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
        // 打印线程名：三个任务的线程名完全相同，这就是「单 worker 串行」的直接证据
        Log.i(TAG, "[IntentService] 任务 " + taskId + " 开始, thread=" + Thread.currentThread().getName());
        try {
            // 故意睡 1 秒，模拟耗时任务，方便在日志里看出排队效果
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // 恢复中断标记，让上层知道线程被要求退出
            Thread.currentThread().interrupt();
        }
        Log.i(TAG, "[IntentService] 任务 " + taskId + " 结束");
    }

    /**
     * IntentService 内部已经实现了 onBind() 并返回 null，
     * 这里显式重写只是为了让阅读代码的人一眼看到「它不支持绑定」。
     *
     * @param intent 绑定方传入的 Intent
     * @return 恒为 null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 全部任务处理完，IntentService 会自己 stopSelf()，这里能看到销毁日志
        Log.i(TAG, "[IntentService] onDestroy");
    }
}
