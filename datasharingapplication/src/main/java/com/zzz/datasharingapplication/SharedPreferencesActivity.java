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

/**
 * SharedPreferences 演示：键值对式轻量存储，适合"配置项、开关、记住密码"这类少量数据
 *
 * <p>演示要点：
 * 1. {@code getSharedPreferences("config", MODE_PRIVATE)} 会生成
 *    {@code /data/data/<包名>/shared_prefs/config.xml}，内容是可读的 XML（root 设备能直接看）；
 * 2. 读写都是<b>同步 API</b>，但写操作要经过 {@code Editor}：
 *    {@code edit() → putXxx() → apply()/commit()}，不调用最后一步等于没保存；
 * 3. {@code apply()} 异步落盘、不返回结果；{@code commit()} 同步落盘、返回 boolean。
 *    本页用 apply()（界面型数据，丢一两次无所谓），而"是否首次初始化"这种标记要用 commit()；
 * 4. 数据类型必须严格对应：用 putString 存的就必须用 getString 读，
 *    类型不匹配会抛 {@link ClassCastException}——所以 getXxx 的第二个参数（默认值）很重要。
 *
 * <p>对应布局：{@code activity_shared_preferences.xml}
 * <p>涉及 API：{@link SharedPreferences}、{@link SharedPreferences.Editor}、
 * {@link CheckBox#isChecked()}、{@link View.OnClickListener}
 */
public class SharedPreferencesActivity extends AppCompatActivity implements View.OnClickListener {

    // ===== 输入控件 =====
    private EditText et_name;     // 姓名
    private EditText et_age;      // 年龄
    private EditText et_height;   // 身高
    private EditText et_weight;   // 体重
    private CheckBox cb_married;  // 婚否

    // ===== 按钮 =====
    private Button btn_confirm;   // 确认保存

    // SharedPreferences 对象：一个文件名对应一个实例，全应用可共享
    private SharedPreferences sharedPreferences;

    /**
     * 页面创建：绑定控件 → 取 SharedPreferences → 把已存数据回填到控件
     *
     * @param savedInstanceState 系统保存的临时状态（数据已存在 prefs 里，无需它）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_preferences);

        // 与其他页面一致的 insets 处理（本页根布局 id 是 ll_main，注意别抄错）
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ll_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 绑定输入控件
        et_name = findViewById(R.id.et_name);
        et_age = findViewById(R.id.et_age);
        et_height = findViewById(R.id.et_height);
        et_weight = findViewById(R.id.et_weight);

        // 2. 绑定复选框与按钮，并让本 Activity 自己实现监听（implements View.OnClickListener）
        cb_married = findViewById(R.id.cb_married);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(this);   // this = 本类，因为它实现了 OnClickListener

        // 3. 取 SharedPreferences：文件名 "config" + MODE_PRIVATE（仅本应用可读写）
        sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);

        // 4. 进页面先把已保存的数据回填，让用户看到上次的值
        reload();
    }

    /**
     * 从 SharedPreferences 读取数据并回填到控件
     *
     * <p>key 不存在时返回 getXxx 的第二个参数（默认值），所以首次进入页面不会崩，只是控件为空。
     */
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

    /**
     * 统一点击回调（因为本类 implements View.OnClickListener）
     *
     * @param v 被点击的控件
     */
    @Override
    public void onClick(View v) {
        // 一个监听器可能挂多个按钮，必须按 id 区分，不能直接当成 btn_confirm 处理
        if (v.getId() == R.id.btn_confirm) {

            // 1. 获取输入框中的值（trim 去掉首尾空格）
            String name = et_name.getText().toString().trim();
            String age = et_age.getText().toString().trim();
            String height = et_height.getText().toString().trim();
            String weight = et_weight.getText().toString().trim();

            // 2. 获取 CheckBox 的选中状态（boolean，不是字符串）
            boolean married = cb_married.isChecked();

            // 3. 获取 SharedPreferences 编辑器（所有写入都必须经过它）
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // 4. 存储数据：key 与读取时（reload 里）必须一一对应，写错一个字母就读不到
            editor.putString("name", name);
            editor.putString("age", age);
            editor.putString("height", height);
            editor.putString("weight", weight);
            editor.putBoolean("married", married);

            // 5. 提交保存：apply 异步写磁盘，不阻塞 UI 线程
            editor.apply();

            // 6. 提示保存成功
            Toast.makeText(this, "数据保存成功", Toast.LENGTH_SHORT).show();
        }
    }
}
