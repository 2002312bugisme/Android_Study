package com.zzz.broadcastapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.IntentCompat;

/**
 * 监听系统网络变更广播的接收器（动态注册）。
 */
public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        // 系统在 CONNECTIVITY_CHANGE 里塞了当前网络的 NetworkInfo
        NetworkInfo networkInfo = IntentCompat.getParcelableExtra(intent, "networkInfo", NetworkInfo.class);
        if (networkInfo == null) {
            return;
        }

        String format = String.format(
                "收到一个网络变更广播，网络大类为%s,网络小类为%s,网络制式为%s，网络状况为%s",
                networkInfo.getTypeName(),
                networkInfo.getSubtypeName(),
                getSubtypeText(networkInfo.getSubtype()),
                networkInfo.getState().toString());
        Log.d("ZZZ", "onReceive: " + format);
    }

    /**
     * 把 {@code NetworkInfo#getSubtype()} 返回的数字翻译成可读的网络制式名。
     *
     * <p>为什么不直接打印：{@code getSubtype()} 返回的是 {@code TelephonyManager.NETWORK_TYPE_*}
     * 常量值，本身就是一个 int（比如 LTE 是 13），直接输出只能看到数字。
     *
     * <p>为什么不调 {@code TelephonyManager.getNetworkTypeName(int)}：那个方法是系统内部使用的，
     * 不在公开 SDK 里，调了运行时会报 NoSuchMethodError，所以只能自己映射。
     *
     * <p>另外 {@code getSubtypeName()} 给的是小类名（HSPA、LTE 这种），在 Wi-Fi 下是空字符串；
     * 这里再补一个「制式」维度（2G/3G/4G/5G），两句话看下来信息才完整。
     *
     * @param subtype {@link TelephonyManager} 里的 NETWORK_TYPE_* 常量值
     * @return 可读名称，如 "LTE（4G）"；不认识的返回 "未知（值）"
     */
    private String getSubtypeText(int subtype) {
        switch (subtype) {
            // ---- 2G ----
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS（2G）";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE（2G）";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA（2G）";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT（2G）";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN（2G）";
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM（2G）";

            // ---- 3G ----
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS（3G）";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO 0（3G）";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO A（3G）";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO B（3G）";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA（3G）";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA（3G）";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA（3G）";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+（3G）";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD（3G）";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "TD-SCDMA（3G）";

            // ---- 4G ----
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE（4G）";
            // 19 = NETWORK_TYPE_LTE_CA，这个常量标了 @SystemApi，公开 SDK 里取不到，
            // 只能写字面量（值本身是稳定的）
            case 19:
                return "LTE-CA（4G+）";
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "IWLAN（4G，非蜂窝）";

            // ---- 5G（常量是 API 29 加的，switch 里的常量编译期就内联成数字，低版本不会崩）----
            case TelephonyManager.NETWORK_TYPE_NR:
                return "NR（5G）";

            default:
                // UNKNOWN = 0 会走到这里；Wi-Fi 下也是 0，因为 Wi-Fi 没有「子类型」的概念
                return "未知（" + subtype + "）";
        }
    }
}
