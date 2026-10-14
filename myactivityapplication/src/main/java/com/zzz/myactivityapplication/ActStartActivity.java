package com.zzz.myactivityapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 本模块的 launcher 页（应用启动后进入的第一个界面）。
 *
 * <p>来源：{@code AndroidManifest.xml} 里 {@code ActStartActivity} 的 {@code <intent-filter>}
 * 声明了 {@code action.MAIN} + {@code category.LAUNCHER}，这是"成为桌面入口"的固定搭配。
 *
 * <p>演示要点：最基础的<b>显式跳转</b>——点按钮后用
 * {@code new Intent(this, 目标Activity.class)} 构造意图，再 {@code startActivity} 打开新页面。
 * 一个页面上只做一件事，所以本页代码非常短，适合作为"页面跳转的最小示例"。
 *
 * <p>对应布局：{@code res/layout/activity_act_start.xml}
 * <p>涉及 API：{@link Intent#Intent(android.content.Context, Class)}、{@code Context#startActivity}
 */
public class ActStartActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给"下一页"按钮挂监听器。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);
        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_act_start);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 拿到"下一页"按钮，并把本 Activity 注册为它的点击监听器
        Button btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
    }

    /**
     * 点击回调。
     *
     * @param v 被点击的 View（这里是 btn_next）。本页只有一个按钮，仍按规范用 id 判断。
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_next){
            // 显式 Intent：两个形参分别是
            //   ① this            —— 当前上下文（Context），用来标识"从哪发起"
            //   ② ActFinishActivity.class —— 目标 Activity 的 Class 对象，系统据此反射创建它
            // 传 Class 的方式叫"显式（Explicit）跳转"：目标写死在代码里，不依赖任何约定
            // startActivity 会把目标 Activity 压入返回栈并显示出来
            startActivity(new Intent(this,ActFinishActivity.class));
        }
    }
}