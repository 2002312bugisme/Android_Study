package com.zzz.intermediatecontrols;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 本模块的入口页之一（名字叫 Main，但当前<b>并不是</b> launcher）。
 *
 * <p>实际情况（照实说明，方便你自己回头梳理）：
 * <ul>
 *   <li>{@code AndroidManifest.xml} 里目前把 launcher 指向了 {@code TimePickerActivity}，
 *       所以开机后直接进的是时间选择页，不是本页；</li>
 *   <li>本页的布局 {@code res/layout/activity_main.xml} 仍是 IDE 生成的默认模板
 *       （ConstraintLayout + 居中一个 "Hello World!"），并没有做成"控件目录"；</li>
 *   <li>也就是说本页只演示「一个最小 Activity 长什么样」，各控件的演示页目前只能靠
 *       改 Manifest 里的 launcher 或写跳转代码才能打开。</li>
 * </ul>
 *
 * <p>对应布局：{@code res/layout/activity_main.xml}
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距——仅此三件事。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);
        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML 作为页面内容（目前仍是 IDE 默认模板布局）
        setContentView(R.layout.activity_main);
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