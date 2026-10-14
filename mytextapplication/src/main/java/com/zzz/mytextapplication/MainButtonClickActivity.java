package com.zzz.mytextapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.mytextapplication.utils.DateUtils;

import org.w3c.dom.Text;

/**
 * 按钮点击事件演示页（三种挂监听器的方式对照）。
 *
 * <p>本页最大的价值是把"同一个点击事件"用 <b>三种不同写法</b>实现，放在一起对比：
 * <ol>
 *   <li>{@code btn_single} —— <b>自定义监听器类</b>：写一个实现 {@link View.OnClickListener}
 *       的静态内部类 {@link MyOnClickListener}，把"要改哪个 TextView"通过构造函数传进去。
 *       适合"逻辑较复杂、需要携带额外状态"的场景；</li>
 *   <li>{@code btn_all} —— <b>Activity 自己实现接口</b>：{@code setOnClickListener(this)}，
 *       在 {@code onClick} 里用 id 分流。适合页面上有多个按钮、逻辑都比较短的情况；</li>
 *   <li>{@code btn_long} —— <b>lambda 表达式</b>：{@code setOnLongClickListener(v -> {...})}。
 *       因为 OnClickListener/OnLongClickListener 都只有一个抽象方法（函数式接口），
 *       Java 8+ 可直接用 lambda 简写。</li>
 * </ol>
 *
 * <p>⚠️ 一个可以顺手清理的小问题：本文件 {@code import org.w3c.dom.Text;} 是<b>误导入</b>——
 * 它是 XML 解析库里的 DOM 文本节点，和 UI 控件 {@code android.widget.TextView} 毫无关系，
 * 而且本文件根本没用到它。这是 IDE 自动补全时选错的典型情况，删掉不影响编译。
 *
 * <p>对应布局：{@code res/layout/activity_main_button_click.xml}
 */
public class MainButtonClickActivity extends AppCompatActivity implements View.OnClickListener {

    // 多个按钮共用同一个监听器时，需要在 onClick 里访问这个 TextView，所以提升为成员变量；
    // 另外两个结果 TextView 只在 onCreate 的局部作用域里被闭包捕获，用局部变量即可
    private TextView tv_message_all;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距，然后给三个按钮分别挂上不同写法的监听器。
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
        setContentView(R.layout.activity_main_button_click);

        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button btn_single = findViewById(R.id.btn_single);


        TextView tv_message_single = findViewById(R.id.tv_message_single);

        // 写法一：new 一个自定义监听器，并把"要写入的结果控件"作为构造参数交给它
        // （自定义类内部持有这个 TextView，因此不需要它自己是成员变量）
        btn_single.setOnClickListener(new MyOnClickListener(tv_message_single));

        Button btn_all = findViewById(R.id.btn_all);

        // 这个结果控件要在下面的 onClick 回调里使用，所以保存为成员变量
        tv_message_all = findViewById(R.id.tv_message_all);

        // 写法二：把本 Activity 自己当监听器（它已 implements View.OnClickListener）
        btn_all.setOnClickListener(this);

        Button btn_long = findViewById(R.id.btn_long);

        TextView tv_message_long = findViewById(R.id.tv_message_long);

        // 写法三：lambda。点击后会执行箭头右边的方法体。
        // 形参 v 就是"被长按的那个 View"（即 btn_long）；tv_message_long 是被 lambda 捕获的局部变量——
        // Java 要求被捕获的局部变量必须是 final 或"事实上不再改变"，这里正符合
        btn_long.setOnLongClickListener(v -> {
            // %s 依次被"当前时间"和"按钮上的文字"替换。
            // getText() 返回 CharSequence，这里强转成 Button 才能调用（v 的声明类型是 View）
            String format = String.format("%s,您点击了：%s", DateUtils.getNowTime(), ((Button) v).getText());
            tv_message_long.setText(format);
            // 关键：返回值决定事件是否被消费。返回 true = "我处理了"，系统不会再把它当成普通点击；
            // 若返回 false，长按结束后还可能继续触发 onClick，产生两次响应
            return true;
        });


    }

    /**
     * 写法二对应的方法：Activity 自身作为点击监听器时的统一入口。
     *
     * @param v 被点击的 View（本页只有 btn_all 注册了 {@code this}，但仍按规范用 id 判断来源，
     *          这样以后再加按钮不会出错）。
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_all) {
            String format = String.format("%s,您点击了：%s", DateUtils.getNowTime(), ((Button) v).getText());
            tv_message_all.setText(format);
        }

    }

    /**
     * 写法一用的自定义点击监听器。
     *
     * <p>为什么写成 {@code static} 内部类：静态内部类<b>不持有外部 Activity 的隐式引用</b>，
     * 不会因为监听器被长期持有而导致 Activity 无法回收（内存泄漏）。
     * 代价是它访问不到 Activity 的成员，所以需要的数据得从构造函数传进来——
     * 本例传的正是"要显示结果的 TextView"。
     */
    static class MyOnClickListener implements View.OnClickListener {

        // 结果控件。声明为 final：构造完成后不再改变，语义清晰也更安全
        private final TextView tvMessageSingle;

        /**
         * 构造监听器。
         *
         * @param tvMessageSingle 点击后要把结果文字写到哪个 TextView 上。
         *                        形参名用大驼峰是因为它直接赋给同名字段，与字段命名保持一致。
         */
        public MyOnClickListener(TextView tvMessageSingle) {
            this.tvMessageSingle = tvMessageSingle;
        }

        /**
         * 点击回调。
         *
         * @param v 被点击的 View（这里是 btn_single）。用它本身的 text 作为结果的一部分，
         *          所以同一个监听器类可以原样复用到别的按钮上。
         */
        @Override
        public void onClick(View v) {
            String format = String.format("%s,您点击了：%s", DateUtils.getNowTime(), ((Button) v).getText());
            tvMessageSingle.setText(format);
        }
    }
}