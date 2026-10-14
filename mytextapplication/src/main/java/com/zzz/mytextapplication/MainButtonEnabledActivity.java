package com.zzz.mytextapplication;

import android.graphics.Color;
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

/**
 * 按钮可用/禁用状态（setEnabled）演示页。
 *
 * <p>演示要点：一个"被测试按钮"（btn_test）+ 两个"遥控按钮"（启用 / 禁用）。
 * <ol>
 *   <li>{@code setEnabled(false)} 会让按钮变灰<b>且不再响应任何点击</b>——
 *       连 {@code onClick} 都不会被触发，这是它和"设置 visibility 隐藏"最本质的区别；</li>
 *   <li>禁用时通常配合 {@code setTextColor(Color.GRAY)} 手动把字色调灰，让"不可用"更直观。
 *       注意 Material 主题本身也会给禁用态加淡化效果，手动改色属于"加强提示"。</li>
 * </ol>
 *
 * <p>初始状态：布局里 btn_test 写了 {@code android:enabled="false"}，所以进页面时它是灰的，
 * 必须先点"启用"才能点。
 *
 * <p>对应布局：{@code res/layout/activity_main_button_enabled.xml}
 */
public class MainButtonEnabledActivity extends AppCompatActivity implements View.OnClickListener {

    // 被启用/禁用的那个按钮，三个回调分支都要用到，提升为成员变量
    private Button btn_test;
    // 结果回显用。注意它的 id 叫 btn_result，但实际是个 TextView——命名不准确，功能没问题
    private TextView result;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、缓存三个控件的引用并挂上同一个监听器。
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
        setContentView(R.layout.activity_main_button_enabled);

        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // "启用"按钮
        Button btn_enabled = findViewById(R.id.btn_enabled);

        // "禁用"按钮
        Button btn_unenabled = findViewById(R.id.btn_unenabled);

        // 被操作的目标按钮（布局里初始 enabled=false）
        btn_test = findViewById(R.id.btn_test);

        // 结果回显控件（实际类型是 TextView）
        result = findViewById(R.id.btn_result);

        // 三个按钮共用本 Activity 这一个监听器，回调里用 id 分流
        btn_enabled.setOnClickListener(this);
        btn_unenabled.setOnClickListener(this);
        btn_test.setOnClickListener(this);

    }

    /**
     * 三个按钮共用的点击回调。
     *
     * <p>逻辑：按 id 分三个分支——启用目标按钮、禁用目标按钮、以及"目标按钮自己被点"时写结果。
     *
     * @param v 被点击的 View。三种可能：btn_enabled / btn_unenabled / btn_test，
     *          所以必须用 {@code v.getId()} 判断来源。
     *          注意：当 btn_test 处于禁用状态时，本方法<b>不会</b>因它而被调用。
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_enabled) {

            // 启用目标按钮：恢复可点击，字色改回黑色
            btn_test.setEnabled(true);
            btn_test.setTextColor(Color.BLACK);

        } else if (v.getId() == R.id.btn_unenabled) {

            // 禁用目标按钮：不可点击，字色改灰（Color.GRAY 即 0xFF888888）
            btn_test.setEnabled(false);
            btn_test.setTextColor(Color.GRAY);

        } else if (v.getId() == R.id.btn_test) {

            // 目标按钮可用时被点击：把"当前时间 + 按钮文字"写进结果区
            String format = String.format(
                    "%s 你点击了：%s",
                    DateUtils.getNowTime(),
                    ((Button) v).getText()
            );

            result.setText(format);
        }
    }
}