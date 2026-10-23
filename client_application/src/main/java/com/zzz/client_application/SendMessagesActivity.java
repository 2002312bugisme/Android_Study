package com.zzz.client_application;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.client_application.util.PermissionUtil;
import com.zzz.client_application.util.ToastUtil;

/**
 * 发送短信页：<b>选图功能已可用，发送逻辑留待实现</b>
 *
 * <p>界面组成（对应 {@code activity_send_messages.xml}）：
 * 目标号码 / 标题 / 内容 / 图片附件 / 发送按钮。
 *
 * <p>图片附件不是"大图 + 按钮"，而是一个 100dp 的加号方块：
 * 外层 {@link FrameLayout} 叠了两个 {@link ImageView} ——
 * 未选图时显示矢量图标 {@code ic_add_image}，选完图后换成缩略图并把加号隐藏
 * {@code (visibility 切换)}。点击事件绑在外层容器上，整个方块都能点。
 *
 * <p><b>先分清 SMS 和 MMS</b>：
 * <ul>
 *   <li><b>SMS</b>（纯文本）：只有「号码 + 内容」，用 {@code SmsManager.sendTextMessage} 就能发；
 *       需要 {@code SEND_SMS} 权限（清单已声明，运行时还要申请）。</li>
 *   <li><b>MMS</b>（彩信）：才有标题、图片附件。发送要走 MMS 通道，依赖 APN 与数据网络，
 *       代码量比 SMS 大一个数量级，本页没有实现。</li>
 * </ul>
 * 所以本页的「标题」「图片」对 SMS 是无效字段——用 SmsManager 发的时候直接忽略即可。
 *
 * <p>对应布局：{@code activity_send_messages.xml}
 */
public class SendMessagesActivity extends AppCompatActivity {

    /**
     * 目标号码
     */
    private EditText etNumber;
    /**
     * 标题（仅 MMS 有效）
     */
    private EditText etTitle;
    /**
     * 短信内容
     */
    private EditText etContent;
    /**
     * 图片选择区（整块可点，未选图时显示加号）
     */
    private FrameLayout flImagePicker;
    /**
     * 图片预览（选完才显示）
     */
    private ImageView ivImage;
    /**
     * 加号占位图标（选完图后隐藏）
     */
    private ImageView ivAddIcon;
    /**
     * 发送按钮
     */
    private Button btnSend;

    /**
     * 用户选中的图片 uri，发送 MMS 时才会用到
     */
    private Uri selectedImageUri;

    private Uri picUri;
    /**
     * 选图器：ActivityResult API 的注册式写法（比旧版 startActivityForResult 更安全，
     * 进程被回收重建后也能正确恢复）。
     *
     * <p>{@code GetContent} 契约会打开系统文件选择器并返回一个 content:// uri；
     * 用户点返回取消时回调参数为 null。
     *
     * <p>⚠️ 必须在 STARTED 之前注册，所以放在字段初始化这里，
     * 不能写在 {@link #onPickImageClick} 这类点击回调里（那时注册会抛 IllegalStateException）。
     */
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
                if (activityResult.getResultCode() != RESULT_OK) {
                    // 用户按返回取消
                    return;
                }
                // getData() 理论上可能为 null（比如目标页面异常退出），先判空再取 uri
                Intent data = activityResult.getData();
                if (data == null || data.getData() == null) {
                    return;
                }
                picUri = data.getData();
                ivImage.setImageURI(picUri);
                // ⚠️ 关键：iv_image 在布局里初始是 visibility="gone"，
                // setImageURI 只改"显示什么"，不改"显不显示"。少了下面两行，
                // 图其实已经加载好了，但 View 仍是 GONE，看上去就是"没替换成功"。
                ivImage.setVisibility(View.VISIBLE);
                ivAddIcon.setVisibility(View.GONE);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send_messages);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== 绑定控件：id 必须与 activity_send_messages.xml 逐个对应 =====
        etNumber = findViewById(R.id.et_number);
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        flImagePicker = findViewById(R.id.fl_image_picker);
        ivImage = findViewById(R.id.iv_image);
        ivAddIcon = findViewById(R.id.iv_add_icon);
        btnSend = findViewById(R.id.btn_send);

        // ===== 绑定事件：每个控件单独绑，不要指望在一个回调里用 getId() 分发 =====
        // 点击绑在整块 FrameLayout 上，而不是那个加号图标：
        // 图标只有 40dp，点击热区太小；绑在外层容器上，整个 100dp 方块都能点。
        flImagePicker.setOnClickListener(this::onPickImageClick);
        btnSend.setOnClickListener(this::onSendClick);

        // 发送短信需要运行时权限；没有它调 SmsManager 会抛 SecurityException。
        // ⚠️ 这里其实申请早了：发送逻辑还没写，属于"饿汉式"提前申请。
        // 等 onSendClick 实现之后，建议挪到点击时再申请（懒汉式），别让用户一进页面就被问权限。
        PermissionUtil.checkPermission(this,
                new String[]{Manifest.permission.SEND_SMS},
                REQUEST_CODE_SEND_SMS);
    }

    /**
     * 请求码：发送短信权限
     */
    private static final int REQUEST_CODE_SEND_SMS = 1;

    /**
     * 「加号方块」点击：拉起系统文件选择器，只显示图片类型
     *
     * @param view 被点击的图片选择区
     */
    private void onPickImageClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * 「发送」点击：待实现
     *
     * <p>纯文本短信（SMS）的思路：
     * 1. 取号码和内容，两者都做非空校验；
     * 2. 确认 {@code SEND_SMS} 已授予（本页 onCreate 已申请，结果看回调）；
     * 3. 用 {@code SmsManager.getDefault().sendTextMessage(number, null, content, null, null)} 发送；
     * 4. 内容超过一条短信的容量（约 70 个中文字符 / 160 个英文字符）时要先
     * {@code divideMessage(content)} 拆成 list，再逐条 sendTextMessage，否则会被截断；
     * 5. 想确认「发出去了」可以给第 4、5 个参数传 PendingIntent，靠广播收发送状态。
     *
     * <p>如果要做彩信（带标题/图片），得用 MMS 那套 API，本页没实现。
     *
     * @param view 被点击的按钮
     */
    private void onSendClick(View view) {
        sendMms(etNumber.getText().toString().trim(),
                etTitle.getText().toString().trim(),
                etContent.getText().toString().trim());
    }

    private void sendMms(String number, String title, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Intent 的接收者将被准许读取 Intent 携带的 URI 数据
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //彩信发送的目标号码
        intent.putExtra("address", number);
        // 彩信的标题
        intent.putExtra("subject", title);
        // 彩信的内容
        intent.putExtra("sms_body", content);
        // 图片附件
        intent.putExtra(Intent.EXTRA_STREAM, picUri);
        // 附件为图片
        intent.setType("image/*");
        // 因为没有指定要打开什么界面，所以系统会弹出选择窗口
        startActivity(intent);
        ToastUtil.show(this,"选择应用");
    }
}
