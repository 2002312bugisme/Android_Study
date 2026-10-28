package com.zzz.broadcastapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.broadcastapplication.receiver.AlarmReceiver;

/**
 * 闹钟广播演示：点按钮 → 设一个 1 秒后的闹钟 → 到点由系统发出广播 → 接收器打日志。
 *
 * <p>注意这是「延迟广播」的正确姿势：不要 sleep，也不要用 Handler，
 * 而是交给 {@link AlarmManager}，App 被杀掉后闹钟依然有效。
 */
public class AlarmActivity extends AppCompatActivity {

    /**
     * 闹钟广播的 action。
     *
     * <p>必须用「包名 + 业务名」这种形式，不能用裸的 "alarm" 之类 ——
     * action 是全局字符串，太短容易和别的应用撞车。
     */
    public static final String ALARM_ACTION = "com.zzz.broadcastapplication.ALARM_ACTION";

    /** 延迟多久触发（毫秒） */
    private static final int TRIGGER_DELAY_MILLIS = 1000;

    private Button btn_alarm;

    /** 动态注册的接收器，onStop 要注销，所以提升成成员变量 */
    private AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alarm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_alarm = findViewById(R.id.btn_alarm);
        // 点一下按钮就排一个闹钟
        btn_alarm.setOnClickListener(v -> sendAlarm());
    }

    /**
     * 设置一个延迟触发的一次性闹钟。
     *
     * <p>三要素：
     * <ol>
     *   <li>Intent —— 到点后要发的广播（带 action）；</li>
     *   <li>PendingIntent —— 把这个 Intent 打包交给系统保管，
     *       系统在闹钟到点时代替我们发出去；</li>
     *   <li>AlarmManager —— 真正管定时器的系统服务。</li>
     * </ol>
     */
    private void sendAlarm() {
        Intent intent = new Intent(ALARM_ACTION);

        // PendingIntent.getBroadcast 的四个参数：context、请求码、要发的 Intent、标志位。
        // Android 12（API 31）起必须显式声明可变性，否则直接抛异常：
        //   FLAG_IMMUTABLE  —— 拿到 PendingIntent 的一方不能改里面的 Intent（推荐）
        //   FLAG_MUTABLE    —— 可以被改，只有确实需要外部填充 extras 时才用
        // FLAG_UPDATE_CURRENT 表示已存在同请求码的 PendingIntent 时，用新 Intent 覆盖它
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 从系统服务里取闹钟管理器
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // 触发时刻 = 现在 + 延迟。
        // 不要直接写 1000 —— 那表示「1970 年的时间戳 1 秒」，是个过去的时间点，会立刻触发。
        long triggerAtMillis = System.currentTimeMillis() + TRIGGER_DELAY_MILLIS;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 起为了省电，普通 set() 在 Doze 休眠时不保证准时，
            // setAndAllowWhileIdle 允许在设备空闲时也把广播发出来（系统仍会做频率限制）
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        alarmReceiver = new AlarmReceiver(getApplicationContext());

        // ⚠️ 这里必须把 action 加进 IntentFilter。
        //    空 filter 匹配不到任何广播，闹钟到点了接收器也不会被调用。
        IntentFilter intentFilter = new IntentFilter(ALARM_ACTION);

        // targetSdk 34 起，动态注册非系统广播必须声明导出状态，否则抛 SecurityException：
        //   RECEIVER_EXPORTED     —— 能收到应用外发来的广播
        //   RECEIVER_NOT_EXPORTED —— 只收本应用 sendBroadcast() 发的
        //
        // ⚠️ 这里必须用 EXPORTED，不能用 NOT_EXPORTED。
        //    闹钟广播是 AlarmManager 「代替」我们发出的，实际发送方是系统进程（uid 1000），
        //    对接收器来说属于「外部来源」，NOT_EXPORTED 会直接把它丢掉 ——
        //    表现就是 dumpsys alarm 里能看到闹钟已触发，但接收器一声不响。
        ContextCompat.registerReceiver(
                this, alarmReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 注销前判空：万一 onStart 因为异常没走完，这里 unregister 会抛 IllegalArgumentException
        if (alarmReceiver != null) {
            unregisterReceiver(alarmReceiver);
            alarmReceiver = null;
        }
    }
}
