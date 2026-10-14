package com.zzz.myactivityapplication;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 启动模式 / Intent Flag 演示页 —— B 页（由 {@code StartupmodeActivity} 启动）。
 *
 * <p>本页是 A 页的镜像：结构完全相同，只是跳转目标反过来指向 A 页，
 * 并且同样启用了 {@link Intent#FLAG_ACTIVITY_REORDER_TO_FRONT}。
 *
 * <p>因此 A、B 互跳时用到的 Flag 是同一个，反复点击按钮会看到
 * "栈里始终只有 A、B 两个实例"（而不是像 standard 那样越点越多）。
 * 详细概念说明见 A 页（{@code StartupmodeActivity}）的注释。
 *
 * <p>注意本页在 Manifest 里 {@code android:exported="false"} —— 只能被本应用内部跳转，
 * 外部应用无法直接启动它，这是"不对外暴露"的默认安全设置。
 *
 * <p>⚠️ 与 A 页一样，顶部的两个静态导入 {@code FLAG_ACTIVITY_CLEAR_TASK} / {@code FLAG_ACTIVITY_CLEAR_TOP}
 * 因对应代码被注释而未被使用。另外本页<b>没有</b>那个长篇的启动模式说明注释，
 * 想复习概念请回到 A 页看。
 *
 * <p>对应布局：{@code res/layout/activity_startupmodefinish.xml}
 * <p>涉及 API：{@code Intent#setFlags}、{@code Context#startActivity}
 */
public class StartupmodefinishActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给"跳转到 A"按钮挂监听器。
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
        setContentView(R.layout.activity_startupmodefinish);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_jump_toA = findViewById(R.id.btn_jump_toA);

        btn_jump_toA.setOnClickListener(this);
    }

    /**
     * 点击回调：带 Flag 跳回 A 页。
     *
     * @param v 被点击的 View（这里是 btn_jump_toA）。
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_jump_toA){
            Intent intent = new Intent(this, StartupmodeActivity.class);
            // 【已注释，可自行切换实验】CLEAR_TOP：回到栈中已存在的 A 页并清掉它上面的页面
            //intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            // 当前启用：REORDER_TO_FRONT —— 不新建实例，只把已在栈中的 A 页移到栈顶
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            intent.setFlags(
//                    Intent.FLAG_ACTIVITY_NEW_TASK |
//                            Intent.FLAG_ACTIVITY_CLEAR_TASK
//            );
            startActivity(intent);
        }
    }
}