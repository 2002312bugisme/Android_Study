package com.zzz.intermediatecontrols;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 九宫格（点九 / .9.png）图形演示页。
 *
 * <p>演示要点：本页<b>没有任何业务逻辑</b>——点九图的效果完全由布局里的
 * {@code android:background="@drawable/nine_origin"} 决定，Java 侧只负责把布局显示出来。
 * 与 {@code DrawableActivity} 形成对照：那边是「在代码里换背景」，这里是「图形写在 XML 里」。
 *
 * <p>点九图是什么：在普通 PNG 四周额外画 1px 边框，边框上的黑线用来告诉系统
 * 「哪些区域可以横向/纵向拉伸、哪个区域是内容区」，所以同一张图放大不糊、不会把边角拉变形。
 * 注意文件总尺寸 = 内容尺寸 + 2（每边 1px 边框）。
 *
 * <p>对应布局：{@code res/layout/activity_drawable_nine.xml}
 * <p>对应图形：{@code res/drawable/nine_origin.9.png}
 */
public class DrawableNineActivity extends AppCompatActivity {

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
        // 加载布局 XML —— 点九图背景就是在这一步的布局里指定的
        setContentView(R.layout.activity_drawable_nine);

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