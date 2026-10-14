package com.zzz.mytextapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 文本（TextView）最小演示页：在代码里给 TextView 设置文字资源。
 *
 * <p>演示要点：{@code res/layout/activity_main_text.xml} 里的 TextView 故意只留 id、不写文字，
 * 由本页 {@code setText} 填进去。这样可以看出「文字既可以写在布局里，也可以写在代码里」。
 *
 * <p>页面的沉浸式（EdgeToEdge）相关两段代码在本页被注释掉了，
 * 因此本页不做「让内容避开状态栏」的处理，布局会从状态栏下方直接开始。
 *
 * <p>对应布局：{@code res/layout/activity_main_text.xml}
 * <p>涉及 API：{@code AppCompatActivity#setContentView}、{@code View#findViewById}、
 * {@code TextView#setText(int)}
 */
public class MainTextActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);

        // 【被注释】开启沉浸式显示：允许内容延伸到状态栏 / 导航栏区域。
        // 本页注释掉它，所以页面是"传统"布局——内容从状态栏下方开始，不会被系统栏盖住
        //        EdgeToEdge.enable(this);

        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_main_text);

        // 【被注释】与上面的 enable() 配套：把系统栏占掉的尺寸补成根布局内边距。
        // 既然没开沉浸式，也就不需要补内边距，所以整段一起注释掉了
        //        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //            return insets;
        //        });

        // 按 id 拿到布局里的 TextView（泛型推断为 TextView，无需强转）
        TextView textView = findViewById(R.id.tv_hello);
        // 设置文字。注意这里传的是 R.string.hello 这个 int ——
        // setText(int) 重载会被当成"资源 id"，由 TextView 自己去 strings.xml 里取对应文本；
        // 若想直接显示数字文本，必须写成 setText(String.valueOf(123)) 之类才不会有歧义
        textView.setText(R.string.hello);

    }
}