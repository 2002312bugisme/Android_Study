package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 相对布局（RelativeLayout）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>不做任何事</b>，
 * {@code activity_main_relative_layout.xml} 里用十几个带绿底的 TextView，
 * 把 RelativeLayout 的对齐属性逐个演示了一遍，分两大类：
 * <ul>
 *   <li><b>相对父容器</b>：{@code layout_alignParentLeft/Right/Bottom}、
 *       {@code layout_centerInParent}、{@code layout_centerHorizontal/Vertical}
 *       —— 即"我在父容器的左上角 / 正中间 / 底部水平中间"；</li>
 *   <li><b>相对兄弟控件</b>：{@code layout_toLeftOf/toRightOf/above/below/alignLeft/alignRight/alignTop/alignBottom}
 *       —— 即"我在某个控件的左边 / 上面 / 与它左对齐"。</li>
 * </ul>
 * 注意：相对兄弟控件的属性必须写成 {@code @+id/xxx} 才能引用，而且被引用的控件要先声明（或至少 id 已存在）。
 *
 * <p>对应布局：{@code res/layout/activity_main_relative_layout.xml}
 */
public class MainRelativeLayoutActivity extends AppCompatActivity {

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

        // 加载布局 XML —— 各种对齐关系全在布局里
        setContentView(R.layout.activity_main_relative_layout);

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