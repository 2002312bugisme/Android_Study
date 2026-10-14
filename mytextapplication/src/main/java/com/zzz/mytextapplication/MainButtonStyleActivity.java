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
 * 按钮样式 + 「在布局里绑定点击事件」演示页。
 *
 * <p>演示要点：本页给出了<b>第四种</b>挂点击事件的方式——不是在 Java 里写
 * {@code setOnClickListener}，而是在布局里写 {@code android:onClick="doClick"}，
 * 由布局加载器通过反射去 Activity 上找同名方法。参见
 * {@code activity_main_button_style.xml} 中 id 为 tv_button 的那个 Button。
 *
 * <p>这种写法的硬性要求（三条缺一不可，否则运行时会直接崩溃）：
 * <ol>
 *   <li>方法必须是 {@code public}；</li>
 *   <li>必须有且只有一个 {@code View} 类型形参；</li>
 *   <li>返回类型必须是 {@code void}（所以本方法<b>没有</b> {@code @Override}，它不是覆写接口方法）。</li>
 * </ol>
 * 另外它只在"该布局由 Activity 解析（inflate）"时才能找到方法，这也是为什么方法名不带后缀、
 * 却能被自动调用的原因。
 *
 * <p>⚠️ 与 {@code MainButtonClickActivity} 一样，本文件 {@code import org.w3c.dom.Text;} 是<b>误导入</b>
 * （XML DOM 的文本节点，不是 TextView），且并未被使用，可以删除。
 *
 * <p>对应布局：{@code res/layout/activity_main_button_style.xml}
 */
public class MainButtonStyleActivity extends AppCompatActivity {

    // 结果回显用，doClick 回调里要写它，所以提升为成员变量
    private TextView viewById;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、缓存结果 TextView。<b>不</b>挂任何监听器——
     * 点击事件已经写在布局的 {@code android:onClick} 上。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);

        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);

        // 加载布局 XML —— 这一步同时会把 android:onClick="doClick" 绑定到下面的方法上
        setContentView(R.layout.activity_main_button_style);

        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 缓存结果控件（变量名叫 viewById 是因为初学时用 findViewById 的返回值直接起的名字，语义偏弱）
        viewById = findViewById(R.id.tv_result);
    }

    /**
     * 由布局中 {@code android:onClick="doClick"} 反射调用的点击处理方法。
     *
     * <p>方法名必须与布局里的字符串完全一致（大小写敏感）。
     *
     * @param view 被点击的那个 View（这里是 id 为 tv_button 的 Button）。
     *             因为这个方法可能被多个控件共用，所以形参名叫 view 而不是具体按钮名，
     *             并通过 {@code view.getText()} 取到各自的文字来区分是谁被点了。
     *             注意：声明类型是 {@code View}，要用 getText() 必须先强转成 {@code Button}。
     */
    public void doClick(View view) {
        // %s 依次被"当前时间"与"按钮文字"替换，拼好后写入结果区
        String format = String.format("%s, 你点击了按钮：%s", DateUtils.getNowTime(), ((Button) view).getText());
        viewById.setText(format);
    }
}