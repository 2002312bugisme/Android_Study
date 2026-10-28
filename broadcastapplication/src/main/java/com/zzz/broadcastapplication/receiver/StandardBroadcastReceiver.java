package com.zzz.broadcastapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// 定义一个标桩广播的接收器
public class StandardBroadcastReceiver extends BroadcastReceiver {

    public static final String STANDARD_ACTION = "11111";
    public static final String TAG = "ZZZ";

    // 一旦收到标准广播，马上触发接收器的onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals(STANDARD_ACTION)) {
            Log.d(TAG, "onReceive: ");
        }
    }
}
