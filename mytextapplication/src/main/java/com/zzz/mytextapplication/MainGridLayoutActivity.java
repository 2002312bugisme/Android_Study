package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 网格布局（GridLayout）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>不做任何事</b>，
 * {@code activity_main_grid_layout.xml} 里用 4 个彩色 TextView 演示 GridLayout 的"行列 + 列权重"：
 * <ul>
 *   <li>根节点声明 {@code columnCount="2"}、{@code rowCount="2"} —— 2 列 2 行的网格；</li>
 *   <li>每个格子写 {@code layout_columnWeight} —— 控制该格在<b>本行里占的宽度比例</b>
 *       （第 2 个格子权重是 2，另外三个是 1，所以第 2 格明显更宽）；</li>
 *   <li>用权重时宽度必须写 {@code 0dp}，这与 LinearLayout 的规则一致。</li>
 * </ul>
 * 子控件默认"按顺序填格子"，也可以显式写 {@code layout_row} / {@code layout_column} 指定位置。
 *
 * <p>对应布局：{@code res/layout/activity_main_grid_layout.xml}
 */
public class MainGridLayoutActivity extends AppCompatActivity {

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

        // 加载布局 XML —— 行列与权重全在布局里
        setContentView(R.layout.activity_main_grid_layout);

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