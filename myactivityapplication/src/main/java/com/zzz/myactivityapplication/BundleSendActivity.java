package com.zzz.myactivityapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.myactivityapplication.utils.DateUtils;

/**
 * Bundle + 带返回结果的页面跳转 —— <b>发起方</b>页面。
 *
 * <p>本页要解决的问题：A 页把数据发给 B 页，B 页处理完还要把结果<b>带回来</b>给 A 页。
 * 用到的机制是 AndroidX 推荐的 {@link ActivityResultLauncher}（新式 API），共三步：
 * <ol>
 *   <li>在 onCreate 里用 {@code registerForActivityResult(contract, callback)} 注册一个"启动器"，
 *       同时把<b>结果回调</b>写好。注册必须发生在 Activity 处于 STARTED 之前，
 *       所以必须写在 onCreate 里，不能写在按钮点击里；</li>
 *   <li>点击时用 {@code register.launch(intent)} 启动 B 页（而不是 {@code startActivity}）；</li>
 *   <li>B 页 {@code setResult(RESULT_OK, intent)} 后结束，系统回调第 1 步那个 callback，
 *       在回调里取回数据。</li>
 * </ol>
 *
 * <p>数据载体是 {@link Bundle}：本质是一张"键值对表"，用 {@code putXxx(key, value)} 存、
 * 用 {@code getXxx(key)} 取（键名是两边约定的字符串）。Bundle 挂在 Intent 上通过
 * {@code putExtras} 传递，所以它是"随页面跳转携带数据"的标准做法。
 *
 * <p>⚠️ 照实说明两处可改进的写法：
 * <ul>
 *   <li>{@code import android.util.Log} 在本文件里<b>没有被使用</b>（作者用的是 {@code System.out.println}），
 *       属于可清理的冗余导入；生产代码建议用 {@code Log.d} 而不是 System.out——后者不受日志级别控制；</li>
 *   <li>{@code tv_receive} 在回调里通过 {@code findViewById} 临时获取，
 *       而 {@code waiting_for_sending} 却是成员变量，两者风格不统一（功能都正常）。</li>
 * </ul>
 *
 * <p>对应布局：{@code res/layout/activity_bundle_send.xml}
 * <p>涉及 API：{@code registerForActivityResult}、{@code ActivityResultContracts.StartActivityForResult}、
 * {@code ActivityResultLauncher#launch}、{@code ActivityResult#getData/getResultCode}、
 * {@code Intent#getStringExtra}、{@code Bundle#putString}
 */
public class BundleSendActivity extends AppCompatActivity implements View.OnClickListener {

    // "待发送的消息"展示控件
    private TextView waiting_for_sending;

    // 要发送出去的内容（本页固定为 hello world!，仅作演示）
    private String message = "hello world!";
    // 结果启动器。泛型参数是"启动时携带的数据类型"，StartActivityForResult 约定下是 Intent
    private ActivityResultLauncher<Intent> register;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、展示待发送内容、注册结果回调并挂按钮监听。
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
        setContentView(R.layout.activity_bundle_send);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 进页面就把"待发送的消息"显示出来，方便对照 B 页收到的内容是否一致
        waiting_for_sending = findViewById(R.id.waiting_for_sending);
        waiting_for_sending.setText("待发送的消息为：" + message);

        Button btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);

        // 注册"启动后等结果"的启动器。
        // 形参一：ActivityResultContracts.StartActivityForResult() —— 使用标准的 startActivityForResult 语义，
        //        它等价于旧写法 startActivityForResult(intent, REQUEST_CODE)
        // 形参二：结果回调（lambda）。它的形参 o 就是 ActivityResult，包含：
        //        getResultCode() 返回怎么结束的、getData() 返回携带的 Intent
        // ⚠️ 注册必须在 onCreate 内完成（不能放按钮点击里），否则 Activity 已 STARTED 会抛异常
        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            // 理论上不会为 null，但这里仍做防御性判断，避免极端情况下崩溃
            if (o != null) {
                // 调试用：把整个 ActivityResult 打出来，便于观察它的结构
                System.out.println(o.toString());
                // 取出 B 页 setResult 时带回的那个 Intent
                Intent intent = o.getData();

                // 双重判断：Intent 不能为空；且结果码必须是 RESULT_OK（B 页明确表示"处理成功"）
                // 若 B 页用 RESULT_CANCELED 结束或直接按返回键，就不该把它当成有效结果
                if (intent != null && o.getResultCode() == Activity.RESULT_OK) {

                    System.out.println(intent.toString());

                    // 按键名取值。键名 "response_time" / "response_message" 是 A、B 两页的约定，
                    // 写错任何一个都会取到 null（编译期不会报错）
                    String responseTime = intent.getStringExtra("response_time");
                    String responseMessage = intent.getStringExtra("response_message");
                    // %s 依次被时间、消息替换；\n 是换行，让结果多行显示
                    String format = String.format("接收到的信息为：\n请求时间：%s,\n请求信息为：%s", responseTime, responseMessage);
                    TextView tv_receive = findViewById(R.id.tv_receive);
                    tv_receive.setText(format);
                }
            }

        });
    }


    /**
     * 点击回调：把数据塞进 Bundle 并启动 B 页。
     *
     * @param v 被点击的 View（这里是 btn_send）。
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_send) {
            // 显式 Intent 指定目标页为 BundleReceiveActivity
            Intent intent = new Intent(this, BundleReceiveActivity.class);
            // 创建数据袋（键值对表）
            Bundle bundle = new Bundle();
            // putString 形参：① 键名（双方约定的字符串）② 值
            // 这里把"当前时间"和"待发送消息"两个键放进袋子
            bundle.putString("response_time", DateUtils.getNowTime());
            bundle.putString("response_message", message);
            // 把数据袋挂到 Intent 上（Bundle 必须 putExtras 才随跳转传递）
            intent.putExtras(bundle);
            //不进行这样进行跳转了
            //startActivity(intent);

            // 用启动器启动（而不是 startActivity），这样 B 页的返回值才能回到上面注册的回调里
            register.launch(intent);

        }
    }

}