package com.zzz.mytextapplication;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainTextColorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main_text_color);


//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

//        TextView viewById = findViewById(R.id.tv_color_code);

//        viewById.setTextColor(Color.GREEN);
        //RGB 八位十六进制数
//        viewById.setTextColor(0x11111111);
        //RGB 六位十六进制数  透明度 默认 00 透明
//        viewById.setTextColor(0x00ff00);

        //设置背景颜色
        TextView viewById = findViewById(R.id.tv_color_background_0);
//        viewById.setBackgroundColor(Color.BLACK);
        viewById.setBackgroundResource(R.color.black);
    }
}