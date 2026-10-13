package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditTextFocusActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    private EditText edit_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_text_focus);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edit_phone = findViewById(R.id.edit_phone);
        edit_phone.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String phone = edit_phone.getText().toString().trim();

            if (phone.matches("^1[3-9]\\d{9}$")) {
                // 手机号格式正确
                edit_phone.setError(null);
            } else {
                // 手机号格式错误
                edit_phone.setError("请输入正确的中国大陆手机号");

                Toast.makeText(
                        this,
                        "请输入正确的中国大陆手机号",
                        Toast.LENGTH_SHORT
                ).show();
                // 请求手机号输入框重新获得焦点
                // edit_phone.requestFocus();
            }
        }
    }
}