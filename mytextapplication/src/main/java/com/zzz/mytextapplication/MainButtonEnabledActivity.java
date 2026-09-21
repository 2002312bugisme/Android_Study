package com.zzz.mytextapplication;

import android.graphics.Color;
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

public class MainButtonEnabledActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_test;
    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main_button_enabled);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_enabled = findViewById(R.id.btn_enabled);

        Button btn_unenabled = findViewById(R.id.btn_unenabled);

        btn_test = findViewById(R.id.btn_test);

        result = findViewById(R.id.btn_result);

        btn_enabled.setOnClickListener(this);
        btn_unenabled.setOnClickListener(this);
        btn_test.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_enabled) {

            btn_test.setEnabled(true);
            btn_test.setTextColor(Color.BLACK);

        } else if (v.getId() == R.id.btn_unenabled) {

            btn_test.setEnabled(false);
            btn_test.setTextColor(Color.GRAY);

        } else if (v.getId() == R.id.btn_test) {

            String format = String.format(
                    "%s 你点击了：%s",
                    DateUtils.getNowTime(),
                    ((Button) v).getText()
            );

            result.setText(format);
        }
    }
}