package com.zzz.client_application;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.client_application.util.PermissionUtil;

/**
 * 动态权限申请（懒汉式：点了按钮才申请）
 *
 * <p>主线只有四步：
 * 1. 清单里声明 {@code <uses-permission>}（"我想要"）；
 * 2. {@code checkSelfPermission} 查是否已授权（"有没有"）；
 * 3. {@code requestPermissions} 申请，系统弹窗（"去要"）；
 * 4. {@code onRequestPermissionsResult} 收回结果（"结果是什么"）。
 *
 * <p>涉及 API：{@link ContextCompat#checkSelfPermission}、{@link ActivityCompat#requestPermissions}、
 * {@link ActivityCompat#shouldShowRequestPermissionRationale}
 */
public class PermissionLazyActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PermissionLazy";

    /** 通讯录权限组：读 + 写，一次申请只弹一个框 */
    private static final String[] PERMISSION_CONTACTS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };

    /** 短信权限组 */
    private static final String[] PERMISSION_SMS = new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
    };

    /** 请求码：回调时靠它区分是哪一次申请（int，不是权限数组） */
    public static final int REQUEST_CODE_CONTACTS = 1;
    public static final int REQUEST_CODE_SMS = 2;

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permission_lazy);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvStatus = findViewById(R.id.tv_status);

        // 两个按钮共用 this 作为监听器，必须逐个绑定；少绑一个点了就没反应，且不报错
        findViewById(R.id.btn_request_contacts).setOnClickListener(this);
        findViewById(R.id.btn_request_sms).setOnClickListener(this);

        refreshStatus();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_request_contacts) {
            // 返回 true = 本来就有权限，直接干活；返回 false = 弹窗已发出，结果等回调
            if (PermissionUtil.checkPermission(this, PERMISSION_CONTACTS, REQUEST_CODE_CONTACTS)) {
                onContactsGranted();
            }
        } else if (id == R.id.btn_request_sms) {
            if (PermissionUtil.checkPermission(this, PERMISSION_SMS, REQUEST_CODE_SMS)) {
                onSmsGranted();
            }
        }
    }

    /**
     * 权限结果回调
     *
     * <p><b>必须是这个 3 参数版本</b>。API 36 的 Activity 多了一个带 {@code deviceId} 的 4 参数重载，
     * 但系统与 AndroidX 分发走的是 3 参数这条，只重写 4 参数版本会导致弹窗关掉后本方法不执行。
     *
     * @param requestCode  申请时传的请求码
     * @param permissions  申请的权限数组
     * @param grantResults 与 permissions 一一对应的结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // 必须调 super：ComponentActivity 在这里给 ActivityResult API 分发结果
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 用请求码（int）匹配，不能写 requestCode == PERMISSION_CONTACTS（int 比 String[]）
        if (requestCode == REQUEST_CODE_CONTACTS) {
            if (PermissionUtil.checkGrant(grantResults)) {
                onContactsGranted();
            } else {
                handleDenied("通讯录", PERMISSION_CONTACTS);
            }
        } else if (requestCode == REQUEST_CODE_SMS) {
            if (PermissionUtil.checkGrant(grantResults)) {
                onSmsGranted();
            } else {
                handleDenied("短信", PERMISSION_SMS);
            }
        }
    }

    /**
     * 权限到手后才真正能做的事：查一次系统通讯录，顺便验证权限确实生效
     *
     * <p>Cursor 必须关闭，这里用 try-with-resources 自动关。
     */
    private void onContactsGranted() {
        int count = 0;
        try (Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null)) {
            if (cursor != null) {
                count = cursor.getCount();
            }
        }
        Log.d(TAG, "通讯录权限已授予，联系人 " + count + " 条");
        tvStatus.setText("通讯录：已授予（联系人 " + count + " 条）");
    }

    /** 短信权限到手（真发短信有费用，这里只记录权限状态） */
    private void onSmsGranted() {
        Log.d(TAG, "短信权限已授予");
        tvStatus.setText("短信：已授予");
    }

    /**
     * 被拒绝：区分「这次拒绝」和「勾了不再询问」
     *
     * @param label       功能名，只用于拼日志和状态文本
     * @param permissions 本次申请的权限组
     */
    private void handleDenied(String label, String[] permissions) {
        Log.w(TAG, label + "权限被拒绝");
        if (isPermanentlyDenied(permissions)) {
            // 系统不会再弹窗，只能去设置页手动开
            tvStatus.setText(label + "：永久拒绝，需到设置里开");
            jumpToSettings();
        } else {
            tvStatus.setText(label + "：被拒绝，可再点一次");
        }
    }

    /**
     * 是否已被"永久拒绝"（勾了不再询问）
     *
     * <p>判据：{@code shouldShowRequestPermissionRationale} 返回 false。
     * 注意它在"首次申请之前"也返回 false，所以只能在收到拒绝回调后调用。
     */
    private boolean isPermanentlyDenied(String[] permissions) {
        if (PermissionUtil.hasPermission(this, permissions)) {
            return false;
        }
        for (String permission : permissions) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED;
            if (!granted && ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // 还有能再弹窗的权限，就不算永久拒绝
                return false;
            }
        }
        return true;
    }

    /** 刷新状态：只读不申请 */
    private void refreshStatus() {
        boolean contacts = PermissionUtil.hasPermission(this, PERMISSION_CONTACTS);
        boolean sms = PermissionUtil.hasPermission(this, PERMISSION_SMS);
        tvStatus.setText("通讯录：" + (contacts ? "已授予" : "未授予")
                + "\n短信：" + (sms ? "已授予" : "未授予"));
    }

    /** 跳到本应用详情页（权限被永久拒绝后唯一的手动开关入口） */
    private void jumpToSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        // 必须带 package: 的 data，否则打开的是列表页而不是本应用详情
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }
}
