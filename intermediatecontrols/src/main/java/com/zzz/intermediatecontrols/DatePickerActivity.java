package com.zzz.intermediatecontrols;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.Month;

/**
 * 日期选择演示页：演示「嵌入页面的 DatePicker」与「弹出的 DatePickerDialog」两种用法。
 *
 * <p>演示要点：
 * <ol>
 *   <li>页面里直接内嵌一个 {@link DatePicker}（spinner 模式），点 Submit 读取它的年月日；</li>
 *   <li>点 "Choose Date" 弹出 {@link DatePickerDialog}，用 {@code java.time.LocalDate} 取
 *       <b>当前日期</b>作为对话框初始值，选完直接在回调里回显。</li>
 * </ol>
 *
 * <p>两个易错点：
 * <ul>
 *   <li>{@code DatePicker#getMonth()} 与 {@code DatePickerDialog} 回调里的 month 都是
 *       <b>从 0 开始</b>（1 月 = 0），所以显示时要 <b>+1</b>；</li>
 *   <li>而 {@code LocalDate} 用的是 {@link Month} 枚举，{@code getValue()} 是 <b>从 1 开始</b> 的，
 *       传回给 {@code DatePickerDialog} 时要 <b>-1</b> 才符合它「0 基」的约定。</li>
 * </ul>
 *
 * <p>对应布局：{@code res/layout/activity_date_picker.xml}
 * <p>涉及 API：{@code DatePicker#getYear/getMonth/getDayOfMonth}、
 * {@code DatePickerDialog} 与 {@code DatePickerDialog.OnDateSetListener}、
 * {@code LocalDate.now}、{@code String.format}
 */
public class DatePickerActivity extends AppCompatActivity implements View.OnClickListener {

    // 结果回显用的 TextView，两个分支都要写它
    private TextView tv_date;
    // 内嵌在布局里的 DatePicker，Submit 时读它的年月日
    private DatePicker dialog_date_picker;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给两个按钮挂监听器、缓存控件引用。
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
        setContentView(R.layout.activity_date_picker);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 内嵌的日期选择器（在布局里以 spinner 形式呈现）
        dialog_date_picker = findViewById(R.id.dialog_date_picker);
        // "Choose Date" 按钮：点击后弹出日期选择对话框
        Button btn_choose_date = findViewById(R.id.btn_choose_date);
        btn_choose_date.setOnClickListener(this);
        // "Submit" 按钮：点击后读取内嵌选择器的当前值
        Button btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        // 缓存结果 TextView 的引用
        tv_date = findViewById(R.id.tv_date);
    }

    /**
     * 两个按钮共用的点击回调。
     *
     * <p>逻辑：按按钮 id 分流——Submit 直接读内嵌 DatePicker 的值；Choose Date 则弹对话框。
     *
     * @param v 被点击的 View（btn_submit 或 btn_choose_date），必须靠 {@code v.getId()} 区分。
     */
    @Override
    public void onClick(View v) {
        // 分支一：提交——读取页面上内嵌的 DatePicker 当前值并回显
        if (v.getId() == R.id.btn_submit) {
            // 格式化占位符：%d 依次被年份、月份、日替换（都是 int）
            String desc = String.format("The date you selected is %d year %d month %d day",
                    dialog_date_picker.getYear(),
                    // getMonth() 返回 0~11（0 表示 1 月），所以要 +1 才是人类习惯的月份
                    dialog_date_picker.getMonth() + 1,
                    dialog_date_picker.getDayOfMonth());
            tv_date.setText(desc);
        } else if (v.getId() == R.id.btn_choose_date) {
            // 分支二：弹出日期选择对话框，并用「今天」作为初始选中值
            // LocalDate.now() 取当前系统日期（需要 API 26+，本项目 minSdk 28 满足）
            LocalDate now = LocalDate.now();
            // 拆出年、月、日三个分量传给 DatePickerDialog 的构造函数
            int year = now.getYear();
            // getMonth() 返回的是 java.time.Month 枚举（JANUARY 对应 1），不是 int
            Month monthNum = now.getMonth();
            // 枚举值减 1，转成 DatePickerDialog 要求的「0 基月份」
            int month = monthNum.getValue() - 1;
            int day = now.getDayOfMonth();
            // 构造日期选择对话框。
            // 构造形参：① this = Context；② 选中后的回调监听器；③~⑤ 初始年、月(0基)、日
            // 监听器形参：view = 内部的 DatePicker 控件；year1/month1/dayOfMonth = 用户最终选中的年、月(0基)、日
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String desc = String.format("The date you selected is %d year %d month %d day",
                        year1,
                        month1,
                        dayOfMonth);
                tv_date.setText(desc);
            }, year, month, day);

            // 必须 show() 才会真正显示
            dialog.show();

        }

    }
}