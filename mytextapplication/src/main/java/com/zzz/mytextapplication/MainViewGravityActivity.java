package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 文字对齐方式（gravity）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>不做任何事</b>，各种对齐效果全写在
 * {@code activity_main_view_gravity.xml} 里（android:gravity 的不同取值：
 * left / center / right，以及水平与垂直的组合）。
 *
 * <p>顺带记住两个容易混的属性：
 * <ul>
 *   <li>{@code android:gravity} —— 控制<b>控件内部内容</b>（如文字）相对控件自身的对齐；</li>
 *   <li>{@code android:layout_gravity} —— 控制<b>控件自己</b>在父容器里的对齐。</li>
 * </ul>
 *
 * <p>对应布局：{@code res/layout/activity_main_view_gravity.xml}
 */
public class MainViewGravityActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距。没有其它逻辑。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);

        // 加载布局 XML —— 对齐效果全在布局里
        setContentView(R.layout.activity_main_view_gravity);

        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
}