package com.zzz.broadcastapplication;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 配置变更演示页：旋转手机时 Activity <b>不重建</b>，只走 onConfigurationChanged。
 *
 * <p>效果全靠清单里这一行：
 * <pre>
 * android:configChanges="orientation|screenLayout|screenSize|keyboard"
 * </pre>
 *
 * <p>⚠️ 只写 {@code orientation} 是不够的：Android 3.2（API 13）之后，
 * 横竖屏切换同时会改变 screenSize，漏声明它 Activity 照样会重建。
 * 这也是为什么四个属性要一起写。
 *
 * <p>验证方法：旋转手机后，界面上的 hashCode 不变、日志里没有第二次 onCreate，
 * 只多出一条 onConfigurationChanged —— 说明是「就地处理」而不是「销毁重建」。
 */
public class ChangeDirectionActivity extends AppCompatActivity {

    private static final String TAG = "changeDirection";

    private TextView tv_direction;
    private TextView tv_log;
    private ScrollView sv_log;

    /** 时间线内容。本页不会重建，所以用普通成员变量就够了，不需要 static */
    private final StringBuilder logBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_direction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_direction = findViewById(R.id.tv_direction);
        tv_log = findViewById(R.id.tv_log);
        sv_log = findViewById(R.id.sv_log);

        refreshDirection();
        append("onCreate（hashCode = " + hashCode() + "）");
    }

    /**
     * 配置发生变更时由系统回调。
     *
     * <p>只有清单里声明了对应的 configChanges，这个方法才会被调用；
     * 没声明的话，配置一变系统就把 Activity 销毁重建，根本轮不到这里。
     *
     * @param newConfig 变更后的完整配置对象（方向、屏幕尺寸、字体比例都在里面）
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Log.d(TAG, "onConfigurationChanged: orientation=" + newConfig.orientation
                + "  instance=" + hashCode());

        // 重建没发生，但界面需要自己按新配置刷新一次——系统不会帮你重跑 onCreate
        refreshDirection();
        append("onConfigurationChanged → 方向变为" + getOrientationText());
    }

    /** 刷新方向文字 */
    private void refreshDirection() {
        tv_direction.setText("当前方向：" + getOrientationText());
    }

    /** 当前屏幕方向的可读文字 */
    private String getOrientationText() {
        return getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE ? "横屏" : "竖屏";
    }

    /**
     * 往界面时间线追加一条。
     *
     * @param event 事件描述
     */
    private void append(String event) {
        Log.d(TAG, event);
        logBuilder.append(event).append('\n');
        tv_log.setText(logBuilder);
        sv_log.post(() -> sv_log.fullScroll(View.FOCUS_DOWN));
    }
}
