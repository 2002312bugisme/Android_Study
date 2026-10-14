package com.zzz.myactivityapplication;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * "读取资源"演示页：演示两种数据来源的读取方式。
 *
 * <p>一、读字符串资源（{@code res/values/strings.xml}）
 * <pre>
 *   getString(R.string.string_data)   // Activity 继承自 Context，可直接调用
 * </pre>
 * 传进去的是资源 id（int），由系统去对应的 values 目录里取当前语言/设备环境下的那份文本。
 *
 * <p>二、读清单文件里的 metadata（{@code AndroidManifest.xml}）
 * 本页在 Manifest 中注册了这样一段：
 * <pre>
 *   &lt;activity android:name=".ReadResourceActivity" ...&gt;
 *       &lt;meta-data android:name="metadata" android:value="Hello World!" /&gt;
 *   &lt;/activity&gt;
 * </pre>
 * 读取步骤：拿 PackageManager → 用"本组件名 + GET_META_DATA 标志"查自己的 ActivityInfo
 * → 从 ActivityInfo.metaData 这个 Bundle 里按键名取值。
 *
 * <p>⚠️ 照实说明本页的冗余之处：同一个 TextView（id 都是 read_string）被 findViewById 了两次、
 * setText 了两次，第 32 行与第 36 行的效果完全一样，属于"两种写法都演示一遍"的重复代码。
 *
 * <p>对应布局：{@code res/layout/activity_read_resource.xml}
 * <p>涉及 API：{@code Context#getString}、{@code Context#getPackageManager}、
 * {@code Context#getComponentName}、{@code PackageManager#getActivityInfo}、
 * {@code ActivityInfo#metaData}、{@code Bundle#getString}
 */
public class ReadResourceActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距，然后读取字符串资源与清单 metadata 并显示。
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
        setContentView(R.layout.activity_read_resource);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== 读字符串资源：第一种写法（先取控件 → 求值 → 设置）=====
        TextView read_string = findViewById(R.id.read_string);

        // getString 的形参是资源 id；返回当前环境下对应的字符串
        String stringData = getString(R.string.string_data);

        read_string.setText(stringData);

        // 读取 strings.xml
        // ===== 读字符串资源：第二种写法（求值与设置合成一行）=====
        // 变量名 readString 与上面的 read_string 只是下划线差异，且指向同一个控件，属于重复
        TextView readString = findViewById(R.id.read_string);
        readString.setText(getString(R.string.string_data));

        // 读取 AndroidManifest.xml 中的 metadata
        TextView readMetadata = findViewById(R.id.read_metadata);

        // 拿到包管理器：所有"查询应用自身或其它应用信息"的操作都通过它
        PackageManager packageManager = getPackageManager();

        // getActivityInfo 声明会抛 NameNotFoundException（找不到该组件时），所以必须 try-catch
        try {
            // getActivityInfo 形参：① getComponentName() = 本 Activity 的组件名（包名 + 类名）
            //                       ② GET_META_DATA 标志位，表示"请把 meta-data 一起读出来"
            //                         不加这个标志的话，activityInfo.metaData 会是 null
            ActivityInfo activityInfo = packageManager.getActivityInfo(
                    getComponentName(),
                    PackageManager.GET_META_DATA
            );

            // meta-data 在读出来后就装在一个 Bundle 里，键名就是 Manifest 里的 android:name
            Bundle metaData = activityInfo.metaData;

            // 防御性判空：若 Manifest 没写 meta-data，这里就是 null
            if (metaData != null) {
                // "metadata" 这个键名与 Manifest 里 <meta-data android:name="metadata"> 完全对应
                String metadata = metaData.getString("metadata");
                readMetadata.setText(metadata);
            }

        } catch (PackageManager.NameNotFoundException e) {
            // 正常情况下不会走到这里（本 Activity 一定存在于自己的包里），
            // 打印堆栈便于排查即可；也可以给用户一个提示
            e.printStackTrace();
        }

    }
}