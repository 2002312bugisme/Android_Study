package com.zzz.intermediatecontrols;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 提醒对话框（AlertDialog）演示页。
 *
 * <p>演示要点：用 {@link AlertDialog.Builder} 建造器一步一步拼出一个对话框——设标题、设内容、
 * 设「肯定按钮」和「否定按钮」以及各自的点击回调，最后 {@code create()} 再 {@code show()}。
 *
 * <p>这是 Android 里最常见的交互弹窗写法；按钮点击后不改动对话框自身，而是去改页面上的 TextView，
 * 用来证明回调确实被执行了。
 *
 * <p>对应布局：{@code res/layout/activity_alert_dialog.xml}（只要一个按钮 + 一个 TextView）
 * <p>涉及 API：{@code AlertDialog.Builder#setTitle/setMessage/setPositiveButton/setNegativeButton/create/show}、
 * {@code View.OnClickListener}、{@code TextView#setText}
 */
public class AlertDialogActivity extends AppCompatActivity implements View.OnClickListener {

    // 页面上的结果 TextView，对话框按钮被点击后把结果显示在这里，所以提升为成员变量供回调访问
    private TextView tv_message;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给按钮挂监听器、缓存 TextView 引用。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类：完成窗口、主题等基础初始化，必须放在最前
        super.onCreate(savedInstanceState);
        // 开启沉浸式显示，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML，解析成 View 树作为页面内容
        setContentView(R.layout.activity_alert_dialog);

        // 因为内容已经顶到系统栏下面，这里把系统栏占掉的尺寸补成根布局的 padding，避免内容被遮住。
        // 监听器形参：v = 被监听的根布局（id=main）；insets = 本次窗口插入信息；
        //             返回值 = 原样回传，表示处理完毕、继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取「系统栏」这一类区域（状态栏 + 导航栏）
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四个方向原样设为根布局内边距
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 按 id 找到触发按钮；泛型由左侧类型推断，无需强转
        Button btn_dialog = findViewById(R.id.btn_dialog);
        // 让本 Activity（已实现 View.OnClickListener）处理点击，点击后进入下面的 onClick
        btn_dialog.setOnClickListener(this);

        // 缓存结果 TextView 的引用，稍后在对话框回调里使用
        tv_message = findViewById(R.id.tv_message);

    }

    /**
     * 按钮点击回调（本页实现了 {@link View.OnClickListener} 接口，所以这里是统一入口）。
     *
     * <p>逻辑：每次点击都重新构造并弹出一个 AlertDialog。
     *
     * @param v 被点击的 View，即 {@code btn_dialog}。
     *          本页只有一个可点控件，所以无需用 {@code v.getId()} 判断来源。
     */
    @Override
    public void onClick(View v) {
        // 创建提示对话框的建造器（Builder 模式：先攒配置，最后 create）
        // 构造参数 this 是 Context，对话框需要依附它来取主题与窗口
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置对话框的标题文本
        builder.setTitle("Dear User : ");
        // 设置对话框的内容文本
        builder.setMessage("Are you really gonna uninstall me?");
        // 设计对话框的肯定按钮文本及其点击监听器
        // 回调形参：dialog = 当前这个 AlertDialog 对象；which = 被点的按钮标识（此处用不到，仅保留形参）
        builder.setPositiveButton("uninstall",(DialogInterface.OnClickListener) (dialog, which) -> {
            // 点肯定按钮：把结果写到页面 TextView 上
            tv_message.setText("Although reluctantly, but can only leave");
        });
        // 设置对话框的否定按钮文本及其点击监听器（形参含义同上）
        builder.setNegativeButton("Let me think about it", (dialog, which) -> {
            tv_message.setText("Let me stay with you three hundred and sixty-five days and nights");
        });
        // 构建提醒对话框对象（Builder 攒完配置后真正 new 出 AlertDialog）
        AlertDialog alertDialog = builder.create();
        // 显示提醒对话框（不调用它对话框不会出现）
        alertDialog.show();

    }
}