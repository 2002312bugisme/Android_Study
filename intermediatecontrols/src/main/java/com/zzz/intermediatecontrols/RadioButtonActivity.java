package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 单选按钮（RadioButton + RadioGroup）演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li>单选按钮<b>必须</b>放进 {@link RadioGroup} 才会呈现「互斥」效果——同组内选中一个会自动取消其它；</li>
 *   <li>监听要挂在 {@code RadioGroup} 上（{@link RadioGroup.OnCheckedChangeListener}），
 *       而不是挂每个 RadioButton 上，回调一次就能拿到「最终选中了谁」；</li>
 *   <li>回调只给被选中项的 <b>id</b>，需要自己用 {@code if / else if} 把它翻译成业务含义。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_radio_button.xml}
 * <p>涉及 API：{@code RadioGroup#setOnCheckedChangeListener}、
 * {@code RadioGroup.OnCheckedChangeListener#onCheckedChanged}、{@code String.format}
 */
public class RadioButtonActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    // 用于回显选择结果的 TextView，回调里要写它，所以提升为成员变量
    private TextView radioBtn_choice;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给单选组挂监听器、缓存结果 TextView。
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
        setContentView(R.layout.activity_radio_button);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 找到单选组（互斥容器）
        RadioGroup radioBtn_gender = findViewById(R.id.radioBtn_gender);
        // 找到组内的两个单选按钮。
        // 注意：这两个变量之后并没有被再次使用——选中态判断完全由回调给出的 checkedId 完成，
        //      这里取引用只是为了「能直观看到组内成员有哪些」，属于可保留的冗余写法
        RadioButton radioBtn_man = findViewById(R.id.radioBtn_man);
        RadioButton radioBtn_woman = findViewById(R.id.radioBtn_woman);
        // 监听器挂在组上而不是挂在每个按钮上，这样一次回调就能拿到最终选中的那个 id
        radioBtn_gender.setOnCheckedChangeListener(this);

        // 缓存结果 TextView 的引用，供 onCheckedChanged 使用
        radioBtn_choice = findViewById(R.id.radioBtn_choice);
    }

    /**
     * 单选组选中项变化回调，由 {@link RadioGroup} 在组内选中项改变时自动触发。
     *
     * @param group     触发事件的单选组本身（这里是 radioBtn_gender），
     *                  可用于在回调里继续操作组（如清空选择、取选中项）。
     * @param checkedId 本次<b>被选中</b>的那个单选按钮的资源 id（形如 R.id.radioBtn_man），
     *                  系统不会在回调里传「被取消选中」的按钮 id。
     */
    @Override
    public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
        // 先把结果置空，再由下面的分支赋上具体含义；
        // 这样即使将来布局新增了选项而这里忘了加分支，也不会显示上一个选择（只会显示 null）
        String choice = null;
        // 把 id 翻译成业务含义
        if (checkedId == R.id.radioBtn_man){
            choice = "Man";
        } else if (checkedId == R.id.radioBtn_woman) {
            choice = "Woman";
        }
        // 回显到 TextView：%s 会被上面选出的字符串替换（choice 为 null 时 formatted 结果就是 "null"）
        radioBtn_choice.setText(String.format("Your Choice: %s",choice));
    }
}