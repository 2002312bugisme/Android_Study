package com.zzz.advancedcontrolsapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 对话框式下拉列表（Spinner，spinnerMode="dialog"）演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li><b>装配链路和 dropdown 完全一样</b>：ArrayAdapter + setDropDownViewResource + setAdapter
 *       + OnItemSelectedListener，一个 API 都不换。换的只是布局里一个 XML 属性，
 *       所以这两种形态属于「同一个控件的两种展开方式」，不是两个控件。</li>
 *   <li><b>弹层形态变了</b>：dropdown 是贴着控件往下弹的列表，点空白处即关闭；
 *       dialog 是居中的模态 AlertDialog，带标题栏（标题来自 {@code android:prompt}）和取消入口。</li>
 *   <li><b>弹窗行的样式仍由 setDropDownViewResource() 决定</b>，但别指望它自动变成
 *       「radio + 文字」。实测（API 33 + Material3 主题）弹窗里是<b>纯文字行</b>：
 *       {@code simple_spinner_dropdown_item} 虽然是 CheckedTextView，可它的单选指示器
 *       在这个对话框列表里并不显示，当前选中项也没有勾选标记。
 *       要那种经典单选观感，得自己写一份含 CheckedTextView / RadioButton 的布局传进去。</li>
 *   <li><b>取消不会回调</b>：点列表项 = 立即确认并关闭（会触发 onItemSelected）；
 *       按返回键 / 点对话框外部 = 放弃本次操作，选中项保持原样，
 *       而且不会走 {@code onNothingSelected()} —— 因为「有选中项」这个状态从未消失。</li>
 *   <li>{@code onItemSelected} 同样在 setAdapter 之后<b>立即回调一次</b>（position = 0），
 *       这不是用户操作，而是"当前选中项已确定"的通知。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_spinner_dialog.xml}
 * <p>对应数据源：{@code res/values/arrays.xml}（{@code spinner_cities}，与 dropdown 页共用同一份数据）
 * <p>涉及 API：{@code ArrayAdapter#createFromResource}、{@code ArrayAdapter#setDropDownViewResource}、
 * {@code Spinner#setAdapter}、{@code AdapterView.OnItemSelectedListener}
 */
public class SpinnerDialogActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（本页唯一覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、装配 Spinner 的数据源与选中监听。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口 / 主题等基础初始化
        super.onCreate(savedInstanceState);
        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_spinner_dialog);
        // 把系统栏占掉的尺寸补成根布局内边距，避免内容被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding（覆盖式赋值，所以留白写在子容器上）
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 下拉列表控件（spinnerMode 在 XML 里已设为 dialog，Java 侧无需再设置）
        Spinner spinnerDialog = findViewById(R.id.spinner_dialog);
        // 结果回显控件，监听回调里要写它
        TextView tvResult = findViewById(R.id.tv_dialog_result);

        // 数据源与 dropdown 页共用 arrays.xml 里的同一个字符串数组。
        // 形参：context + 数组资源 id + 只含一个 TextView 的单项布局（系统内置 simple_spinner_item）
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.spinner_cities, android.R.layout.simple_spinner_item);

        // dialog 模式同样要用它：弹窗里每一行（radio + 文字）的版式就来自这里。
        // 若省掉这句，弹窗行会复用 spinner_item，行高偏高且没有单选控件的位置。
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 交给 Spinner；此刻控件渲染出折叠态当前项，并触发一次 onItemSelected(position = 0)
        spinnerDialog.setAdapter(arrayAdapter);

        // 选中监听：参数签名与 dropdown 页完全一致 —— 因为回调属于 AdapterView，
        // 而 Spinner 并不区分自己是哪种展开模式。
        spinnerDialog.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * 某一项被选中时回调（用户在弹窗里点了某一行，或代码调用 setSelection）。
             *
             * @param parent   触发回调的 AdapterView 本体，这里是 spinner_dialog
             * @param view     被选中那一行的行 View；弹窗关闭后该引用失效，不要缓存
             * @param position 选中项在数据源中的下标（从 0 开始）
             * @param id       该行的 row id，ArrayAdapter 里恒等于 position
             */
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view,
                                       int position, long id) {
                // getItemAtPosition 拿到数据源里那一项（CharSequence），转成 String 再拼接
                String city = parent.getItemAtPosition(position).toString();
                // 一行结果回显：核心 API 调用的结论就落在这句上
                tvResult.setText("选中的城市：" + city);
            }

            /**
             * 选中项被清空时回调。
             *
             * <p>dialog 模式下用户按返回键 / 点对话框外部属于「放弃本次操作」，
             * 选中项仍是原来那个，并不会变成"未选中"，所以这里同样不会被调用；
             * 只有数据源被清空时才可能走到。接口要求必须实现，空实现即可。
             *
             * @param parent 触发回调的 AdapterView 本体
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 无选中项时不做处理
            }
        });
    }
}
