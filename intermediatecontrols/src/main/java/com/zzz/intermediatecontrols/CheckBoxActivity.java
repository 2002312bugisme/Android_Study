package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 复选框（CheckBox）演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li>系统默认样式 CheckBox 与自定义样式的 AppCompatCheckBox 对比；</li>
 *   <li>用 <b>同一个</b> {@link CompoundButton.OnCheckedChangeListener} 监听多个复选框，靠回调参数区分是谁被点了；</li>
 *   <li>在回调中动态改写被点击控件的文字，把「勾选 / 取消勾选」状态反馈到界面上。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_check_box.xml}
 * <p>涉及 API：{@code CheckBox#setOnCheckedChangeListener}、{@code CompoundButton#setText}、
 * {@code String.format}、{@code ViewCompat#setOnApplyWindowInsetsListener}
 */
public class CheckBoxActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给两个复选框挂上监听器。
     *
     * @param savedInstanceState 系统在进程被回收后「重建」本页时回传的上次状态快照；
     *                           首次正常启动时值为 {@code null}。
     *                           本页既没有写入也没有读取它，属于系统默认行为（写法保持不变）。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 必须先调用父类实现：完成窗口、主题、Fragment 管理器等基础初始化，漏掉会抛异常
        super.onCreate(savedInstanceState);
        // 开启「边到边」（沉浸式）显示：允许页面内容延伸到状态栏 / 导航栏所在区域
        EdgeToEdge.enable(this);
        // 把布局 XML 解析成 View 树并挂到当前 Activity 上，成为页面可见内容
        setContentView(R.layout.activity_check_box);

        // 既然内容已经延伸到系统栏下方，就需要把系统栏占掉的高度补成内边距，避免内容被遮住。
        // 监听器参数含义：
        //   v      —— 被监听的 View，这里是根布局 LinearLayout（android:id="@+id/main"）
        //   insets —— 本次窗口插入信息（各方向被系统 UI 占用的像素），由系统在布局阶段回调
        //   return —— 返回原 insets 表示"监听完毕，继续向后续节点分发"
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取出「系统栏」这一类区域（= 状态栏 + 导航栏），忽略刘海、输入法等其它类型
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 把系统栏尺寸四个方向原样设成根布局的 padding，内容就自然让开了
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 按 id 找到系统默认样式的复选框。
        // findViewById 的泛型由左侧变量类型推断，返回的已是 CheckBox，无需强转；找不到该 id 时返回 null
        CheckBox sys_checkbox = findViewById(R.id.sys_checkbox);

        // 按 id 找到自定义样式的复选框（布局里写的是 androidx.appcompat.widget.AppCompatCheckBox，
        // 它继承自 CheckBox，所以这里用父类型接收完全合法）
        CheckBox custom_checkbox = findViewById(R.id.custom_checkbox);

        // 让本 Activity 自己（实现了 OnCheckedChangeListener）成为监听器对象，
        // 两个复选框共用同一个监听器实例，省掉写两个匿名内部类
        sys_checkbox.setOnCheckedChangeListener(this);
        custom_checkbox.setOnCheckedChangeListener(this);
    }

    /**
     * 复选框勾选状态变化时的回调，由 {@link CompoundButton} 在用户点击后自动触发。
     *
     * <p>因为两个复选框都注册了 {@code this}，所以本方法会被两个控件复用，
     * 必须通过第一个参数分辨事件来源。
     *
     * @param buttonView 触发本次状态变化的控件自身（不是被点击以外的那一个）。
     *                   类型是 {@link CompoundButton}——"有两态、会勾选"的按钮基类，
     *                   CheckBox / RadioButton / Switch 都继承它，所以放进来的实参是具体的 CheckBox 实例。
     * @param isChecked  true = 本次操作让它变成「已勾选」；false = 本次操作让它变成「未勾选」。
     *                   注意它只表示"本次变化后的结果"，不代表"用户勾上了"。
     */
    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
        // 三目运算先选词，再交给 String.format 做占位替换：
        // 格式串里的 %s 会被 "勾选" 或 "取消勾选" 替换（format 的第二个参数是可变参数 Object...，此处自动装箱为 Object）
        String description = String.format("您%s了这个CheckBox",isChecked ? "勾选" : "取消勾选");
        // 把拼好的描述文字写回「被点击的那个」控件，实现即时状态反馈
        buttonView.setText(description);
    }
}