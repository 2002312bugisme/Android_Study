package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 外边距（margin）与内边距（padding）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>不做任何事</b>，对比效果全写在
 * {@code activity_main_view_margin_padding.xml} 里。这是最容易混的一对概念：
 * <ul>
 *   <li>{@code layout_margin*} —— <b>外边距</b>：控件与"别人"之间的距离，属于控件外部；</li>
 *   <li>{@code padding*} —— <b>内边距</b>：控件边框与"自己内容（如文字）"之间的距离，属于控件内部。</li>
 * </ul>
 * 判断技巧：给控件加个背景色。背景色覆盖的范围包含 padding，但不包含 margin。
 *
 * <p>对应布局：{@code res/layout/activity_main_view_margin_padding.xml}
 */
public class MainViewMarginPaddingActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距。没有其它逻辑。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);
        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML —— margin / padding 对比全在布局里
        setContentView(R.layout.activity_main_view_margin_padding);
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