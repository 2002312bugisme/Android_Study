package com.zzz.client_application;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.client_application.util.PermissionUtil;
import com.zzz.client_application.util.ToastUtil;

import java.io.File;

/**
 * 安装本地 APK 演示页：点按钮把 Download 目录下的 app-debug.apk 交给系统安装器
 *
 * <p><b>为什么要用 FileProvider</b>：
 * Android 7.0(N) 起禁止把 {@code file://} 形式的 Uri 传给别的应用（会抛 FileUriExposedException），
 * 必须换成 {@code content://} 的 Uri，并给接收方授予读权限。系统安装器是"别的应用"，
 * 所以这里必须走 FileProvider，而不能直接把文件路径塞进 Intent。
 *
 * <p><b>为什么要 MANAGE_EXTERNAL_STORAGE</b>：
 * Download 目录下的 .apk 属于"非媒体文件"，{@code READ_MEDIA_IMAGES} 这类媒体权限管不到它。
 * Android 11(R) 起要读这类文件，只能申请"所有文件访问权限"（MANAGE_EXTERNAL_STORAGE），
 * 它在设置页里是单独的一个开关，不能通过普通权限弹窗授予。
 *
 * <p>对应布局：{@code activity_provider_apk_activity.xml}
 */
public class ProviderAPKActivity extends AppCompatActivity {

    /**
     * 低版本（Android 11 以下）用的运行时权限。
     *
     * <p>注意：这条在 Android 13+ 已经对媒体/普通文件无效，所以只在 R 以下的 else 分支里用到。
     */
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final int PERMISSIONS_REQUEST_CODE = 1;

    public static final String TAG = "Zzz";

    private Button btn_install;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_provider_apk_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_install = findViewById(R.id.btn_install);

        btn_install.setOnClickListener(v -> {
            // Android 11 之后获取 MANAGE_EXTERNAL_STORAGE 权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Log.d(TAG, "Android 11 +");
                checkAndInstall();
            } else {
                // 如果有权限，直接安装，没有权限则获取权限
                if (PermissionUtil.checkPermission(this, PERMISSIONS, PERMISSIONS_REQUEST_CODE)) {
                    installApk();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE && PermissionUtil.checkGrant(grantResults)) {
            installApk();
        }
    }

    /**
     * Android 11+ 的分支：检查"所有文件访问权限"，没有就跳设置页
     *
     * <p>{@code Environment.isExternalStorageManager()} 是这条权限的唯一判据，
     * 普通 checkSelfPermission 对它无效。
     */
    private void checkAndInstall() {
        // 检查是否拥有 MANAGE_EXTERNAL_STORAGE 权限，没有则跳转到设置页面
        if (!Environment.isExternalStorageManager()) {
            // ⚠️ 这里是第二个坑：原来用的是 ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION，
            // 那个 Action 打开的是"所有应用"的列表页，**不接受 package: 形式的 data**，
            // 带上去会让系统找不到匹配的 Activity，抛：
            //   ActivityNotFoundException: No Activity found to handle Intent
            //   { act=android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION dat=package: }
            //
            // 想"直接跳到本应用的授权页"，必须用 ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION（多一个 APP_）。
            // 两者区别：
            //   ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION     → 列表页，不能带 data
            //   ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION → 本应用详情页，必须带 package data
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            // 带 package: 的 data 是为了直接定位到本应用的授权页，
            // 不带的话会打开"所有应用"的列表，用户还要自己找。
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            // ⚠️ 从 Activity 启动不需要 FLAG_ACTIVITY_NEW_TASK（那是给非 Activity 的 Context 用的）
            startActivity(intent);
        } else {
            installApk();
        }
    }

    /**
     * 把 Download 目录下的 app-debug.apk 交给系统安装器
     *
     * <p>流程：校验文件可解析 → 用 FileProvider 转成 content:// Uri → ACTION_VIEW + APK 的 MIME 类型。
     */
    private void installApk() {
        String apkPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/app-debug.apk";
        Log.d(TAG, "apkPath: " + apkPath);

        // 获取应用包管理器
        PackageManager packageManager = getPackageManager();
        // 解析 APK 文件的包信息，用于前置校验：
        // 文件不存在 / 没有读权限 / 不是合法 APK 时，这里都会返回 null。
        // ⚠️ 注意区分：返回 null 不代表"文件损坏"，也可能只是权限没给到位。
        PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(
                apkPath, PackageManager.GET_ACTIVITIES);
        if (packageArchiveInfo == null) {
            ToastUtil.show(this, "无法读取安装包（检查文件是否存在、是否已授予所有文件访问权限）");
            return;
        }

        // ⚠️ 原来的写法有两个问题：
        //   1) Uri.parse(apkPath) 得到的是 file:///storage/... 这种 Uri；
        //   2) FileProvider.getUriForFile(...) 的返回值没有赋给 uri，白调了一次，
        //      后面 setDataAndType 传的仍然是 file:// Uri。
        // 结果是把 file:// 传给系统安装器，Android 7+ 直接抛 FileUriExposedException。
        // 正确写法：把 getUriForFile 的返回值接住。
        Uri uri = FileProvider.getUriForFile(this,
                getString(R.string.file_provider), new File(apkPath));
        Log.d(TAG, "new uri: " + uri.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 让接收方（系统安装器）有权读取这个 content:// Uri，漏了这行安装器会打不开文件
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 设置 Uri 的数据类型为 APK 文件，系统据此匹配到 PackageInstaller
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        // 启动系统自带的应用安装程序
        startActivity(intent);
    }
}
