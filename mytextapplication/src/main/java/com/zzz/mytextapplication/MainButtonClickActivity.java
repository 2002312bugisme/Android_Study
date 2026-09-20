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

public class MainButtonClickActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_message_all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main_button_click);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button btn_single = findViewById(R.id.btn_single);


        TextView tv_message_single = findViewById(R.id.tv_message_single);

        btn_single.setOnClickListener(new MyOnClickListener(tv_message_single));

        Button btn_all = findViewById(R.id.btn_all);

        tv_message_all = findViewById(R.id.tv_message_all);

        btn_all.setOnClickListener(this);

        Button btn_long = findViewById(R.id.btn_long);

        TextView tv_message_long = findViewById(R.id.tv_message_long);

        btn_long.setOnLongClickListener(v -> {
            String format = String.format("%s,您点击了：%s", DateUtils.getNowTime(), ((Button) v).getText());
            tv_message_long.setText(format);
            return true;
        });


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_all) {
            String format = String.format("%s,您点击了：%s", DateUtils.getNowTime(), ((Button) v).getText());
            tv_message_all.setText(format);
        }

    }

    static class MyOnClickListener implements View.OnClickListener {

        private final TextView tvMessageSingle;

        public MyOnClickListener(TextView tvMessageSingle) {
            this.tvMessageSingle = tvMessageSingle;
        }

        @Override
        public void onClick(View v) {
            String format = String.format("%s,您点击了：%s", DateUtils.getNowTime(), ((Button) v).getText());
            tvMessageSingle.setText(format);
        }
    }
}