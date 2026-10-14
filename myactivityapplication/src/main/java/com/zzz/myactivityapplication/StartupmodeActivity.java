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
 * 启动模式 / Intent Flag 演示页 —— A 页。
 *
 * <p>本页与 {@code StartupmodefinishActivity}（B 页）互相跳转，用来观察
 * <b>启动标志位（Flag）如何影响返回栈（Back Stack）</b>。本页当前启用的是
 * {@link Intent#FLAG_ACTIVITY_REORDER_TO_FRONT}。
 *
 * <p>四个关键概念（代码里也有一段长注释做了同样说明，两者互为对照）：
 * <pre>
 *   standard           每次启动都 new 一个新实例压栈      例：A→B→A→B，返回要按 4 次
 *   FLAG_ACTIVITY_CLEAR_TASK   清空整个 Task 的 Activity 栈后再启动目标
 *                              （通常与 FLAG_ACTIVITY_NEW_TASK 配合，适合"登录成功后清掉登录页"）
 *   FLAG_ACTIVITY_CLEAR_TOP    若目标已在栈中，则销毁目标<b>之上</b>的所有 Activity，回到目标
 *   FLAG_ACTIVITY_REORDER_TO_FRONT  若目标已在栈中，不新建、不销毁其他页，只把目标移到栈顶
 * </pre>
 *
 * <p>两种"生效方式"的差别值得注意：
 * <ul>
 *   <li>用 Intent Flag（本页做法）：写在<b>跳转方</b>，对单次跳转生效；</li>
 *   <li>用 {@code android:launchMode} 属性（写在该 Activity 的 Manifest 声明里）：写在<b>被启动方</b>，
 *       对该 Activity 的所有跳转长期生效。</li>
 * </ul>
 * 两类方式作用于同一套栈规则，只是配置位置不同。
 *
 * <p>实验建议：安装后连续点两个页面的按钮来回跳，
 * 然后按系统返回键观察"需要按几次才退出"，就能直观感受到 Flag 的差别。
 *
 * <p>⚠️ 本文件顶部的两个静态导入 {@code FLAG_ACTIVITY_CLEAR_TASK} / {@code FLAG_ACTIVITY_CLEAR_TOP}
 * 因为对应代码被注释掉了，目前<b>未被使用</b>（第 43 行用的是 Intent.FLAG_ACTIVITY_CLEAR_TOP 全名写法）。
 *
 * <p>对应布局：{@code res/layout/activity_startupmode.xml}
 * <p>涉及 API：{@code Intent#setFlags}、{@code Context#startActivity}
 */
public class StartupmodeActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给"跳转到 B"按钮挂监听器。
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
        setContentView(R.layout.activity_startupmode);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_jump_toB = findViewById(R.id.btn_jump_toB);

        btn_jump_toB.setOnClickListener(this);
    }

    // standard      ：创建新的 Activity
    // CLEAR_TASK    ：清空整个 Activity 栈
    // CLEAR_TOP     ：找到目标，删除目标上面的 Activity
    // REORDER_FRONT ：找到目标，把目标移动到栈顶
    /**
     * 点击回调：带 Flag 跳转到 B 页。
     *
     * @param v 被点击的 View（这里是 btn_jump_toB）。
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_jump_toB) {
            // 显式 Intent 指定目标为 B 页
            Intent intent = new Intent(this, StartupmodefinishActivity.class);
            // 【已注释，可自行切换实验】CLEAR_TOP：若 B 页已在栈中就回到它、并清掉它上面的页面
            //intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            // 当前启用：REORDER_TO_FRONT —— 若 B 页已在栈中，不新建实例，只把它移到栈顶
            // 效果：反复来回跳转时栈里始终只有 A、B 两个实例，不会越点越多
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            intent.setFlags(
//                    Intent.FLAG_ACTIVITY_NEW_TASK |
//                            Intent.FLAG_ACTIVITY_CLEAR_TASK
//            );
            startActivity(intent);


            /*
             * Activity 启动模式 / Intent Flag 对返回栈（Back Stack）的影响：
             *
             * 1. standard
             *    每次启动都创建新的 Activity 实例，并压入栈顶。
             *    例如：A → B → A → B
             *    返回：B → A → B → A → 退出
             *
             * 2. FLAG_ACTIVITY_CLEAR_TASK
             *    清空整个 Task 的 Activity 栈，再启动目标 Activity。
             *    通常与 FLAG_ACTIVITY_NEW_TASK 搭配使用。
             *    适合登录成功后清除登录页面等场景。
             *
             * 3. FLAG_ACTIVITY_CLEAR_TOP
             *    如果目标 Activity 已经存在于当前栈中，
             *    则清除目标 Activity 上面的所有 Activity，
             *    然后回到目标 Activity。
             *
             * 4. FLAG_ACTIVITY_REORDER_TO_FRONT
             *    如果目标 Activity 已经存在于当前栈中，
             *    不创建新的实例，也不删除其他 Activity，
             *    只是将目标 Activity 移动到栈顶。
             *
             * 核心理解：
             *    standard       → 创建新的
             *    CLEAR_TASK     → 清空整个栈
             *    CLEAR_TOP      → 删除目标上面的
             *    REORDER_FRONT  → 把目标移动到最上面
             *
             * Back 操作本质：
             *    从栈顶移除当前 Activity（pop），
             *    显示下面的 Activity；栈为空时退出 Task。
             */


        }
    }
}