package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 线性布局（LinearLayout）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>不做任何事</b>，布局效果全在
 * {@code activity_main_linear_layout.xml} 里，主要对比两件事：
 * <ul>
 *   <li><b>横向</b>排列时，两个 TextView 各写 {@code layout_width="0dp" + layout_weight="1"}
 *       → 平分整行宽度；颜色不同便于看出边界（purple_600 / purple_200）；</li>
 *   <li><b>纵向</b>排列时，两个 TextView 各写 {@code layout_height="0dp" + layout_weight="1"}
 *       → 平分容器高度。</li>
 * </ul>
 * 记住规则：<b>用 weight 的那一维必须写 0dp</b>，否则权重失去意义。
 *
 * <p>对应布局：{@code res/layout/activity_main_linear_layout.xml}
 */
public class MainLinearLayoutActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);

        // 加载布局 XML —— 权重对比全在布局里
        setContentView(R.layout.activity_main_linear_layout);

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