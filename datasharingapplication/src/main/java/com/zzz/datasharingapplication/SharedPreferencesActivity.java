package com.zzz.datasharingapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SharedPreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText et_name;
    private EditText et_age;
    private EditText et_height;
    private EditText et_weight;
    private CheckBox cb_married;
    private Button btn_confirm;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_preferences);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ll_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        et_name = findViewById(R.id.et_name);
        et_age = findViewById(R.id.et_age);
        et_height = findViewById(R.id.et_height);
        et_weight = findViewById(R.id.et_weight);

        cb_married = findViewById(R.id.cb_married);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(this);

        // 私有
        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        reload();
    }

    private void reload() {
        // 1. 读取保存的数据，没有数据时使用默认值
        String name = sharedPreferences.getString("name", "");
        String age = sharedPreferences.getString("age", "");
        String height = sharedPreferences.getString("height", "");
        String weight = sharedPreferences.getString("weight", "");
        boolean married = sharedPreferences.getBoolean("married", false);

        // 2. 将数据重新设置到控件中
        et_name.setText(name);
        et_age.setText(age);
        et_height.setText(height);
        et_weight.setText(weight);
        cb_married.setChecked(married);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {

            // 1. 获取输入框中的值
            String name = et_name.getText().toString().trim();
            String age = et_age.getText().toString().trim();
            String height = et_height.getText().toString().trim();
            String weight = et_weight.getText().toString().trim();

            // 2. 获取 CheckBox 的选中状态
            boolean married = cb_married.isChecked();

            // 3. 获取 SharedPreferences 编辑器
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // 4. 存储数据
            editor.putString("name", name);
            editor.putString("age", age);
            editor.putString("height", height);
            editor.putString("weight", weight);
            editor.putBoolean("married", married);

            // 5. 提交保存
            editor.apply();

            // 6. 提示保存成功
            Toast.makeText(this, "数据保存成功", Toast.LENGTH_SHORT).show();
        }
    }
}