package com.zzz.broadcastapplication;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 锁定屏幕方向演示页。
 *
 * <p>两种锁法，本页都演示了：
 * <ol>
 *   <li><b>清单里写死</b>（本页采用）：
 *       {@code android:screenOrientation="portrait"} —— 这个页面永远是竖屏，
 *       旋转手机时系统直接不理会传感器；</li>
 *   <li><b>代码里设置</b>：{@code setRequestedOrientation(...)} —— 可以在运行时切，
 *       按钮就是这个用法。</li>
 * </ol>
 *
 * <p>⚠️ Android 16（API 36）起，在 {@code sw >= 600dp} 的大屏设备上，
 * 系统会忽略应用的方向锁定（平板/折叠屏不受应用控制）。手机不受影响。
 */
public class LockDirectionActivity extends AppCompatActivity {

    private TextView tv_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lock_direction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_status = findViewById(R.id.tv_status);
        refreshStatus();

        // 代码方式切换锁定方向。切换时会触发一次 Activity 重建（因为方向确实变了），
        // 重建后 onCreate 会重新执行，refreshStatus 读到的就是新的值
        Button btnSwitchLock = findViewById(R.id.btn_switch_lock);
        btnSwitchLock.setOnClickListener(v -> setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
    }

    /** 读当前「请求的方向」并显示。用 getRequestedOrientation 而不是看屏幕实际方向 */
    private void refreshStatus() {
        int orientation = getRequestedOrientation();
        String lockText;
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            lockText = "已锁定：横屏";
        } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            lockText = "已锁定：竖屏";
        } else {
            lockText = "未锁定（跟随系统旋转）";
        }
        tv_status.setText(lockText);
    }
}
