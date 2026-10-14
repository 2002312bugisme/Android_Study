package com.zzz.myactivityapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.myactivityapplication.utils.DateUtils;

/**
 * Bundle + 带返回结果的页面跳转 —— <b>接收方</b>页面（由 {@code BundleSendActivity} 启动）。
 *
 * <p>本页做两件事：
 * <ol>
 *   <li><b>收</b>：从启动它的 Intent 里取出 Bundle，把传来的两个键（response_time、response_message）
 *       显示出来；</li>
 *   <li><b>回</b>：点按钮时构造一个新的 Bundle 作为"回礼"，用 {@code setResult(RESULT_OK, intent)}
 *       挂到结果上，再 {@code finish()} 结束自己，于是 A 页的结果回调被触发、拿到数据。</li>
 * </ol>
 *
 * <p>读取 Bundle 的两种等价写法（本页把两种都用了一遍，所以看起来"同一件事做了两次"）：
 * <pre>
 *   写法一：先 intent.getExtras() 拿到 Bundle，再 bundle.getString(key)
 *   写法二：直接 intent.getStringExtra(key)（内部就是去 Bundle 里取，更简洁）
 * </pre>
 * 注意这两种写法所取到的值完全一样，所以第 49 行与第 54 行的 setText 是重复覆盖，
 * 前一次的赋值会被后一次覆盖掉 —— 属于演示性质，不删也无害。
 *
 * <p>⚠️ 照实说明三个可改进点：
 * <ul>
 *   <li>{@code import android.util.Log} 用到了（下方的 Log.d），但同一个变量名下
 *       打了两次内容几乎相同的日志（tag 分别为 response1 与 response），略显冗余；</li>
 *   <li>本页声明的 {@link ActivityResultLauncher} / {@link ActivityResultCallback} /
 *       {@link ActivityResult} / {@link ActivityResultContracts} 四个 import <b>都没有实际使用</b>
 *       （本页是被调用方，不需要结果启动器），属于复制粘贴残留；</li>
 *   <li>{@code btn_send_receive} 命名为"发送"，但它实际做的是"设置返回值并结束本页"。</li>
 * </ul>
 *
 * <p>对应布局：{@code res/layout/activity_bundle_receive.xml}
 * <p>涉及 API：{@code Activity#getIntent}、{@code Intent#getExtras/getStringExtra}、
 * {@code Activity#setResult}、{@code Activity#finish}、{@code Log#d}
 */
public class BundleReceiveActivity extends AppCompatActivity implements View.OnClickListener {

    // 用来显示"从上一页收到的内容"
    private TextView tv_receive_receive;
    private Button btn_send_receive;
    // 要回传给上一页的内容（同样固定为 hello world!）
    private String message = "hello world!";
    // 用来显示"将要返回的内容"，让用户点之前就知道会传回什么
    private TextView waiting_for_returning;


    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、读取上一页传来的数据并显示、
     * 把"回传按钮"挂上监听器、展示待返回内容。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。
     *                           注意本页读的数据来自 {@code getIntent()} 而不是它——两者来源不同：
     *                           getIntent 是"调用方传来的"，savedInstanceState 是"系统恢复用的"。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);
        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_bundle_receive);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_receive_receive = findViewById(R.id.tv_receive_receive);
        // getIntent() 返回"启动本页的那个 Intent"（由上一页 new 出来并交给系统的那个对象）
        Intent intent = getIntent();
        // 取出挂在 Intent 上的数据袋
        Bundle extras = intent.getExtras();
        // ===== 读取写法一：先取 Bundle，再按 key 取值 =====
        // 若上一页没放这两个键，这里会返回 null（不会抛异常），所以后续要允许为 null
        String responseTime1 = extras.getString("response_time");
        String responseMessage1 = extras.getString("response_message");
        // Log.d 形参：① tag（日志标签，用来在 Logcat 里过滤）② msg（日志内容）
        // 注意这里把两个字符串直接相加，没有分隔符，看起来会粘在一起
        Log.d("response1", responseTime1 + responseMessage1);
        String format1 = String.format("接收到的信息为：\n请求时间：%s,\n请求信息为：%s", responseTime1, responseMessage1);
        tv_receive_receive.setText(format1);
        // ===== 读取写法二：直接从 Intent 取（更常用、更简洁）=====
        String responseTime = intent.getStringExtra("response_time");
        String responseMessage = intent.getStringExtra("response_message");
        String format = String.format("接收到的信息为：\n请求时间：%s,\n请求信息为：%s", responseTime, responseMessage);
        Log.d("response", responseTime + responseMessage);
        // 再次 setText，会把上面 format1 的内容覆盖掉（两者内容相同，所以看不出差别）
        tv_receive_receive.setText(format);

        btn_send_receive = findViewById(R.id.btn_send_receive);
        btn_send_receive.setOnClickListener(this);

        waiting_for_returning = findViewById(R.id.waiting_for_returning);
        waiting_for_returning.setText("待返回的信息：" + message);

    }

    /**
     * 回传按钮的点击回调：打包结果 → 设置结果码 → 结束本页。
     *
     * @param v 被点击的 View（这里是 btn_send_receive）。
     */
    @Override
    public void onClick(View v) {
        // 承载回传数据的 Intent。这里不需要 startActivity，所以无需指定目标组件
        Intent intent = new Intent(this, BundleSendActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("response_time", DateUtils.getNowTime());
        bundle.putString("response_message", message);
        intent.putExtras(bundle);
        //携带意图返回上一个界面。RESULT_OK 表示处理成功
        // setResult 形参：① 结果码（Activity.RESULT_OK / RESULT_CANCELED，由调用方判断）
        //                ② 要回传的数据 Intent
        // 只有配合"上一页用 ActivityResultLauncher 启动"，这个结果才会被回调收到
        setResult(Activity.RESULT_OK, intent);
        //结束当前活动页
        // finish 后返回栈弹出栈顶，上一页重新显示，同时系统把上面的 setResult 结果交回给它
        finish();
    }

}