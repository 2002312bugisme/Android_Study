package com.zzz.broadcastapplication.receiver;

import static com.zzz.broadcastapplication.BroadcastOrderActivity.ORDER_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OrderReceiverA extends BroadcastReceiver {

    public static final String TAG = "ZZZ-A";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction().equals(ORDER_ACTION)) {
            Log.d(TAG, "onReceive: ");
        }
    }
}
