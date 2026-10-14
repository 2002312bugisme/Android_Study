package com.zzz.intermediatecontrols;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

/**
 * 时间选择演示页：演示「嵌入页面的 TimePicker」与「弹出的 TimePickerDialog」两种用法。
 *
 * <p>演示要点：
 * <ol>
 *   <li>页面里内嵌一个 {@link TimePicker} 并设为 24 小时制，点 Submit 读取它的时、分；</li>
 *   <li>点 "Choose time" 弹出 {@link TimePickerDialog}，用 {@code java.time.LocalTime} 取
 *       <b>当前时间</b>作为初始值；</li>
 *   <li>对话框回调只给「时、分」不给秒——本例把进入对话框前记下的秒用成员变量 {@code second}
 *       带进回调，凑成完整的时分秒显示。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_time_picker.xml}
 * <p>涉及 API：{@code TimePicker#setIs24HourView/getHour/getMinute}、
 * {@code TimePickerDialog} 与 {@code TimePickerDialog.OnTimeSetListener}、{@code LocalTime.now}
 */
public class TimePickerActivity extends AppCompatActivity implements View.OnClickListener {

    // 结果回显用的 TextView，两个分支都要写它，所以提升为成员变量
    private TextView tv_time;
    // 内嵌在布局里的 TimePicker，Submit 时读它的时、分
    private TimePicker dialog_time_picker;
    // 进入对话框那一刻的「秒」。TimePickerDialog 的回调只提供时和分，
    // 这里先用字段把秒记下来，回调触发时再取用——"用成员变量跨越回调传值"的常见做法
    private int second;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、把内嵌选择器设为 24 小时制、
     * 给两个按钮挂监听器、缓存结果 TextView。
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
        setContentView(R.layout.activity_time_picker);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 内嵌的 TimePicker
        dialog_time_picker = findViewById(R.id.dialog_time_picker);
        // 形参 true 表示 24 小时制（传 false 则显示上午/下午 + 12 小时制）
        dialog_time_picker.setIs24HourView(true);
        // "Choose time" 按钮：点击后弹出时间选择对话框
        Button btn_choose_time = findViewById(R.id.btn_choose_time);
        btn_choose_time.setOnClickListener(this);
        // "Submit" 按钮：点击后读取内嵌选择器的当前值
        Button btn_submit = findViewById(R.id.btn_submit_time);
        btn_submit.setOnClickListener(this);

        // 缓存结果 TextView 的引用
        tv_time = findViewById(R.id.tv_time);
    }

    /**
     * 两个按钮共用的点击回调。
     *
     * <p>逻辑：按按钮 id 分流——Submit 读内嵌 TimePicker；Choose time 弹 TimePickerDialog。
     *
     * @param v 被点击的 View（btn_submit_time 或 btn_choose_time），必须靠 {@code v.getId()} 区分。
     */
    @Override
    public void onClick(View v) {
        // 分支一：提交——读取内嵌 TimePicker 的当前时、分并回显
        if (v.getId() == R.id.btn_submit_time) {
            // %d 依次被「时」「分」替换。
            // 注意 getHour() / getMinute() 是 API 23 起才有的方法（旧写法 getCurrentHour() 已废弃）
            String desc = String.format(
                    "The time you selected is %d hours and %d minutes",
                    dialog_time_picker.getHour(),
                    dialog_time_picker.getMinute()
            );
            tv_time.setText(desc);

        }else if (v.getId() == R.id.btn_choose_time) {
            // 分支二：弹出时间选择对话框，用「此刻」作为初始选中值
            // LocalTime.now() 取当前系统时间（需要 API 26+，本项目 minSdk 28 满足）
            LocalTime now = LocalTime.now();
            int hour = now.getHour();
            int minute1 = now.getMinute();
            // 秒先存进成员变量，供稍后的对话框回调使用（回调本身拿不到秒）
            second = now.getSecond();
            // 构造时间选择对话框。
            // 构造形参：① this = Context；
            //          ② android.R.style.Widget_Holo_ActionBar_Solid = 对话框使用的样式，
            //             取自系统内置的 Holo 主题样式，指定它会覆盖默认外观（演示"可传自定义 style"的写法）；
            //          ③ 选中后的回调监听器（OnTimeSetListener，用 lambda 实现后显式强转以保证类型匹配）；
            //          ④~⑥ 初始时、分，以及 true（true = 24 小时制）
            // 监听器形参：view = 内部 TimePicker 控件；hourOfDay / minute = 用户选中的时、分
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Widget_Holo_ActionBar_Solid,(TimePickerDialog.OnTimeSetListener) (view, hourOfDay, minute) -> {
                // 用回调给的时、分 + 进入对话框前记下的秒，拼出完整时间描述
                String desc = String.format("The time you selected is %d hours, %d minutes and %d seconds",
                        hourOfDay,
                        minute,
                        second);
                tv_time.setText(desc);
            },hour,minute1,true);

            // 必须 show() 才会真正显示
            timePickerDialog.show();


        }
    }
}