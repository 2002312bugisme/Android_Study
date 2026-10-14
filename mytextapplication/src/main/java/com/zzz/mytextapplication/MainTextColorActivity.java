package com.zzz.mytextapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 文字颜色 / 背景颜色演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li>文字颜色可以用 {@code setTextColor(0xAARRGGBB)} 直接传十六进制色值（源码里以注释形式保留了写法）；</li>
 *   <li>背景颜色更适合用 {@code setBackgroundResource(R.color.xxx)} 引用 colors.xml 的资源，
 *       这样颜色集中管理、将来要改只改一处。</li>
 * </ol>
 *
 * <p>注意本页的几个细节：
 * <ul>
 *   <li>{@code import android.view.View;} 与 {@code import android.graphics.Color;} 在注释掉代码后已经用不到了，
 *       本文件里也没有它们——因为对应的调用都被注释，IDE 顺手把 import 也去掉了；</li>
 *   <li>沉浸式（EdgeToEdge）两段代码在本页同样被注释掉了。</li>
 * </ul>
 *
 * <p>对应布局：{@code res/layout/activity_main_text_color.xml}
 * <p>涉及 API：{@code TextView#setTextColor}、{@code TextView#setBackgroundColor}、
 * {@code View#setBackgroundResource}
 */
public class MainTextColorActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：加载布局，然后给指定 id 的 TextView 换背景颜色。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);

        // 【被注释】开启沉浸式显示；本页注释掉，所以内容从状态栏下方开始
        //        EdgeToEdge.enable(this);

        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_main_text_color);


        // 【被注释】与上面的 enable() 配套的系统栏内边距处理，一并注释掉了
        //        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //            return insets;
        //        });

        // 【被注释】下面这段是"用代码设文字颜色"的三种写法示范，当前被停用。
        // 变量名起得不好（viewById 其实是个 TextView），且会与下面 33 行的同名变量冲突，所以整段注释掉了
        //        TextView viewById = findViewById(R.id.tv_color_code);

        // 【被注释】用颜色常量设文字颜色（Color 类里的预定义颜色）
        //        viewById.setTextColor(Color.GREEN);
        //RGB 八位十六进制数
        // 【被注释】8 位写法 0xAARRGGBB：前两位 AA 是透明度（11 = 半透明），后六位是 RGB 颜色
        //        viewById.setTextColor(0x11111111);
        //RGB 六位十六进制数  透明度 默认 00 透明
        // 【被注释】6 位写法：省略了透明度，注意原注释里"默认 00 透明"的说法值得商榷——
        // 实际是"不传 alpha 时按不透明处理"，写成 0x00ff00 得到的是不透明绿色（若想全透明应显式写 0x0000ff00）
        //        viewById.setTextColor(0x00ff00);

        //设置背景颜色
        // 按 id 拿到待改背景的 TextView（id 里的 0 表示"第 0 个"，暗示布局里可能还有 1、2…）
        TextView viewById = findViewById(R.id.tv_color_background_0);
        // 【被注释】setBackgroundColor 可以直接传一个 int 颜色值
        //        viewById.setBackgroundColor(Color.BLACK);
        // 改用资源引用：@color/black 定义在本模块 colors.xml 里。
        // setBackgroundResource 接受的是"资源 id"（int 类型），不是颜色值本身——
        // 这也是它和 setBackgroundColor 的本质区别：一个给 id，一个给 ARGB 数值
        viewById.setBackgroundResource(R.color.black);
    }
}