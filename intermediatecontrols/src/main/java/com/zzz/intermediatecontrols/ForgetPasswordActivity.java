package com.zzz.intermediatecontrols;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.intermediatecontrols.utils.ViewUtil;

import java.util.Random;

public class ForgetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_get_verification_code;
    private EditText et_verification_code;
    private EditText et_new_password;
    private EditText et_confirm_password;
    private Button btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 新密码
        et_new_password = findViewById(R.id.et_new_password);
        et_new_password.addTextChangedListener(new HideTextWatcher(et_new_password, 6));

        // 确认新密码
        et_confirm_password = findViewById(R.id.et_confirm_password);
        et_confirm_password.addTextChangedListener(new HideTextWatcher(et_confirm_password, 6));

        // 验证码
        et_verification_code = findViewById(R.id.et_verification_code);
        et_verification_code.addTextChangedListener(new HideTextWatcher(et_verification_code, 6));

        // 获取验证码按钮
        btn_get_verification_code = findViewById(R.id.btn_get_verification_code);
        btn_get_verification_code.setOnClickListener(this);

        // 确认按钮
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_get_verification_code) {
            String phone = getIntent().getStringExtra("phone");

            if (phone == null || phone.trim().isEmpty()) {
                Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            // 生成六位数验证码
            String code = generateCode();

            new AlertDialog.Builder(this)
                    .setTitle("获取验证码")
                    .setMessage(
                            "用户手机号：" + phone
                                    + "\n当前验证码为：" + code
                    )
                    .setPositiveButton("确认", (dialog, which) -> {
                        // 点击确认，将验证码填入输入框
                        et_verification_code.setText(code);

                        // 将光标移动到输入框末尾
                        et_verification_code.setSelection(
                                et_verification_code.getText().length()
                        );
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else if (v.getId() == R.id.btn_confirm) {
            String newPassword = et_new_password.getText().toString().trim();
            String confirmPassword = et_confirm_password.getText().toString().trim();
            String inputCode = et_verification_code.getText().toString().trim();

            // 检查新密码
            if (newPassword.isEmpty()) {
                et_new_password.setError("请输入新密码");
                et_new_password.requestFocus();
                return;
            }

            // 检查两次密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                et_confirm_password.setError("两次输入的密码不一致");
                et_confirm_password.requestFocus();
                return;
            }

            // 检查验证码是否为空
            if (inputCode.isEmpty()) {
                et_verification_code.setError("请输入验证码");
                et_verification_code.requestFocus();
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("new_password", newPassword);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    private class HideTextWatcher implements TextWatcher {

        private EditText mView;

        private int mMaxLength;

        public HideTextWatcher(EditText mView, int mMaxLength) {
            this.mMaxLength = mMaxLength;
            this.mView = mView;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == mMaxLength) {
                ViewUtil.hideOneInputMethod(ForgetPasswordActivity.this, mView);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }

    // 生成随机六位数验证码
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

}