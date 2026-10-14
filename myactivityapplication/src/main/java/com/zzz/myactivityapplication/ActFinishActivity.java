package com.zzz.myactivityapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * "返回上一页"演示页：演示结束当前 Activity 的两种触发方式。
 *
 * <p>演示要点：
 * <ol>
 *   <li>一个"返回"图片按钮（{@code btn_image_goback}）与一个"返回"文字按钮（{@code btn_goback}），
 *       两者共用同一个 {@code onClick}，效果都是 {@link #finish()}；</li>
 *   <li>{@code finish()} 的作用是把<b>当前</b> Activity 从返回栈顶弹出（销毁），
 *       于是屏幕上自动露出栈里下面的那个 Activity——也就是"返回上一页"；
 *       它和用户按系统返回键的效果一致，区别只是这里由按钮主动触发。</li>
 * </ol>
 *
 * <p>注意：本页还有一个 id 为 {@code tv_title} 的标题 TextView 在布局里，
 * 但 Java 没有引用它，所以它只是静态展示。
 *
 * <p>对应布局：{@code res/layout/activity_act_finish.xml}
 * <p>涉及 API：{@code Activity#finish}、{@code View#getId}
 */
public class ActFinishActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、把两个"返回"控件挂到同一个监听器上。
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
        setContentView(R.layout.activity_act_finish);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 返回的"图片按钮"。注意变量类型写的是 ImageView 而不是 ImageButton——
        // 因为布局里用的是 ImageView 配点击监听，这样也能点，只是没有按钮的按压反馈
        ImageView btn_image_goback = findViewById(R.id.btn_image_goback);
        btn_image_goback.setOnClickListener(this);
        // 返回的"文字按钮"
        Button btn_goback = findViewById(R.id.btn_goback);
        btn_goback.setOnClickListener(this);
    }

    /**
     * 两个"返回"控件共用的点击回调。
     *
     * @param v 被点击的 View，可能是 btn_image_goback，也可能是 btn_goback。
     *          两者要做的事完全一样，所以用 {@code ||} 把两个 id 判断合并成一个分支，
     *          不必写成两段重复代码。
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_image_goback || v.getId() == R.id.btn_goback) {
            // 结束（销毁）当前 Activity：返回栈弹出栈顶，露出下面的页面，等价于按系统返回键
            finish();
        }
    }
}