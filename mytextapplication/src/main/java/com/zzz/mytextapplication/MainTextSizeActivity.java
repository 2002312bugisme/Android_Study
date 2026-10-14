package com.zzz.mytextapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 文字大小（textSize）演示页。
 *
 * <p>演示要点：本页在 Java 侧<b>什么都不做</b>，不同字号完全写在
 * {@code activity_main_text_size.xml} 里（sp 单位写法、不同数值的对比）。
 * 这样安排是为了把注意力集中在"字号这个属性本身"上。
 *
 * <p>页面的沉浸式（EdgeToEdge）两段代码在本页被注释掉了，所以不做系统栏避让处理。
 *
 * <p>对应布局：{@code res/layout/activity_main_text_size.xml}
 */
public class MainTextSizeActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：调用父类初始化 + 加载布局。仅此两件事。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);

        // 【被注释】开启沉浸式显示；本页注释掉，所以内容从状态栏下方开始
        //        EdgeToEdge.enable(this);

        // 加载布局 XML —— 字号对比全在布局里，Java 不需要参与
        setContentView(R.layout.activity_main_text_size);

        // 【被注释】与上面的 enable() 配套的系统栏内边距处理，一并注释掉了
        //        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
        //            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
        //            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
        //            return insets;
        //        });
    }
}