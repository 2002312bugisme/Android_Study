package com.zzz.broadcastapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

/**
 * 静态注册的接收器（清单里声明），收到广播后震动。
 *
 * <p>对应的清单声明在 AndroidManifest.xml 里，action 就是下面的 {@link #ACTION}。
 */
public class ShockReceiver extends BroadcastReceiver {

    /** 广播的 action，发送方和接收方必须完全一致 */
    public static final String ACTION = "com.zzz.broadcastapplication.SHOCK";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 常量放前面：intent.getAction() 可能为 null，反过来写会空指针
        if (intent != null && ACTION.equals(intent.getAction())) {
            Log.d("ZZZ", "震动");
            // 从系统服务中获取震动管理器（需要清单里的 VIBRATE 权限）
            Vibrator vb = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vb != null) {
                vb.vibrate(2000);
            }
        }
    }
}
