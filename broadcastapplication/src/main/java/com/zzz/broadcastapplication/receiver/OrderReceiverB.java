package com.zzz.broadcastapplication.receiver;

import static com.zzz.broadcastapplication.BroadcastOrderActivity.ORDER_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OrderReceiverB extends BroadcastReceiver {

    public static final String TAG = "ZZZ-B";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals(ORDER_ACTION)) {
            Log.d(TAG, "onReceive: ");
            abortBroadcast();//中断广播，此时后面的接收器无法收到广播
        }

    }
}
