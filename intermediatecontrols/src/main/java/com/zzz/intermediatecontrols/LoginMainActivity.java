package com.zzz.intermediatecontrols;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.intermediatecontrols.utils.ViewUtil;

import java.util.Random;

public class LoginMainActivity extends AppCompatActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private CheckBox cb_remember_me;
    private EditText et_phone;
    private EditText et_password;
    private TextView tv_password_label;
    private Button btn_forgot_password;
    private ActivityResultLauncher<Intent> register;

    // 模拟初始密码
    private String password = "123456";
    private RadioGroup rg_login_type;
    private Button btn_login;
    private String code;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rg_login_type = findViewById(R.id.rg_login_type);

        RadioButton rb_password_login = findViewById(R.id.rb_password_login);
        RadioButton rb_code_login = findViewById(R.id.rb_code_login);

        tv_password_label = findViewById(R.id.tv_password_label);
        TextView tv_phone_label = findViewById(R.id.tv_phone_label);

        et_phone = findViewById(R.id.et_phone);
        et_password = findViewById(R.id.et_password);

        btn_forgot_password = findViewById(R.id.btn_forgot_password);
        btn_login = findViewById(R.id.btn_login);

        cb_remember_me = findViewById(R.id.cb_remember_me);

        rg_login_type.setOnCheckedChangeListener(this);
        btn_forgot_password.setOnClickListener(this);
        btn_login.setOnClickListener(this);

        et_phone.addTextChangedListener(new HideTextWatcher(et_phone, 11));
        et_password.addTextChangedListener(new HideTextWatcher(et_password, 6));

        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            if (o != null) {
                Intent oData = o.getData();
                if (oData != null && o.getResultCode() == Activity.RESULT_OK) {
                    // 获取另一个 Activity 返回的新密码
                    String newPassword = oData.getStringExtra("new_password");

                    // 更新当前 Activity 的密码变量
                    if (newPassword != null && !newPassword.isEmpty()) {
                        password = newPassword;
                    }

                }
            }
        });

        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_forgot_password) {
            if (rg_login_type.getCheckedRadioButtonId()
                    == R.id.rb_password_login) {

                String phone = et_phone.getText().toString().trim();

                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    et_phone.setError("请输入正确的手机号");
                    return;
                }
                //忘记密码携带手机号进行跳转。
                Intent intent = new Intent(
                        this, ForgetPasswordActivity.class);

                intent.putExtra("phone", phone);
                register.launch(intent);

            } else if (rg_login_type.getCheckedRadioButtonId()
                    == R.id.rb_code_login) {
                // TODO 验证码获取逻辑后续实现
                String phone = et_phone.getText().toString().trim();

                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    et_phone.setError("请输入正确的手机号");
                    return;
                }

                // 生成六位数验证码
                code = generateCode();

                new AlertDialog.Builder(this)
                        .setTitle("获取验证码")
                        .setMessage(
                                "用户手机号：" + phone
                                        + "\n当前验证码为：" + code
                        )
                        .setPositiveButton("确认", (dialog, which) -> {
                            // 点击确认，将验证码填入输入框
                            et_password.setText(code);

                            // 将光标移动到输入框末尾
                            et_password.setSelection(
                                    et_password.getText().length()
                            );
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }

        } else if (v.getId() == R.id.btn_login) {
            if (rg_login_type.getCheckedRadioButtonId()
                    == R.id.rb_password_login) {
                String phone = et_phone.getText().toString().trim();

                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    et_phone.setError("请输入正确的手机号");
                    return;
                }

                // 获取用户输入的密码
                String inputPassword = et_password.getText().toString().trim();

                // 判断是否为空
                if (inputPassword.isEmpty()) {
                    et_password.setError("请输入密码");
                    return;
                }

                // 校验密码
                if (inputPassword.equals(password)) {
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                    boolean checked = cb_remember_me.isChecked();
                    if (checked) {
                        // CheckBox 已选中
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("phone",phone);
                        editor.putString("password",inputPassword);
                        editor.putBoolean("remember_me", true);

                        editor.apply();

                        Toast.makeText(this, "数据保存成功", Toast.LENGTH_SHORT).show();

                    }
                    // TODO: 登录成功后跳转到主页

                } else {
                    et_password.setError("密码错误，请重新输入");
                }

            } else if (rg_login_type.getCheckedRadioButtonId()
                    == R.id.rb_code_login) {

                // TODO 验证码登录

                String phone = et_phone.getText().toString().trim();

                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    et_phone.setError("请输入正确的手机号");
                    return;
                }

                //获取验证码
                String inputCode = et_password.getText().toString().trim();

                // 校验验证码
                if (inputCode.equals(code)) {
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

                    // TODO: 登录成功后跳转到主页

                } else {
                    et_password.setError("验证码错误，请重新输入");
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_password_login) {
            cb_remember_me.setVisibility(View.VISIBLE);
            tv_password_label.setText("登录密码:");
            et_password.setHint("请输入密码");
            btn_forgot_password.setText("忘记密码");
        } else if (checkedId == R.id.rb_code_login) {
            cb_remember_me.setVisibility(View.GONE);
            tv_password_label.setText("验证码:");
            et_password.setHint("请输入验证码");
            btn_forgot_password.setText("获取验证码");
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
                ViewUtil.hideOneInputMethod(LoginMainActivity.this, mView);
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