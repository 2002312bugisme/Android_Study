package com.zzz.client_application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 客户端首页：唯一的职责是给出一个进入"用户信息录入"页的入口
 *
 * <p>为什么要这一页：{@code ContentWriteActivity} 在清单里是 {@code exported="false"}，
 * 只能由本应用内部拉起 —— 直接把 launcher 挂成 ContentWriteActivity 也可以，
 * 但那样"返回"就会直接退出应用，体验上不如图层首页。
 *
 * <p>对应布局：{@code activity_main.xml}
 * <p>涉及 API：{@link Intent}（显式跳转）
 */
public class MainActivity extends AppCompatActivity {

    /**
     * 页面创建：处理系统栏内边距 + 绑定"进入录入页"按钮
     *
     * @param savedInstanceState 系统保存的临时状态（本页没有需要恢复的数据）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 显式 Intent：目标类写死，只会跳到本应用的这个页面
        Button btnOpenWrite = findViewById(R.id.btn_open_write);
        btnOpenWrite.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ContentWriteActivity.class)));
    }
}
