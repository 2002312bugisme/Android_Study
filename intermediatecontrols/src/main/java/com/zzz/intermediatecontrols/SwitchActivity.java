package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 开关按钮（Switch）演示页。
 *
 * <p>演示要点：{@link Switch} 和 CheckBox 一样都继承自 {@link CompoundButton}，
 * 所以同样用 {@code OnCheckedChangeListener} 监听，回调里拿 {@code isChecked}
 * 把当前状态翻译成中文并回显到 TextView 上。
 *
 * <p>与上一页 {@code CheckBoxActivity} 的区别：两者回调签名完全相同，
 * 差别只在「外观语义」——CheckBox 表示"多选独立"，Switch 表示"单项开/关"。
 *
 * <p>对应布局：{@code res/layout/activity_switch.xml}
 * <p>涉及 API：{@code Switch#setOnCheckedChangeListener}、{@code String.format}
 */
public class SwitchActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    // 用于回显开关状态的 TextView，回调里要写它，所以提升为成员变量
    private TextView switch_state;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给开关挂监听器、缓存结果 TextView。
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
        setContentView(R.layout.activity_switch);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 找到系统样式的开关按钮
        Switch sys_switch = findViewById(R.id.sys_switch);

        // 让本 Activity（实现了 CompoundButton.OnCheckedChangeListener）处理开/关变化
        sys_switch.setOnCheckedChangeListener(this);
        // 缓存结果 TextView 的引用，供 onCheckedChanged 使用
        switch_state = findViewById(R.id.switch_state);

    }

    /**
     * 开关状态变化回调，由 {@link CompoundButton} 在用户拨动开关后自动触发。
     *
     * @param buttonView 触发本次变化的控件自身（这里是 sys_switch）。
     *                   类型为 {@link CompoundButton}——它是 CheckBox / RadioButton / Switch 的共同父类，
     *                   所以同一个回调方法可以复用给这三种控件（本页只注册了一个，仍按通用签名书写）。
     * @param isChecked  true = 本次变化后开关处于「开」；false = 处于「关」。
     */
    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
        // 三目运算把布尔值翻译成中文字，再交给 String.format 替换 %s 占位符
        String desc = String.format("当前Switch开关状态为：%s",isChecked ? "开" : "关");
        // 把拼好的描述写到结果 TextView 上，实现状态回显
        switch_state.setText(desc);
    }
}