package com.zzz.broadcastapplication;

import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Rational;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 画中画（Picture in Picture）演示页。
 *
 * <p>触发进入画中画有三条路，本页都写了，方便对比：
 * <ol>
 *   <li><b>监听 {@code ACTION_CLOSE_SYSTEM_DIALOGS} 广播</b>（教材做法）：
 *       从 {@code reason} 字段判断是 homekey（回到桌面）还是 recentapps（任务列表）。
 *       ⚠️ 这个广播 <b>Android 12（API 31）起不再发给普通应用</b>，在 targetSdk 37 上收不到，
 *       保留它只是为了对照教材。</li>
 *   <li><b>重写 {@link #onUserLeaveHint()}</b>（现代做法）：
 *       用户主动离开页面（按 Home / 任务键）时由系统回调，是目前唯一可靠的方式。</li>
 *   <li><b>按钮手动进入</b>：不依赖任何系统事件，方便验证画中画本身有没有配好。</li>
 * </ol>
 */
public class ReturnDesktopActivity extends AppCompatActivity {

    private static final String TAG = "ning";

    /** 广播里 reason 字段的两种取值 */
    private static final String REASON_HOME_KEY = "homekey";
    private static final String REASON_RECENT_APPS = "recentapps";

    private DesktopReceiver desktopReceiver;

    private TextView tv_status;

    /** 状态栏文字，正常模式下才看得见（画中画窗口太小） */
    private final StringBuilder statusBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_return_desktop);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_status = findViewById(R.id.tv_status);
        Button btnPip = findViewById(R.id.btn_pip);
        btnPip.setOnClickListener(v -> {
            appendStatus("点了按钮");
            enterPipMode();
        });

        // 注册「系统对话框关闭」广播：按 Home 或任务键时发出
        desktopReceiver = new DesktopReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        // 发送方是系统，所以用 RECEIVER_EXPORTED；系统广播本来就豁免标志检查，写上更明确
        ContextCompat.registerReceiver(
                this, desktopReceiver, filter, ContextCompat.RECEIVER_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注册了就一定要注销，否则 Activity 销毁后接收器还挂在系统里
        if (desktopReceiver != null) {
            unregisterReceiver(desktopReceiver);
            desktopReceiver = null;
        }
    }

    /**
     * 进入画中画模式。
     *
     * <p>三步：① 判断系统版本与当前状态 → ② 配置宽高比 → ③ 调用 enterPictureInPictureMode。
     */
    private void enterPipMode() {
        // Android 8.0（API 26）才开始提供画中画；已经在画中画里就不要重复进
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isInPictureInPictureMode()) {
            // 画中画参数构建器
            PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder();

            // 设置宽高比。Rational(分子, 分母)，10:5 就是宽是高的两倍
            Rational ratio = new Rational(10, 5);
            builder.setAspectRatio(ratio);

            // 进入画中画：当前界面会整体缩成小窗，Activity 不会销毁
            enterPictureInPictureMode(builder.build());
        }
    }

    /**
     * 进入 / 退出画中画时回调。
     *
     * @param isInPictureInPictureMode true = 刚进入，false = 刚退出
     * @param newConfig               变更后的配置
     */
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                              Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            Log.d(TAG, "进入画中画模式");
            appendStatus("进入画中画");
        } else {
            Log.d(TAG, "退出画中画模式");
            appendStatus("退出画中画");
        }
    }

    /**
     * 用户主动离开页面时回调（按 Home、任务键、或切到别的应用）。
     *
     * <p>从 Android 12 起，这是进入画中画的<b>推荐</b>时机 ——
     * 因为 {@code ACTION_CLOSE_SYSTEM_DIALOGS} 已经不再发给普通应用了。
     */
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d(TAG, "onUserLeaveHint");
        appendStatus("onUserLeaveHint");
        enterPipMode();
    }

    /**
     * 往界面状态栏追加一行。
     *
     * @param event 事件描述
     */
    private void appendStatus(String event) {
        statusBuilder.append(event).append('\n');
        if (tv_status != null) {
            tv_status.setText(statusBuilder);
        }
    }

    /**
     * 接收「系统对话框关闭」广播（教材做法，Android 12 起收不到）。
     */
    private class DesktopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                // reason 说明是哪种操作触发的
                String reason = intent.getStringExtra("reason");
                Log.d(TAG, "ACTION_CLOSE_SYSTEM_DIALOGS reason=" + reason);

                if (!TextUtils.isEmpty(reason)
                        && (REASON_HOME_KEY.equals(reason) || REASON_RECENT_APPS.equals(reason))) {
                    appendStatus("收到广播 reason=" + reason);
                    enterPipMode();
                }
            }
        }
    }
}
