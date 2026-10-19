package com.zzz.datasharingapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ShowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView tvResult = findViewById(R.id.tvResult);

        // 通过单例读取 Application 中存储的全局数据
        MyApplication app = MyApplication.getInstance();
        List<MyApplication.UserInfo> list = app.getUserInfoList();

        if (list.isEmpty()) {
            tvResult.setText("暂无数据");
        } else {
            StringBuilder sb = new StringBuilder("共 " + list.size() + " 条数据：\n\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append(i + 1).append(". ").append(list.get(i).toString()).append("\n\n");
            }
            tvResult.setText(sb.toString());
        }
    }
}