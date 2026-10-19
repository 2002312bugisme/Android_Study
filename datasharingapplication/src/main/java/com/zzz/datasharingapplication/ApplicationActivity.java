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

public class ApplicationActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText etName, etAge, etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_application);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d(TAG, "onCreate");

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);

        // 提交按钮
        Button btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String age = etAge.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(email)) {
                    Toast.makeText(ApplicationActivity.this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 使用懒汉单例获取 Application，存储数据
                MyApplication app = MyApplication.getInstance();
                app.addUserInfo(new MyApplication.UserInfo(name, age, email));

                Toast.makeText(ApplicationActivity.this, "已保存到 Application", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "提交数据：" + name + ", " + age + ", " + email);

                // 清空输入框
                etName.setText("");
                etAge.setText("");
                etEmail.setText("");
            }
        });

        // 查看按钮
        Button btnShow = findViewById(R.id.btnShow);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ApplicationActivity.this, ShowActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}