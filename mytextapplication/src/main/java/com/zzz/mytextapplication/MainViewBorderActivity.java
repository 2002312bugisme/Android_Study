package com.zzz.mytextapplication;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.mytextapplication.utils.Utils;

public class MainViewBorderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main_text_border);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 获取控件
        TextView viewById = findViewById(R.id.tv_border_coder);
        // 获取布局参数
        ViewGroup.LayoutParams layoutParams = viewById.getLayoutParams();
        // 修改布局参数中的宽度数值，默认px单位，需要把dp 转换成 px
        layoutParams.width = Utils.dp2px(this,300);
        layoutParams.height = Utils.dp2px(this,50);
        //设置布局参数
        viewById.setLayoutParams(layoutParams);
    }
}