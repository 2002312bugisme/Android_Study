package com.zzz.broadcastapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zzz.broadcastapplication.AlarmActivity;

/**
 * 闹钟广播的接收器（动态注册，由 AlarmActivity 在 onStart 里注册、onStop 里注销）。
 */
public class AlarmReceiver extends BroadcastReceiver {

    /** 日志标签，用 adb logcat -s ZZZ 过滤 */
    private static final String TAG = "ZZZ";

    /**
     * 保存外部传进来的 Context。
     *
     * <p>⚠️ 其实这个字段是多余的：{@link #onReceive} 的第一个参数就是 Context，
     * 直接用它即可。保留只是为了和示例代码的结构一致；
     * 另外存 Activity 的 Context 到长期对象里容易造成内存泄漏，真要存请存 ApplicationContext。
     */
    private final Context mContext;

    public AlarmReceiver(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 常量放前面判空：广播的 action 有可能为 null
        if (intent != null && AlarmActivity.ALARM_ACTION.equals(intent.getAction())) {
            // onReceive 跑在主线程，且整个方法执行完系统就可能回收本对象，
            // 所以这里只适合打日志/更新 UI 这类轻量操作，不能做耗时任务
            Log.d(TAG, "收到闹钟广播");
        }
    }
}
