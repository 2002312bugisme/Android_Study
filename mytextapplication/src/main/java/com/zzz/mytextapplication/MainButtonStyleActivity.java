package com.zzz.mytextapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.mytextapplication.utils.DateUtils;

import org.w3c.dom.Text;

public class MainButtonStyleActivity extends AppCompatActivity {

    private TextView viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main_button_style);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewById = findViewById(R.id.tv_result);
    }

    public void doClick(View view) {
        String format = String.format("%s, 你点击了按钮：%s", DateUtils.getNowTime(), ((Button) view).getText());
        viewById.setText(format);
    }
}