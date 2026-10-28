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
 * 生命周期观察页。
 *
 * <p>每个生命周期方法都往界面和 Logcat 各写一条，旋转手机就能看清顺序。
 * 关键看两处：
 * <ol>
 *   <li>旋转后顶部的 hashCode 变了 —— 说明 Activity 被销毁重建了（默认行为）；</li>
 *   <li>重建前后的完整顺序：
 *       <pre>
 * 旧实例：onPause → onSaveInstanceState → onStop → onDestroy
 * 新实例：onCreate → onStart → onRestoreInstanceState → onResume
 *       </pre></li>
 * </ol>
 */
public class LifeCycleTestActivity extends AppCompatActivity {

    private static final String TAG = "lifeCycle";

    /**
     * 用 static 保存时间线，这样 Activity 重建后还能看到旧实例留下的记录。
     *
     * <p>⚠️ 仅限演示：static 持有跨度超过 Activity 生命周期的数据容易造成泄漏，
     * 真实项目应该用 ViewModel 或 savedInstanceState 传递状态。
     */
    private static final StringBuilder LOG_BUFFER = new StringBuilder();

    private TextView tv_info;
    private TextView tv_log;
    private ScrollView sv_log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_life_cycle_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_info = findViewById(R.id.tv_info);
        tv_log = findViewById(R.id.tv_log);
        sv_log = findViewById(R.id.sv_log);

        // savedInstanceState 为 null = 首次创建；非 null = 因配置变更/系统回收而重建
        if (savedInstanceState == null) {
            LOG_BUFFER.setLength(0);
        } else {
            LOG_BUFFER.append("-------- 下面是新实例，上面是已被销毁的旧实例 --------\n");
        }

        refreshInfo();
        append("onCreate");
    }

    /** 刷新顶部的实例信息。方向取自 Configuration，旋转后会自动变 */
    private void refreshInfo() {
        tv_info.setText("实例 hashCode = " + hashCode()
                + "\n当前方向 = " + getOrientationText());
    }

    /** 当前屏幕方向的可读文字 */
    private String getOrientationText() {
        return getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE ? "横屏" : "竖屏";
    }

    /**
     * 记一条生命周期事件：同时写 Logcat 和界面时间线。
     *
     * @param event 方法名
     */
    private void append(String event) {
        // hashCode 能区分不同实例，日志里带上它才看得出「换了个对象」
        Log.d(TAG, event + "  instance=" + hashCode());

        // setContentView 之前 tv_log 还是 null，这里要防空
        if (tv_log == null) {
            return;
        }
        LOG_BUFFER.append(event).append('\n');
        tv_log.setText(LOG_BUFFER);
        // 滚到最底部，保证最新一条在可视范围内
        sv_log.post(() -> sv_log.fullScroll(View.FOCUS_DOWN));
    }

    // ==================== 七个标准生命周期 ====================

    @Override
    protected void onStart() {
        super.onStart();
        append("onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        append("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        append("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        append("onStop");
    }

    /** 只有「从 onStop 回到前台」才会走；首次启动和旋转都不会触发 */
    @Override
    protected void onRestart() {
        super.onRestart();
        append("onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        append("onDestroy");
    }

    // ==================== 状态保存与恢复（旋转时的关键） ====================

    /** 在 onStop 之前调用，用来把界面状态存进 Bundle */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        append("onSaveInstanceState");
    }

    /** 在 onStart 之后、onResume 之前调用，把 Bundle 里的状态取回来 */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        append("onRestoreInstanceState");
    }
}
