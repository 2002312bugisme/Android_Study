package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 滚动视图（HorizontalScrollView / ScrollView）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>不做任何事</b>，
 * {@code activity_main_scroll_view.xml} 里用两块彩色色块演示两种滚动：
 * <ul>
 *   <li>{@code HorizontalScrollView} + 内部横向 LinearLayout：
 *       两个宽 300dp 的色块总宽超出屏幕，于是可以<b>左右</b>拖动；</li>
 *   <li>{@code ScrollView} + 内部纵向 LinearLayout：
 *       两个高 400dp 的色块总高超出可视区，于是可以<b>上下</b>滚动。</li>
 * </ul>
 *
 * <p>硬性限制（初学最容易踩的坑）：ScrollView 内部<b>只能有一个直接子控件</b>，
 * 所以必须先套一层 LinearLayout 再往里放多个 View；放两个直接子控件会直接崩溃。
 *
 * <p>对应布局：{@code res/layout/activity_main_scroll_view.xml}
 */
public class MainScrollViewActivity extends AppCompatActivity {

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

        // 加载布局 XML —— 两种滚动的结构全在布局里
        setContentView(R.layout.activity_main_scroll_view);

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