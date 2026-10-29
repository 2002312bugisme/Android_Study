package com.zzz.serviceapplication.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zzz.serviceapplication.R;

/**
 * 前台服务演示（对应文章第 6 章）。
 * 对应布局：activity_foreground.xml
 * 涉及 API：startForeground()、stopForeground()、NotificationCompat.Builder、NotificationChannel
 * <p>
 * 前台服务与普通启动服务的差别不只是「多一条通知」：
 * 用户在通知栏能看见它，系统在内存不足时也不会优先杀它。
 * 代价是必须把服务做的事明示给用户，不能悄悄占用资源。
 * <p>
 * <b>原文没写、但现在必须处理的三个时代变化</b>
 * <ul>
 *   <li><b>Android 8.0（API 26）</b>：启动前台服务必须用 {@code startForegroundService()}，
 *       并且要在 <b>5 秒内</b>调用 {@code startForeground()}，否则抛
 *       {@code RemoteServiceException: Context.startForegroundService() did not then call
 *       Service.startForeground()}。同时应用进入后台后不能再随意 startService()，
 *       会抛 {@code IllegalStateException}。</li>
 *   <li><b>Android 13（API 33）</b>：通知需要 {@code POST_NOTIFICATIONS} 运行时授权，
 *       没授权时服务仍会进入前台状态，但通知栏什么都看不到，很容易误判成「没生效」。</li>
 *   <li><b>Android 14（API 34）</b>：必须声明 {@code android:foregroundServiceType}
 *       （本例是 dataSync）并申请对应的 {@code FOREGROUND_SERVICE_DATA_SYNC} 权限，
 *       否则 startForeground() 直接抛 {@code MissingForegroundServiceTypeException}。</li>
 * </ul>
 * <b>startForeground() 与 stopForeground() 的语义</b><br>
 * {@code startForeground(id, notification)}：把服务提升为前台，通知常驻且不可滑动删除，
 * 传 0 作为 id 会直接失败。{@code stopForeground(boolean removeNotification)}：
 * 只撤销前台状态（可选是否移除通知），<b>并不会停止服务</b> —— 本类实测验证过这一点。
 */
public class ForegroundService extends Service {

    /** 日志 TAG，与其他 demo 保持一致 */
    private static final String TAG = "ZZZ";

    /**
     * id 不可设置为 0，否则不能设置为前台 service
     */
    private static final int NOTIFICATION_DOWNLOAD_PROGRESS_ID = 0x0001;

    /**
     * 通知渠道 id。API 26 起通知必须归属某个渠道，
     * 不指定渠道的通知不会显示、也不会报错 —— 这是最容易踩的静默失败。
     */
    private static final String CHANNEL_ID = "foreground_demo";

    private boolean isRemove = false;//是否需要移除

    /**
     * 创建通知渠道。重复调用是幂等的，系统只会创建一次。
     */
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                // 渠道名会显示在系统设置的通知管理里
                "前台服务演示",
                // LOW：不响铃、不弹横幅，适合常驻通知；DEFAULT 以上每次都会打扰用户
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("展示前台服务的常驻通知");
        // API 23+ 用 getSystemService(Class) 拿服务，省去强转
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * Notification
     */
    public void createNotification() {
        createChannel();

        // 使用兼容版本；API 26 起必须传渠道 id，原来的单参数构造已废弃
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        // 设置状态栏的通知图标。必须用纯色小图标：
        // 系统只取它的 alpha 当遮罩，所以用 adaptive icon（mipmap-anydpi 的 XML）会显示成一团白块
        builder.setSmallIcon(R.drawable.ic_stat_flash);
        // 设置通知栏横条的图标。BitmapFactory 只能解位图，
        // 所以 screenflash_logo 必须是 PNG，换成 vector 会解出 null，大图标直接不显示
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.screenflash_logo));
        // 禁止用户点击删除按钮删除
        builder.setAutoCancel(false);
        // 禁止滑动删除
        builder.setOngoing(true);
        // 右上角的时间显示
        builder.setShowWhen(true);
        // 设置通知栏的标题内容
        builder.setContentTitle("I am Foreground Service!!!");
        // 创建通知
        Notification notification = builder.build();
        // 设置为前台服务
        startForeground(NOTIFICATION_DOWNLOAD_PROGRESS_ID, notification);
        Log.d(TAG, "startForeground 已调用，通知常驻");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 服务被系统重建时 intent 可能是 null，原来的 intent.getExtras().getInt() 会直接 NPE。
        // getIntExtra 带默认值，能同时解决 null 和缺 key 两种情况
        int cmd = (intent == null) ? -1 : intent.getIntExtra("cmd", -1);
        Log.d(TAG, "onStartCommand cmd=" + cmd + ", startId=" + startId);

        if (cmd == 0) {
            if (!isRemove) {
                createNotification();
            }
            isRemove = true;
        } else if (cmd == 1) {
            //移除前台服务。注意：这只是把服务降级为普通后台服务，服务本身还在跑
            if (isRemove) {
                stopForeground(true);
                Log.d(TAG, "stopForeground 已调用，服务仍在运行");
            }
            isRemove = false;
        }

        // 前台服务靠通知让用户感知，被杀后没必要自动重建；
        // 用 NOT_STICKY 也顺带避开「重建时收到 null Intent」这条分支
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //移除前台服务
        if (isRemove) {
            stopForeground(true);
        }
        isRemove = false;
        // 服务被销毁时通知会跟着消失，这里打一行日志便于对照
        Log.d(TAG, "onDestroy，前台服务已销毁");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
