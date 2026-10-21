package com.zzz.datasharingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Application 全局数据演示（录入页）：把表单数据存进 Application，再跳页读取
 *
 * <p>演示要点：
 * 1. {@code Application} 的生命周期等于<b>进程</b>生命周期，所以存在它里面的数据
 *    不会因为 Activity 销毁而丢失（这正是它与"成员变量"的关键区别）；
 * 2. 反例提醒：数据也只活在内存里，进程被系统杀掉就全没了，不能当持久化用；
 * 3. 本页只负责"写"，读在 {@link ShowActivity}；
 * 4. 注意 {@code EdgeToEdge} + {@code OnApplyWindowInsetsListener} 这段样板代码：
 *    让内容避开状态栏 / 导航栏，几乎每个 Activity 都要写一遍。
 *
 * <p>对应布局：{@code activity_application.xml}
 * <p>涉及 API：{@link EdgeToEdge}、{@link ViewCompat#setOnApplyWindowInsetsListener}、
 * {@link TextUtils#isEmpty}、{@link Intent}
 */
public class ApplicationActivity extends AppCompatActivity {

    private static final String TAG = "ApplicationActivity";

    // 表单控件：姓名 / 年龄 / 邮箱
    private EditText etName, etAge, etEmail;

    /**
     * 页面创建：绑定控件 + 绑定两个按钮的点击事件
     *
     * @param savedInstanceState 系统保存的临时状态（本页没有需要恢复的数据，未使用）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);                 // 开启"内容延伸到系统栏下方"
        setContentView(R.layout.activity_application);

        // 用 insets 把系统栏占用的高度补成 padding，内容才不会被状态栏挡住
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // systemBars 同时包含状态栏、导航栏、刘海等"系统窗口"
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;                       // 返回 insets 表示"我已处理，继续往下分发"
        });
        Log.d(TAG, "onCreate");

        // 1. 绑定输入框
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);

        // 2. 提交按钮：校验 → 存进 Application → 清空输入
        Button btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // getText() 返回 Editable，必须 toString()；trim() 去掉首尾空格，防止"全是空格"被当成有值
                String name = etName.getText().toString().trim();
                String age = etAge.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                // 任意一项为空就提示并中断（return 是结束本次点击处理）
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(email)) {
                    Toast.makeText(ApplicationActivity.this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 使用单例获取 Application，存储数据（addUserInfo 内部就是往 List 里 add）
                MyApplication app = MyApplication.getInstance();
                app.addUserInfo(new MyApplication.UserInfo(name, age, email));

                Toast.makeText(ApplicationActivity.this, "已保存到 Application", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "提交数据：" + name + ", " + age + ", " + email);

                // 清空输入框，方便连续录入
                etName.setText("");
                etAge.setText("");
                etEmail.setText("");
            }
        });

        // 3. 查看按钮：跳到 ShowActivity 读取全局数据
        Button btnShow = findViewById(R.id.btnShow);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显式 Intent：直接指定目标类，不经过隐式匹配
                startActivity(new Intent(ApplicationActivity.this, ShowActivity.class));
            }
        });
    }

    /**
     * 页面销毁：这里只打日志，用来对比 Application 与 Activity 的生命周期长短
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
