package com.zzz.serviceapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.RemoteService;

import java.util.List;

/**
 * 隐式启动与跨应用启动的对照实验（对应文章「Android 5.0 以上的隐式启动问题」）。
 * 对应布局：activity_implicit_start.xml（四个按钮 + 结果回显）
 * 涉及 API：startService()、Intent.setPackage()、PackageManager.queryIntentServices()
 * <p>
 * 四个按钮对应四种启动方式，结果都打印在界面上：
 * 1. 显式启动 —— 平时最常用的写法，一定能成；
 * 2. 裸隐式启动 —— Android 5.0 起这里会抛 IllegalArgumentException，是本页的主角；
 * 3. 隐式 + setPackage() —— 官方推荐的修复方式，只补一个包名就够了；
 * 4. 隐式转显式 —— 文章里的 getExplicitIntent() 方案，用 PackageManager 查出唯一匹配项再拼 ComponentName。
 */
public class ImplicitStartActivity extends AppCompatActivity {

    /** 日志 TAG */
    private static final String TAG = "ZZZ";

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_implicit_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvStatus = findViewById(R.id.tv_status);

        // 方式一：显式启动。Intent 里已经带了目标类，系统不需要再查匹配
        findViewById(R.id.btn_explicit).setOnClickListener(v -> {
            try {
                startService(new Intent(this, RemoteService.class));
                show("1. 显式启动：成功\nnew Intent(this, RemoteService.class)");
            } catch (Exception e) {
                showError("1. 显式启动", e);
            }
        });

        // 方式二：裸隐式启动。只设 action，不设 package，也不设 component。
        // Android 5.0 起系统在 validateServiceIntent() 里直接抛异常，不会走到服务端
        findViewById(R.id.btn_implicit).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(RemoteService.ACTION_REMOTE_START);
            try {
                startService(intent);
                show("2. 裸隐式启动：竟然成功了（说明当前系统放宽了限制）");
            } catch (Exception e) {
                // 预期会捕获到 IllegalArgumentException: Service Intent must be explicit
                showError("2. 裸隐式启动", e);
            }
        });

        // 方式三：补一个包名。component 依然为空，但 package 有值，校验就通过了
        findViewById(R.id.btn_implicit_pkg).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(RemoteService.ACTION_REMOTE_START);
            // 这一行就是全部修复内容
            intent.setPackage(getPackageName());
            try {
                startService(intent);
                show("3. 隐式 + setPackage：成功\nsetPackage(\"" + getPackageName() + "\")");
            } catch (Exception e) {
                showError("3. 隐式 + setPackage", e);
            }
        });

        // 方式四：查匹配项再转显式，适用于「不确定由哪个应用响应这个 action」的场景
        findViewById(R.id.btn_convert).setOnClickListener(v -> {
            Intent implicit = new Intent();
            implicit.setAction(RemoteService.ACTION_REMOTE_START);
            Intent explicit = getExplicitIntent(this, implicit);
            if (explicit == null) {
                // 匹配不到、或匹配到多个，都返回 null —— 隐式启动要求「唯一确定」
                show("4. 隐式转显式：没有找到唯一匹配的服务");
                return;
            }
            try {
                startService(explicit);
                ComponentName cn = explicit.getComponent();
                show("4. 隐式转显式：成功\n" + (cn == null ? "" : cn.flattenToShortString()));
            } catch (Exception e) {
                showError("4. 隐式转显式", e);
            }
        });
    }

    /**
     * 把隐式 Intent 转换成显式 Intent。
     * <p>
     * 思路：先用 action 向 PackageManager 查询所有能响应的服务，
     * 只有匹配结果唯一时才有意义（多个匹配说明无法确定目标），
     * 拿到它的包名和类名拼成 ComponentName，再塞进新的 Intent。
     *
     * @param context        用于获取 PackageManager
     * @param implicitIntent 带 action 的隐式 Intent
     * @return 显式 Intent；匹配不到或匹配到多个时返回 null
     */
    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        PackageManager pm = context.getPackageManager();
        // 第二个参数是查询标志，0 表示不做额外过滤
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // 必须恰好一个匹配项，否则无法确定要启动谁
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // 复制原 Intent 以保留 extras 等数据，再补上 component
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    /**
     * 把结果写到界面上，便于截图。
     *
     * @param text 要显示的文字
     */
    private void show(String text) {
        tvStatus.setText(text);
        Log.d(TAG, text);
    }

    /**
     * 把异常类型和消息一并显示出来。
     *
     * @param step 哪一步失败了
     * @param e    捕获到的异常
     */
    private void showError(String step, Exception e) {
        String text = step + "：\n" + e.getClass().getSimpleName() + "\n" + e.getMessage();
        tvStatus.setText(text);
        Log.e(TAG, step + " 失败", e);
    }
}
