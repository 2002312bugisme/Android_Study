package com.zzz.mytextapplication;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.mytextapplication.utils.Utils;

/**
 * 视图边框（用代码修改控件尺寸）演示页。
 *
 * <p>演示要点：这次不是改文字，而是<b>用代码改控件的宽高</b>。做法共四步：
 * <ol>
 *   <li>拿到控件（{@code findViewById}）；</li>
 *   <li>取出它的布局参数（{@code getLayoutParams()}）；</li>
 *   <li>改参数里的宽高数值——注意这里的单位是 <b>px</b>，所以要先用工具类把 dp 换算成 px；</li>
 *   <li>把改完的参数<b>重新设回去</b>（{@code setLayoutParams}），否则不生效。</li>
 * </ol>
 *
 * <p>关键概念：为什么必须先取再设？因为 {@code getLayoutParams()} 给的是"当前生效的参数对象"，
 * 直接改它有时不会触发重新布局；显式 setLayoutParams 才能让父容器重新测量与摆放。
 *
 * <p>对应布局：{@code res/layout/activity_main_text_border.xml}
 * <p>涉及 API：{@code View#getLayoutParams/setLayoutParams}、{@code ViewGroup.LayoutParams}、
 * {@link Utils#dp2px}
 */
public class MainViewBorderActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距，然后把目标 TextView 的宽高改成 300dp × 50dp。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);

        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_main_text_border);

        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 获取控件
        TextView viewById = findViewById(R.id.tv_border_coder);
        // 获取布局参数
        // 返回类型是 ViewGroup.LayoutParams（所有布局参数的基类），
        // 里面至少能保证有 width / height 两个字段可读可写
        ViewGroup.LayoutParams layoutParams = viewById.getLayoutParams();
        // 修改布局参数中的宽度数值，默认px单位，需要把dp 转换成 px
        // dp2px 的形参：① this = Context（用来读屏幕密度）；② dp 数值 → 返回对应的 px 像素数
        // 300dp 是设计稿上的"视觉尺寸"，换算成 px 才能赋给 LayoutParams（它只认像素）
        layoutParams.width = Utils.dp2px(this,300);
        layoutParams.height = Utils.dp2px(this,50);
        //设置布局参数
        // 关键一步：把改好的参数设回控件，触发父容器重新测量并摆放
        viewById.setLayoutParams(layoutParams);
    }
}