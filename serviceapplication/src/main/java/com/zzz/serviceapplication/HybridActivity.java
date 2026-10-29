package com.zzz.serviceapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.HybridService;

/**
 * 「启动 + 绑定」结合体的操作台（对应文章第 5 章）。
 * 对应布局：activity_hybrid.xml（start / bind / stop / unbind 四个按钮）
 * 涉及 API：startService()、bindService()、stopService()、unbindService()
 * <p>
 * 四个按钮不预设顺序，由你决定先点哪个，用来观察两种组合的生命周期差异：
 * 组合一（先启动后绑定）：start → bind → stop → unbind
 * 组合二（先绑定后启动）：bind → start → unbind → stop
 */
public class HybridActivity extends AppCompatActivity {

    /** 日志 TAG，服务端也用同一个，方便一行行对照 */
    private static final String TAG = "ZZZ";

    /** 绑定成功后拿到的 Service 实例 */
    private HybridService hybridService;

    /** 是否处于已绑定状态，unbindService 必须先判断 */
    private boolean bound = false;

    /** 界面上只用于回显，不参与业务逻辑 */
    private int startCount = 0;
    private int bindCount = 0;

    private TextView tvState;

    /**
     * 绑定回调。跑在客户端进程的主线程。
     */
    private final ServiceConnection connection = new ServiceConnection() {
        /**
         * @param name    被绑定服务的组件信息
         * @param service 服务端 onBind() 返回的 IBinder
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "[Client] onServiceConnected");
            // 同进程直连，可以安全强转
            HybridService.HybridBinder binder = (HybridService.HybridBinder) service;
            hybridService = binder.getService();
            bound = true;
            updateState();
        }

        /**
         * 只在服务崩溃或被系统杀死时回调，主动解绑不会走这里。
         *
         * @param name 被绑定服务的组件信息
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "[Client] onServiceDisconnected");
            hybridService = null;
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hybrid);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvState = findViewById(R.id.tv_state);

        // 先启动：服务进入启动态，此后即使没有客户端也会一直运行
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            Log.d(TAG, "[Client] 点击 startService");
            startService(new Intent(this, HybridService.class));
            startCount++;
            updateState();
        });

        // 再绑定：BIND_AUTO_CREATE 让服务不存在时自动创建
        findViewById(R.id.btn_bind).setOnClickListener(v -> {
            Log.d(TAG, "[Client] 点击 bindService");
            bindService(new Intent(this, HybridService.class), connection, Context.BIND_AUTO_CREATE);
            bindCount++;
            updateState();
        });

        // 停止：若还有客户端绑着，这个调用不会真正销毁服务
        findViewById(R.id.btn_stop).setOnClickListener(v -> {
            Log.d(TAG, "[Client] 点击 stopService");
            stopService(new Intent(this, HybridService.class));
        });

        // 解绑：若服务曾被 start 过，全部解绑也不会销毁它
        findViewById(R.id.btn_unbind).setOnClickListener(v -> {
            if (bound) {
                Log.d(TAG, "[Client] 点击 unbindService");
                unbindService(connection);
                bound = false;
                hybridService = null;
                updateState();
            } else {
                // 未绑定时调用 unbindService 会抛 IllegalArgumentException，这里直接拦掉
                Log.d(TAG, "[Client] 当前未绑定，unbindService 被忽略");
            }
        });
    }

    /**
     * 把累计次数刷到界面上，便于截图时判断服务此刻的状态。
     */
    private void updateState() {
        tvState.setText("start: " + startCount + " 次，bind: " + bindCount + " 次，"
                + (bound ? "已绑定" : "未绑定"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 不主动解绑会泄漏 ServiceConnection
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }
}
