package com.zzz.client_application;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.client_application.pojo.ImageItem;
import com.zzz.client_application.util.FileUtil;
import com.zzz.client_application.util.PermissionUtil;
import com.zzz.client_application.util.ToastUtil;
import com.zzz.client_application.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 彩信发送页：MediaStore 选图 + FileProvider 分享
 *
 * <p><b>交互流程（点图片不发送，只切换选中）</b>：
 * <pre>
 * 输入号码/标题/内容 → 点图片选中（可多张）→ 点「发送」→ 系统弹出应用选择器
 * </pre>
 * 为什么不是"点图片直接发送"：
 * 1. 用户点图片可能只想看看大图，一点就直接跳到别的应用，没有反悔的机会；
 * 2. 一张一张点着发，没法一次带上多张附件；
 * 3. 「选中 → 确认 → 发送」是用户对"发消息"这件事的固有预期。
 *
 * <p><b>FileProvider 在这里的作用</b>：
 * Android 7.0(N) 起禁止把 {@code file://} 形式的 Uri 传出本应用（会抛 FileUriExposedException），
 * 必须换成 {@code content://} 的 Uri，并显式授权给接收方读。
 * FileProvider 就是那个"把文件路径翻译成 content:// Uri"的中间人，
 * 而 {@code res/xml/file_paths.xml} 规定了**哪些目录的文件可以被翻译**——
 * 不在配置内的路径调 {@code getUriForFile} 会抛 IllegalArgumentException。
 */
public class ProviderMmsActivity extends AppCompatActivity {

    private static final String TAG = "Zzz";

    private List<ImageItem> imageItemList = new ArrayList<>();

    /**
     * 已选中的图片路径。
     *
     * <p>用 {@link LinkedHashSet} 而不是 ArrayList：
     * - Set 天然去重（同一张图点两次不会重复添加）；
     * - LinkedHashSet 还能保持点击顺序，发送时附件次序稳定。
     */
    private final Set<String> selectedPaths = new LinkedHashSet<>();

    private GridLayout gl_appendix;

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
     * 发送按钮
     */
    private Button btnSend;

    // Android 13(API 33) 起读取图片改用 READ_MEDIA_IMAGES；
    // 在 targetSdk >= 33 的应用上，旧权限 READ_EXTERNAL_STORAGE 不会再授予任何媒体访问权。
    private static final String[] PERMISSION_EXTERNAL_STORAGE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? new String[]{Manifest.permission.READ_MEDIA_IMAGES}
            : new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    private static final int REQUEST_CODE_EXTERNAL_STORAGE = 1;

    /** 选中态的背景色（半透明靛蓝），未选中时清成透明 */
    private static final int COLOR_SELECTED = Color.parseColor("#803F51B5");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_mms);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        gl_appendix = findViewById(R.id.gl_appendix);
        etNumber = findViewById(R.id.et_number);
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        btnSend = findViewById(R.id.btn_send);

        if (PermissionUtil.checkPermission(this, PERMISSION_EXTERNAL_STORAGE, REQUEST_CODE_EXTERNAL_STORAGE)) {
            // 加载图片列表
            loadImageList();
            // 显示图片网格
            showImageGrid();
        }

        // 手动触发让MediaStore扫描入库
        MediaScannerConnection.scanFile(this,
                new String[]{Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()},
                null,
                null);

        // 发送按钮：原来这里是空实现（{}），所以点了没任何反应。
        // 真正的发送动作应该由用户主动确认，而不是点图片就发。
        btnSend.setOnClickListener(v -> onSendClick());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE && PermissionUtil.checkGrant(grantResults)) {
            // 加载图片列表
            loadImageList();

            // 显示图片网格
            showImageGrid();
        }
    }

    /**
     * 「发送」按钮：先校验，再把选中的图片交给系统应用选择器
     *
     * <p>校验顺序是刻意的：号码 → 图片 → 内容。
     * 内容允许为空（彩信可以只发图片），所以不拦。
     */
    private void onSendClick() {
        String number = etNumber.getText().toString().trim();
        if (number.isEmpty()) {
            ToastUtil.show(this, "请先填写目标号码");
            return;
        }
        if (selectedPaths.isEmpty()) {
            ToastUtil.show(this, "请先选择至少一张图片");
            return;
        }

        sendMms(number,
                etTitle.getText().toString().trim(),
                etContent.getText().toString().trim(),
                new ArrayList<>(selectedPaths));
    }

    // 显示图片网络
    private void showImageGrid() {
        gl_appendix.removeAllViews();
        // 重新渲染时清空选中集合：否则上一次选中的路径会指向已经不存在的 ImageView
        selectedPaths.clear();

        for (ImageItem imageItem : imageItemList) {
            // image -> imageView
            ImageView imageView = new ImageView(this);
            Bitmap bitmap = BitmapFactory.decodeFile(imageItem.getData());
            imageView.setImageBitmap(bitmap);
            //设置图像视图的缩放类型
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            //设置图像视图的布局参数
            int px = Utils.dp2px(this, 110);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(px, px);
            imageView.setLayoutParams(layoutParams);
            int padding = Utils.dp2px(this, 5);
            imageView.setPadding(padding, padding, padding, padding);

            // 点击 = 切换选中状态，**不发送**。
            // 原来这里直接调 sendMms(...)，会导致：误触即跳走、只能发一张、正文还没来得及填。
            imageView.setOnClickListener(v -> toggleSelection(imageView, imageItem.getData()));

            //把图像视图添加到网格布局中
            gl_appendix.addView(imageView);
        }

    }

    /**
     * 切换某张图片的选中状态
     *
     * <p>视觉反馈用背景色：ImageView 已有 5dp padding，背景色会在 padding 区域露出来，
     * 视觉上就像一个选中框，比自己画 drawable 省事。
     *
     * @param imageView 被点的图片
     * @param path      图片的绝对路径（同时作为选中集合的 key）
     */
    private void toggleSelection(ImageView imageView, String path) {
        // Set.remove 会返回"是否真的删掉了" —— 正好用来判断之前是不是选中状态
        if (selectedPaths.remove(path)) {
            imageView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            selectedPaths.add(path);
            imageView.setBackgroundColor(COLOR_SELECTED);
        }
        Log.d(TAG, "已选 " + selectedPaths.size() + " 张");
    }

    // 加载图片列表
    private void loadImageList() {
        // 先清空：onCreate 和权限回调都会调本方法，不清会重复累加
        imageItemList.clear();

        // MediaStore 本身有一个数据库
        String[] columns = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATA
        };

        // 300KB 以内
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
                "_size < 307200",
                null,
                "_size DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                ImageItem imageItem = new ImageItem();
                imageItem.setId(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
                imageItem.setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.TITLE)));
                imageItem.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE)));
                imageItem.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                // 只保留"文件真实存在"且"FileProvider 能翻译成 Uri"的图片，
                // 否则后面点发送时会抛 IllegalArgumentException
                if (FileUtil.checkFileUri(this, imageItem.getData())) {
                    imageItemList.add(imageItem);
                }
                Log.d(TAG, "loadImageList: " + imageItem.toString());
            }
            cursor.close();
        }
    }

    /**
     * 把选中的图片 + 正文交给系统，由用户挑一个应用（通常是短信/邮件）完成发送
     *
     * <p><b>这里不直接调用 SmsManager 发彩信</b>：原生发送 MMS 需要自己搭 APN、走 HTTP 提交 PDU，
     * 代码量巨大且各运营商不同。业界通行做法就是 ACTION_SEND 交给系统应用。
     *
     * @param number  目标号码，作为 "address" extra（AOSP 短信应用认这个 key）
     * @param title   彩信标题
     * @param content 正文，对应 "sms_body"
     * @param paths   选中图片的绝对路径集合，至少一张
     */
    private void sendMms(String number, String title, String  content, List<String> paths) {
        // 逐个路径翻译成 content:// Uri，并带上读权限
        ArrayList<Uri> uris = new ArrayList<>();
        for (String path : paths) {
            // getUriForFile 会校验路径是否在 res/xml/file_paths.xml 声明的范围内，
            // 不在范围内直接抛 IllegalArgumentException —— 这是 FileProvider 最常见的崩溃点
            uris.add(FileProvider.getUriForFile(this,
                    getString(R.string.file_provider), new File(path)));
        }

        Intent intent;
        if (uris.size() == 1) {
            // 单张：ACTION_SEND + 单个 Uri
            intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        } else {
            // 多张：必须用 ACTION_SEND_MULTIPLE + ArrayList<Uri>，
            // 用 ACTION_SEND 塞列表是无效的（多数应用会忽略）
            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }

        // 让接收方有权读取这些 content:// Uri，漏了这行对方打开图片会 SecurityException
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //彩信发送的目标号码
        intent.putExtra("address", number);
        // 彩信的标题
        intent.putExtra("subject", title);
        // 彩信的内容
        intent.putExtra("sms_body", content);
        // 附件为图片
        intent.setType("image/*");

        // createChooser 本身就是"选择应用"界面，不需要再 Toast 提示
        startActivity(Intent.createChooser(intent, "选择应用发送"));
    }
}
