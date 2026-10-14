package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 图片显示（ImageView / ImageButton / 按钮图标）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>不做任何事</b>，
 * {@code activity_main_image_view.xml} 里把 8 种 {@code scaleType} 逐个排开对比，
 * 用的都是同一张图（{@code @drawable/tos_cn}），因此差异完全来自 scaleType 本身：
 *
 * <table border="1">
 *   <tr><th>scaleType</th><th>行为</th></tr>
 *   <tr><td>fitCenter</td><td>等比缩放到"能完整放下"，居中（默认值）</td></tr>
 *   <tr><td>centerCrop</td><td>等比缩放到"铺满"，超出的部分裁掉</td></tr>
 *   <tr><td>center</td><td>不缩放，只把原图放中间，超出即裁掉</td></tr>
 *   <tr><td>centerInside</td><td>只缩小不放大，完整显示并居中</td></tr>
 *   <tr><td>fitXY</td><td>强行拉满宽高，<b>不保持比例</b>（会变形）</td></tr>
 *   <tr><td>fitStart / fitEnd</td><td>同 fitCenter，但对齐到左上 / 右下</td></tr>
 *   <tr><td>matrix</td><td>用矩阵自行控制缩放平移，不自动适配</td></tr>
 * </table>
 *
 * <p>另外还演示了两种"图 + 文字"的组合：{@code ImageButton}（可点击的图片按钮，也有 scaleType）
 * 与 Button 的 {@code android:drawableStart}（在文字左侧嵌一张小图）。
 *
 * <p>注意布局里这些 ImageView 都写了 id，但本页<b>没有</b>使用它们——纯粹用来"看效果"。
 *
 * <p>对应布局：{@code res/layout/activity_main_image_view.xml}
 */
public class MainImageViewActivity extends AppCompatActivity {

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

        // 加载布局 XML —— 8 种 scaleType 的对比全在布局里
        setContentView(R.layout.activity_main_image_view);

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