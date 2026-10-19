package com.zzz.datasharingapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.datasharingapplication.database.UserDBHelper;
import com.zzz.datasharingapplication.pojo.User;
import com.zzz.datasharingapplication.util.ToastUtil;

public class SqliteHelperActivity extends AppCompatActivity implements View.OnClickListener {

    // ===== 输入控件 =====
    private EditText etName;
    private EditText etAge;
    private EditText etHeight;
    private EditText etWeight;
    private CheckBox cbMarried;

    // ===== 按钮 =====
    private Button btnAdd;
    private Button btnDelete;
    private Button btnUpdate;
    private Button btnQuery;
    private Button btnBatch;

    // ===== 结果展示 =====
    private TextView tvResult;
    private UserDBHelper mHelper;

    private EditText etId;


    @Override
    protected void onStart() {
        super.onStart();
        //获取数据库帮助器的读写连接
        mHelper = UserDBHelper.getInstance(this);
        //打开数据库帮助器的读写连接
        mHelper.openWriteLink();
        mHelper.openReadLink();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭数据库连接
        mHelper.closeLink();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sqlite_helper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ===== 绑定控件 id =====
        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        cbMarried = findViewById(R.id.cb_married);

        btnAdd = findViewById(R.id.btn_add);
        btnDelete = findViewById(R.id.btn_delete);
        btnUpdate = findViewById(R.id.btn_update);
        btnQuery = findViewById(R.id.btn_query);
        btnBatch = findViewById(R.id.btn_batch);

        tvResult = findViewById(R.id.tv_result);

        etId = findViewById(R.id.et_id);


        // ===== 统一绑定点击事件 =====
        btnAdd.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnQuery.setOnClickListener(this);
        btnBatch.setOnClickListener(this);
    }

    // ===== 统一的点击回调 =====
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add) {
            onAddClick();
        } else if (id == R.id.btn_delete) {
            onDeleteClick();
        } else if (id == R.id.btn_update) {
            onUpdateClick();
        } else if (id == R.id.btn_query) {
            onQueryClick();
        } else if (id == R.id.btn_batch) {
            onBatchInsertClick();
        }
    }

    // ================= 增 =================
    private void onAddClick() {
        // 1. 从输入框读取数据
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        boolean married = cbMarried.isChecked();

        // 2. 非空校验
        if (name.isEmpty()) {
            ToastUtil.show(this, "请输入姓名");
            return;
        }
        if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            ToastUtil.show(this, "请填写完整的年龄、身高、体重");
            return;
        }

        // 3. 数值格式校验并转换
        int age;
        long height;
        float weight;
        try {
            age = Integer.parseInt(ageStr);
            height = Long.parseLong(heightStr);
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            ToastUtil.show(this, "年龄/身高/体重格式不正确");
            return;
        }

        // 4. 封装成 User 对象（id 由数据库自增，无需赋值）
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setHeight(height);
        user.setWeight(weight);
        user.setMarried(married);

        // 5. 调用数据库插入
        long newId = mHelper.insert(user);   // 假设 insert 返回新插入行的 id，失败返回 -1

        // 6. 根据结果更新 UI
        if (newId > 0) {
            ToastUtil.show(this, "添加成功");
            tvResult.setText("新增用户成功，ID = " + newId
                    + "\n姓名：" + name
                    + "\n年龄：" + age
                    + "\n身高：" + height
                    + "\n体重：" + weight
                    + "\n已婚：" + (married ? "是" : "否"));

            // 可选：添加成功后清空输入框
            // clearInputs();
        } else {
            ToastUtil.show(this, "添加失败");
            tvResult.setText("新增用户失败");
        }
    }

    // ================= 删 =================
    private void onDeleteClick() {
        // 1. 获取要删除的 id
        String idStr = etId.getText().toString().trim();
        if (idStr.isEmpty()) {
            ToastUtil.show(this, "请输入要删除的 ID");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            ToastUtil.show(this, "ID 格式不正确");
            return;
        }

        // 2. 调用删除接口
        int rows = mHelper.delete(id);

        // 3. 更新 tvResult
        if (rows > 0) {
            ToastUtil.show(this, "删除成功");
            tvResult.setText("已删除 ID = " + id);
        } else {
            ToastUtil.show(this, "删除失败，未找到该 ID");
            tvResult.setText("删除失败：不存在 ID = " + id + " 的记录");
        }
    }

    // ================= 改 =================
    private void onUpdateClick() {
        // 1. 读取输入（含 id）
        String idStr = etId.getText().toString().trim();
        String name = getText(etName);
        String ageStr = getText(etAge);
        String heightStr = getText(etHeight);
        String weightStr = getText(etWeight);
        boolean married = cbMarried.isChecked();

        if (idStr.isEmpty()) {
            ToastUtil.show(this, "请输入要修改的 ID");
            return;
        }
        if (name.isEmpty() || ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            ToastUtil.show(this, "请填写完整的姓名、年龄、身高、体重");
            return;
        }

        int id, age;
        long height;
        float weight;
        try {
            id = Integer.parseInt(idStr);
            age = Integer.parseInt(ageStr);
            height = Long.parseLong(heightStr);
            weight = Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            ToastUtil.show(this, "ID/年龄/身高/体重格式不正确");
            return;
        }

        // 2. 封装成 User 对象
        User user = new User();
        user.id = id;            // 若你的 User 用了 setter，则写 user.setId(id)
        user.name = name;
        user.age = age;
        user.height = height;
        user.weight = weight;
        user.married = married;

        // 3. 调用更新接口
        int rows = mHelper.update(user);

        // 4. 更新 tvResult
        if (rows > 0) {
            ToastUtil.show(this, "修改成功");
            tvResult.setText("更新用户成功\nID：" + id
                    + "\n姓名：" + name
                    + "\n年龄：" + age
                    + "\n身高：" + height
                    + "\n体重：" + weight
                    + "\n已婚：" + (married ? "是" : "否"));
        } else {
            ToastUtil.show(this, "修改失败，未找到该 ID");
            tvResult.setText("更新失败：不存在 ID = " + id + " 的记录");
        }
    }

    // ================= 查 =================
    private void onQueryClick() {
        String idStr = etId.getText().toString().trim();

        // 情况一：输入了 ID —— 按 ID 精确查询
        if (!idStr.isEmpty()) {
            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                ToastUtil.show(this, "ID 格式不正确");
                return;
            }

            // 2. 调用查询接口
            User user = mHelper.queryById(id);

            // 3. 格式化输出
            if (user != null) {
                tvResult.setText(formatUser(user));
            } else {
                ToastUtil.show(this, "未找到该用户");
                tvResult.setText("查询失败：不存在 ID = " + id + " 的记录");
            }
            return;
        }

        // 情况二：ID 为空 —— 查询全部
        java.util.List<User> list = mHelper.queryAll();
        if (list == null || list.isEmpty()) {
            ToastUtil.show(this, "暂无数据");
            tvResult.setText("暂无用户数据");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(list.size()).append(" 条记录：\n\n");
        for (User u : list) {
            sb.append(formatUser(u)).append("\n\n");
        }
        tvResult.setText(sb.toString().trim());
    }

    // 把 User 对象格式化成可读字符串
    private String formatUser(User u) {
        return "ID：" + u.id
                + "\n姓名：" + u.name
                + "\n年龄：" + u.age
                + "\n身高：" + u.height
                + "\n体重：" + u.weight
                + "\n已婚：" + (u.married ? "是" : "否");
    }

    // ================= 可选：读取输入框的工具方法 =================
    private String getText(EditText et) {
        return et.getText().toString().trim();
    }


    // 单独的事务批量插入方法
    private void onBatchInsertClick() {
        java.util.List<User> userList = new java.util.ArrayList<>();

        User u1 = new User();
        u1.name = "张三";
        u1.age = 25;
        u1.height = 175;
        u1.weight = 65.5f;
        u1.married = true;
        userList.add(u1);

        User u2 = new User();
        u2.name = "李四";
        u2.age = 30;
        u2.height = 180;
        u2.weight = 72.0f;
        u2.married = false;
        userList.add(u2);

        boolean ok = mHelper.insertBatchWithTransaction(userList);
        if (ok) {
            ToastUtil.show(this, "批量插入成功");
            tvResult.setText("事务提交成功，共插入 " + userList.size() + " 条");
        } else {
            ToastUtil.show(this, "批量插入失败，已回滚");
            tvResult.setText("事务回滚，数据库未发生变化");
        }
    }


}