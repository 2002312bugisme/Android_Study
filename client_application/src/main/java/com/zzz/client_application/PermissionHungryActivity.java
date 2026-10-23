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
 * 动态权限申请（<b>饿汉式</b>：一进页面就把全部权限要一遍）
 *
 * <p>饿汉式 vs 懒汉式：
 * <ul>
 *   <li><b>饿汉式</b>（本页）：{@code onCreate} 里就申请，用户还没做任何操作先被连问几个弹窗；</li>
 *   <li><b>懒汉式</b>（{@link PermissionLazyActivity}）：点了具体功能的按钮才申请。</li>
 * </ul>
 *
 * <p>饿汉式的两个实际缺点，跑一遍就能体会到：
 * 1. 一次请求 4 个权限，系统会<b>按权限组连弹两个对话框</b>（通讯录一个、短信一个），
 *    但只回调<b>一次</b> {@code onRequestPermissionsResult}，grantResults 里是 4 个结果；
 * 2. 用户不知道这些权限是干嘛用的，拒绝率通常更高。
 *
 * <p>涉及 API：{@link ContextCompat#checkSelfPermission}、{@link ActivityCompat#requestPermissions}、
 * {@link ActivityCompat#shouldShowRequestPermissionRationale}
 */
public class PermissionHungryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PermissionHungry";

    // ==================== 权限组 ====================
    // 分成三组常量，而不是从一个大数组里按下标取：
    // 下标写法（new String[]{PERMISSION_ALL[0], PERMISSION_ALL[1]}）一旦数组顺序调整就会错，
    // 而且读代码时完全看不出取的是哪两个权限。

    /** 通讯录组 */
    private static final String[] PERMISSION_CONTACTS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };

    /** 短信组 */
    private static final String[] PERMISSION_SMS = new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
    };

    /** 全部权限：饿汉式在 onCreate 里一次性用它申请 */
    private static final String[] PERMISSION_ALL = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
    };

    // ==================== 请求码 ====================
    // 三个请求码对应三种申请来源：单组按钮两个 + 启动时的批量申请一个。
    // 回调里靠它判断"这次结果是谁发起的"，进而决定读哪一组权限。

    /** 单独申请通讯录（重测按钮） */
    public static final int REQUEST_CODE_CONTACTS = 1;
    /** 单独申请短信（重测按钮） */
    public static final int REQUEST_CODE_SMS = 2;
    /** 启动时批量申请全部权限 */
    public static final int REQUEST_CODE_ALL = 3;

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // ⚠️ 这里必须是 activity_permission_hungry，不能复用懒汉式的布局
        setContentView(R.layout.activity_permission_hungry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvStatus = findViewById(R.id.tv_status);

        // 两个重测按钮共用 this 作为监听器，必须逐个绑定
        findViewById(R.id.btn_request_contacts).setOnClickListener(this);
        findViewById(R.id.btn_request_sms).setOnClickListener(this);

        // ===== 饿汉式的核心就这一句：页面一创建就申请，不等用户操作 =====
        // 返回 true = 4 个权限上次就全给了，不会弹窗；
        // 返回 false = 弹窗已发出（可能连弹两个），结果要等 onRequestPermissionsResult。
        PermissionUtil.checkPermission(this, PERMISSION_ALL, REQUEST_CODE_ALL);

        // 把当前状态画出来：权限早就给过的话，这里直接显示"已授予"
        refreshStatus();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn_request_contacts) {
            // 单组重测：只申请通讯录这一组
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
     * @param requestCode  申请时传的请求码，用来区分是单组申请还是批量申请
     * @param permissions  申请的权限数组（批量申请时是 4 个）
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

        } else if (requestCode == REQUEST_CODE_ALL) {
            if (PermissionUtil.checkGrant(grantResults)) {
                onAllGranted();
                return;
            }

            // ===== 批量申请有部分被拒：从 grantResults 里找出是哪一组没给 =====
            // ⚠️ 遍历要用 i++，不能写 i += 2。
            //    虽然 4 个权限是"读+写"成对申请的，但结果数组是 4 项、每项都要看：
            //    用户完全可能只拒掉 WRITE_CONTACTS 而留下 READ_CONTACTS，此时 i 跳过奇数位就漏判了。
            boolean contactsDenied = false;
            boolean smsDenied = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                String permission = permissions[i];
                // 按"权限名"归组，而不是按数组下标归组：下标会随申请顺序变化，权限名不会
                if (Manifest.permission.READ_CONTACTS.equals(permission)
                        || Manifest.permission.WRITE_CONTACTS.equals(permission)) {
                    contactsDenied = true;
                } else if (Manifest.permission.SEND_SMS.equals(permission)
                        || Manifest.permission.READ_SMS.equals(permission)) {
                    smsDenied = true;
                }
            }

            Log.w(TAG, "批量申请未全部通过：通讯录被拒=" + contactsDenied + "，短信被拒=" + smsDenied);
            // 直接显示真实授权情况，比只写一句"申请失败"信息量大
            refreshStatus();

            // 两组里只要有一组是"永久拒绝"就必须去设置页。
            // 这里统一判断、只跳一次 —— 如果分别调 handleDenied，两组都永久拒绝时会连开两个设置页。
            boolean needSettings =
                    (contactsDenied && isPermanentlyDenied(PERMISSION_CONTACTS))
                            || (smsDenied && isPermanentlyDenied(PERMISSION_SMS));
            if (needSettings) {
                jumpToSettings();
            }
        }
    }

    /**
     * 全部权限到手（饿汉式批量申请成功时走这里）
     *
     * <p>真实项目里这里就是"进入主页/开始同步"之类的地方。
     */
    private void onAllGranted() {
        Log.d(TAG, "所有权限已授予");
        tvStatus.setText("通讯录、短信：全部已授予");
    }

    /**
     * 通讯录权限到手后才真正能做的事：查一次系统通讯录，顺便验证权限确实生效
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
     * 单组被拒绝：区分「这次拒绝」和「勾了不再询问」
     *
     * <p>只用于两个单组重测按钮；批量申请的分支因为要给两组分别判断，逻辑写在回调里。
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
