package com.zzz.myactivityapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Intent 演示页：对比"显式 Intent"与三种"隐式 Intent"。
 *
 * <p>核心概念：
 * <ul>
 *   <li><b>显式 Intent</b> —— 直接指定目标组件（用 ComponentName 或 Class 对象），
 *       系统不做匹配，把控制权"点名"交给它。本页第一个按钮即为此种写法；</li>
 *   <li><b>隐式 Intent</b> —— 只描述"我要做什么"（setAction）和"对什么数据做"（setData），
 *       由系统在<b>全系统</b>范围内查找声明了匹配 {@code <intent-filter>} 的组件。
 *       后三个按钮分别演示拨号、发短信、自定义动作。</li>
 * </ul>
 *
 * <p>三个隐式分支的 action 与 data 对应关系（记住"协议头 + 号码"的写法）：
 * <pre>
 *   ACTION_DIAL   + tel:123456789    打开拨号盘并填好号码（不会直接拨出，也不需要权限）
 *   ACTION_SENDTO + smsto:123456789  打开短信编辑界面并填好收件人
 *   "android.intent.action.ZZZ"      作者自定义的 action（无 data）
 * </pre>
 *
 * <p>第三个分支值得单独说明：它的 action {@code "android.intent.action.ZZZ"} 在<b>本模块</b>
 * 的 {@code AndroidManifest.xml} 里确实没有组件声明匹配，但它在本仓库的
 * {@code :mytextapplication} 模块里<b>有</b>匹配组件 —— 该模块的 {@code CalculatorActivity}
 * 声明了 {@code <action android:name="android.intent.action.ZZZ"/>} 且 {@code android:exported="true"}。
 *
 * <p>这正是隐式 Intent 的关键特性：匹配范围是<b>全系统已安装的应用</b>，而不只是自己这个 App。
 * 所以本按钮的实际效果是"跳到另一个 App 的页面"（跨应用跳转），前提是
 * {@code :mytextapplication} 也装在同一台设备上；若没装，{@code startActivity} 会抛出
 * {@code ActivityNotFoundException}。
 *
 * <p>工程上的稳妥写法是先判空再启动：
 * <pre>
 *   if (intent.resolveActivity(getPackageManager()) != null) {
 *       startActivity(intent);
 *   }
 * </pre>
 *
 * <p>对应布局：{@code res/layout/activity_intent.xml}
 * <p>涉及 API：{@code Intent#setComponent/setAction/setData/addCategory}、
 * {@code ComponentName}、{@code Uri.parse}、{@code Intent#ACTION_DIAL/ACTION_SENDTO/CATEGORY_DEFAULT}
 */
public class IntentActivity extends AppCompatActivity implements View.OnClickListener {


    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距，并把四个按钮都挂到本 Activity 上。
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
        setContentView(R.layout.activity_intent);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ① 显式跳转按钮
        Button btn_displayed_intent = findViewById(R.id.btn_displayed_intent);
        btn_displayed_intent.setOnClickListener(this);

        // ② 隐式跳转——拨号
        Button btn_implicit_intent_tel = findViewById(R.id.btn_implicit_intent_tel);
        btn_implicit_intent_tel.setOnClickListener(this);

        // ③ 隐式跳转——发短信
        Button btn_implicit_intent_msg = findViewById(R.id.btn_implicit_intent_msg);
        btn_implicit_intent_msg.setOnClickListener(this);

        // ④ 隐式跳转——自定义 action
        Button btn_implicit_intent_my = findViewById(R.id.btn_implicit_intent_my);
        btn_implicit_intent_my.setOnClickListener(this);
    }

    /**
     * 四个按钮共用的点击回调，按 id 分成四个分支。
     *
     * @param v 被点击的 View，四种可能，必须用 {@code v.getId()} 判断来源。
     */
    @Override
    public void onClick(View v) {
        // 演示用的假号码。用变量而不是写死在各分支里，便于统一修改
        String phoneNo = "123456789";
        // 先声明、在各分支里再赋值；这样四个分支共用同一个变量名，结构清晰
        Intent intent;
        if(v.getId() == R.id.btn_displayed_intent){
            // ===== 分支一：显式 Intent（用 ComponentName 指定目标组件）=====
            // 空构造的 Intent 不带任何信息，随后用 setComponent 明确指定目标
            intent = new Intent();
            // ComponentName 的构造形参：① 包上下文（用来取包名）② 目标 Activity 的 Class
            // 它等价于 new Intent(this, ActFinishActivity.class)，
            // 只是把"目标"抽成了一个可单独传递的对象（AIDL、跨应用跳转时会用到）
            ComponentName component = new ComponentName(this, ActFinishActivity.class);
            intent.setComponent(component);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_implicit_intent_tel) {
            // ===== 分支二：隐式 Intent——打开拨号盘 =====
            intent = new Intent();
            // setAction 声明"动作类型"：ACTION_DIAL 表示打开拨号界面并预填号码
            // 注意它不是 ACTION_CALL（后者会直接拨出，且需要 CALL_PHONE 权限）
            intent.setAction(Intent.ACTION_DIAL);
            // setData 声明"操作的数据"。Uri.parse 把字符串解析成 URI 对象，
            // tel: 是电话协议的固定 scheme，后面直接拼号码字符串
            Uri uri = Uri.parse("tel:" + phoneNo);
            intent.setData(uri);
            startActivity(intent);
        }else if (v.getId() == R.id.btn_implicit_intent_msg) {
            // ===== 分支三：隐式 Intent——打开短信编辑界面 =====
            intent = new Intent();
            // ACTION_SENDTO 表示"发送到某个地址"，配合 smsto: 协议即为发短信
            intent.setAction(Intent.ACTION_SENDTO);
            // smsto: 是短信的固定 scheme
            Uri uri = Uri.parse("smsto:" + phoneNo);
            intent.setData(uri);
            startActivity(intent);
        }else if (v.getId() == R.id.btn_implicit_intent_my) {
            // ===== 分支四：隐式 Intent——自定义 action =====
            intent = new Intent();
            // "android.intent.action.ZZZ" 是作者自造的 action 名。
            // 自定义 action 习惯以包名开头以保证全局唯一；用 android.intent.action. 前缀其实不够规范
            intent.setAction("android.intent.action.ZZZ");
            // category 用于进一步约束匹配。CATEGORY_DEFAULT 表示"这是默认处理这类意图的组件"，
            // 隐式 Intent 不带 category 时系统会自动补上 DEFAULT，所以这一行可写可不写
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            // 隐式 Intent 的匹配范围是全系统已安装应用：
            // 本模块没有组件匹配该 action，但 :mytextapplication 的 CalculatorActivity 声明了它
            // （exported=true），所以点击后会启动"另一个 App"的页面
            startActivity(intent);
        }
    }
}