package com.zzz.client_application.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 动态权限工具类：把"检查 + 申请"和"判断结果"分成两个静态方法
 *
 * <p>演示要点：
 * 1. Android 6.0（API 23）起，危险权限（dangerous permission）必须<b>运行时申请</b>，
 *    只在清单里写 {@code <uses-permission>} 是不够的——清单只是"我想要"，运行时授权才是"我拿到了"；
 * 2. 一个功能往往会牵涉多个权限（如通讯录要读 + 写），所以要支持<b>数组批量</b>检查与申请；
 * 3. 申请的"结果"不在本类处理：系统会回调 Activity 的
 *    {@code onRequestPermissionsResult}，那边再调 {@link #checkGrant(int[])} 判读；
 * 4. 本类只做纯粹的判断与转发，不持有 Activity 引用，所以不会泄漏。
 *
 * <p>涉及 API：{@link ContextCompat#checkSelfPermission}、{@link ActivityCompat#requestPermissions}、
 * {@link PackageManager#PERMISSION_GRANTED}
 */
public class PermissionUtil {

    /** 工具类不需要实例，构造器私有化，避免被 new 出来 */
    private PermissionUtil() {
    }

    /**
     * 只检查、不申请：判断这组权限是否<b>全部</b>已授予（不会弹窗）
     *
     * @param activity    上下文（需要 Activity 而不是普通 Context，内部要用它查权限）
     * @param permissions 要检查的权限数组，如 {@code new String[]{Manifest.permission.READ_CONTACTS}}
     * @return true 表示全部已授予；只要有一个没授予就返回 false
     */
    public static boolean hasPermission(Activity activity, String[] permissions) {
        // 空数组视为"没有需要检查的权限"，直接放行，避免下面遍历出空结论
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        for (String permission : permissions) {
            // checkSelfPermission 返回 PERMISSION_GRANTED(0) 或 PERMISSION_DENIED(-1)
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // 有一个没授予就不用再看后面了
                return false;
            }
        }
        return true;
    }

    /**
     * 检查并（必要时）申请：已经有权限就直接返回 true，缺权限就弹系统对话框并返回 false
     *
     * <p>⚠️ 返回 false 不等于"用户拒绝了"，只代表"弹窗已经发出、结果还没回来"。
     * 真正的结果要等 {@code onRequestPermissionsResult} 回调，所以调用方在 false 分支里
     * <b>不要</b> 把权限当成失败处理，只需提示"等待用户选择"。
     *
     * @param activity    发起申请的 Activity（系统会把结果回调给它）
     * @param permissions 要申请的权限数组，一次弹窗会把数组里缺的权限都问一遍
     * @param requestCode 请求码，回调时用它区分"这次是哪个功能发起的"，取值要 ≥ 0
     * @return true 表示本来就有权限（无需弹窗）；false 表示已发起申请、等待回调
     */
    public static boolean checkPermission(Activity activity, String[] permissions, int requestCode) {
        // Android 6.0 之前是安装即授权，根本没有运行时申请这回事。
        // 本项目 minSdk = 28，这个分支实际永远成立，保留它是为了让"分界线在哪"这件事看得见。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!hasPermission(activity, permissions)) {
                // 弹系统权限对话框：用户在弹窗上的选择，会通过 Activity 的回调返回
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
                return false;
            }
        }
        return true;
    }

    /**
     * 判读回调里的授权结果数组
     *
     * <p>参数顺序与 {@code permissions} 一一对应：{@code grantResults[0]} 是第一个权限的结果。
     *
     * @param grantResults 系统回调给的每个权限的结果，取值为 {@link PackageManager#PERMISSION_GRANTED}
     *                     或 {@link PackageManager#PERMISSION_DENIED}
     * @return true 表示这一批权限<b>全部</b>授予；false 表示至少有一个被拒（或结果为空）
     */
    public static boolean checkGrant(int[] grantResults) {
        // 空数组/空引用：请求被系统打断或取消，不能算成功。
        // 早期版本这里只判 null，空数组会掉进"循环没执行 → 直接 return true"的坑，属于误判。
        if (grantResults == null || grantResults.length == 0) {
            return false;
        }
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                // 批量申请时，只要有一个被拒，整个功能就用不了，统一按失败处理
                return false;
            }
        }
        return true;
    }
}
