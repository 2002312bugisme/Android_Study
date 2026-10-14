package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 图形 Drawable 演示页：用 shape XML 给一个 View 换背景。
 *
 * <p>演示要点：两个按钮分别把中间那块 View 的背景换成
 * 「矩形（圆角/渐变）」和「椭圆」两种 shape 图形，用来观察
 * {@code setBackgroundResource} 的效果，以及 shape 作为背景时会自动拉伸铺满控件。
 *
 * <p>对应布局：{@code res/layout/activity_drawable.xml}
 * <p>对应图形：{@code res/drawable/shape_rectangle.xml}、{@code res/drawable/shape_oval.xml}
 * <p>涉及 API：{@code View#setBackgroundResource}、{@code View#getId}、{@code View.OnClickListener}
 */
public class DrawableActivity extends AppCompatActivity implements View.OnClickListener {

    // 被换背景的目标 View，两个按钮的回调都要操作它，所以提升为成员变量
    private View view_content;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给两个按钮挂监听器、设置初始背景。
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
        setContentView(R.layout.activity_drawable);

        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 拿到被换背景的 View。这里用父类型 View 接收，因为它只需要 setBackground 这一类通用能力
        view_content = findViewById(R.id.view_content);
        // 两个触发按钮，泛型推断为 Button
        Button btn_view_rectangle = findViewById(R.id.btn_view_rectangle);
        Button btn_view_oval = findViewById(R.id.btn_view_oval);

        // 都交给本 Activity 处理点击（实现 View.OnClickListener），后面用 id 区分
        btn_view_rectangle.setOnClickListener(this);

        btn_view_oval.setOnClickListener(this);

        // 进入页面时先给一个矩形背景，避免初始是空白
        view_content.setBackgroundResource(R.drawable.shape_rectangle);


    }

    /**
     * 两个按钮共用的点击回调。
     *
     * <p>逻辑：按被点击控件的 id 分流，把目标 View 的背景替换成对应的 shape 图形。
     *
     * @param v 被点击的 View（可能是 btn_view_rectangle，也可能是 btn_view_oval），
     *          所以必须靠 {@code v.getId()} 判断来源。
     */
    @Override
    public void onClick(View v) {
        // 点的是「矩形」按钮 → 换成 shape_rectangle（圆角 + 渐变）
        if(v.getId() == R.id.btn_view_rectangle){
            view_content.setBackgroundResource(R.drawable.shape_rectangle);
        } else if (v.getId() == R.id.btn_view_oval) {
            // 点的是「椭圆」按钮 → 换成 shape_oval
            view_content.setBackgroundResource(R.drawable.shape_oval);
        }
    }
}