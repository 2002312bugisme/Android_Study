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

/**
 * SQLiteOpenHelper 演示：增 / 删 / 改 / 查 / 事务批量插入五个按钮
 *
 * <p>演示要点：
 * 1. 数据库连接在 {@link #onStart()} 打开、{@link #onStop()} 关闭——
 *    放在这对回调里而不是 onCreate/onDestroy，是为了"页面切后台就释放数据库资源"，
 *    同时又能覆盖"从后台回到前台"的情况（onCreate 只执行一次，回来时不会重开）；
 * 2. 每个按钮都做了<b>两级校验</b>：先判空（{@code isEmpty}），再把字符串转数字
 *    （{@link NumberFormatException}），任一失败都直接 return，不带着脏数据去写库；
 * 3. 界面反馈分两路：{@link ToastUtil} 给即时提示，{@code tvResult} 显示详细结果
 *    （新增会回显自增 ID，删除/修改会告知影响行数）；
 * 4. 批量插入走 {@code UserDBHelper#insertBatchWithTransaction}，演示"要么全成功、要么全回滚"。
 *
 * <p>对应布局：{@code activity_sqlite_helper.xml}
 * <p>涉及 API：{@link UserDBHelper}、{@link User}、{@link NumberFormatException}、
 * {@link View.OnClickListener}
 */
public class SqliteHelperActivity extends AppCompatActivity implements View.OnClickListener {

    // ===== 输入控件 =====
    private EditText etName;      // 姓名
    private EditText etAge;       // 年龄
    private EditText etHeight;    // 身高
    private EditText etWeight;    // 体重
    private CheckBox cbMarried;   // 婚否

    // ===== 按钮 =====
    private Button btnAdd;        // 增
    private Button btnDelete;     // 删
    private Button btnUpdate;     // 改
    private Button btnQuery;      // 查
    private Button btnBatch;      // 事务批量插入

    // ===== 结果展示 =====
    private TextView tvResult;        // 详细结果
    private UserDBHelper mHelper;     // 数据库帮助器（单例）

    // 删除 / 修改 / 按 id 查询都要用到的小输入框
    private EditText etId;

    /**
     * 页面可见时打开数据库连接
     *
     * <p>{@code getInstance} 只是拿单例（此时还没真正建库），
     * 真正的数据库文件是在 {@code openWriteLink()} / {@code openReadLink()} 里创建的。
     */
    @Override
    protected void onStart() {
        super.onStart();
        //获取数据库帮助器的读写连接
        mHelper = UserDBHelper.getInstance(this);
        //打开数据库帮助器的读写连接
        mHelper.openWriteLink();
        mHelper.openReadLink();
    }

    /**
     * 页面不可见时关闭数据库连接，释放句柄
     */
    @Override
    protected void onStop() {
        super.onStop();
        //关闭数据库连接
        mHelper.closeLink();
    }


    /**
     * 页面创建：绑定控件 + 统一挂点击事件
     *
     * @param savedInstanceState 系统保存的临时状态（本页数据都在库里，无需恢复）
     */
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

    /**
     * 统一的点击回调：按控件 id 分发到五个具体方法
     *
     * <p>"一个监听器 + if/else 分发"是《第一行代码》那类教程的经典写法；
     * 页面按钮多时也可以像商城页那样一个按钮一个 lambda，各自更清晰。
     *
     * @param v 被点击的按钮
     */
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

    /**
     * 新增一条用户记录
     *
     * <p>流程：读输入 → 判空 → 转数字 → 组装 User → 调 insert → 按返回值提示
     */
    private void onAddClick() {
        // 1. 从输入框读取数据（trim 去掉首尾空格）
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

        // 3. 数值格式校验并转换：只要有一个不是数字就整体失败
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

        // 5. 调用数据库插入：insert() 返回新插入行的 id，失败返回 -1
        long newId = mHelper.insert(user);

        // 6. 根据结果更新 UI
        if (newId > 0) {
            ToastUtil.show(this, "添加成功");
            // 把插入后的完整信息回显出来，方便确认"到底写进去了什么"
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

    /**
     * 按 id 删除一条记录
     *
     * <p>delete() 返回受影响行数：0 表示没有这个 id（不算异常，只是没删到东西）。
     */
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

        // 3. 按影响行数更新 tvResult
        if (rows > 0) {
            ToastUtil.show(this, "删除成功");
            tvResult.setText("已删除 ID = " + id);
        } else {
            ToastUtil.show(this, "删除失败，未找到该 ID");
            tvResult.setText("删除失败：不存在 ID = " + id + " 的记录");
        }
    }

    // ================= 改 =================

    /**
     * 按 id 修改一条记录（全字段覆盖）
     *
     * <p>数据库 update 是按主键定位行的，所以 id 必须填、且必须已存在。
     */
    private void onUpdateClick() {
        // 1. 读取输入（含 id）
        String idStr = etId.getText().toString().trim();
        String name = getText(etName);
        String ageStr = getText(etAge);
        String heightStr = getText(etHeight);
        String weightStr = getText(etWeight);
        boolean married = cbMarried.isChecked();

        // 2. 判空
        if (idStr.isEmpty()) {
            ToastUtil.show(this, "请输入要修改的 ID");
            return;
        }
        if (name.isEmpty() || ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            ToastUtil.show(this, "请填写完整的姓名、年龄、身高、体重");
            return;
        }

        // 3. 4 个字段一起转数字，任一失败就整体放弃
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

        // 4. 封装成 User 对象（这里直接给 public 字段赋值，也可以换成 setter）
        User user = new User();
        user.id = id;
        user.name = name;
        user.age = age;
        user.height = height;
        user.weight = weight;
        user.married = married;

        // 5. 调用更新接口
        int rows = mHelper.update(user);

        // 6. 按影响行数更新 tvResult
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

    /**
     * 查询：输入框里有 id 就按 id 精确查，没有 id 就查全部
     *
     * <p>一个按钮两种语义，靠"et_id 是否为空"来分流。
     */
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

            // 2. 调用查询接口：查不到返回 null（这是唯一"用返回值判断是否存在"的地方）
            User user = mHelper.queryById(id);

            // 3. 格式化输出
            if (user != null) {
                tvResult.setText(formatUser(user));
            } else {
                ToastUtil.show(this, "未找到该用户");
                tvResult.setText("查询失败：不存在 ID = " + id + " 的记录");
            }
            return;   // 精确查询结束，不再走下面的"查全部"
        }

        // 情况二：ID 为空 —— 查询全部
        java.util.List<User> list = mHelper.queryAll();
        if (list == null || list.isEmpty()) {
            ToastUtil.show(this, "暂无数据");
            tvResult.setText("暂无用户数据");
            return;
        }

        // 逐条格式化后拼接，最后 trim() 去掉末尾多余的换行
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(list.size()).append(" 条记录：\n\n");
        for (User u : list) {
            sb.append(formatUser(u)).append("\n\n");
        }
        tvResult.setText(sb.toString().trim());
    }

    /**
     * 把 User 对象格式化成可读字符串
     *
     * @param u 待格式化的用户
     * @return 多行文本（每行一个字段）
     */
    private String formatUser(User u) {
        return "ID：" + u.id
                + "\n姓名：" + u.name
                + "\n年龄：" + u.age
                + "\n身高：" + u.height
                + "\n体重：" + u.weight
                + "\n已婚：" + (u.married ? "是" : "否");
    }

    /**
     * 读取输入框文本并去首尾空格的小工具
     *
     * @param et 任意输入框
     * @return 去掉首尾空格后的文本
     */
    private String getText(EditText et) {
        return et.getText().toString().trim();
    }


    /**
     * 事务批量插入：两条数据"要么都进库，要么都不进"
     *
     * <p>这里的数据是代码里写死的，方便把注意力放在事务上；
     * 真实实现见 {@code UserDBHelper#insertBatchWithTransaction}：
     * 它在第二条插入时故意抛异常，用来演示"回滚后数据库没有变化"。
     */
    private void onBatchInsertClick() {
        java.util.List<User> userList = new java.util.ArrayList<>();

        // 第一条：张三
        User u1 = new User();
        u1.name = "张三";
        u1.age = 25;
        u1.height = 175;
        u1.weight = 65.5f;
        u1.married = true;
        userList.add(u1);

        // 第二条：李四
        User u2 = new User();
        u2.name = "李四";
        u2.age = 30;
        u2.height = 180;
        u2.weight = 72.0f;
        u2.married = false;
        userList.add(u2);

        // 整个列表在一个事务里插入，返回 true 才表示全部成功
        boolean ok = mHelper.insertBatchWithTransaction(userList);
        if (ok) {
            ToastUtil.show(this, "批量插入成功");
            tvResult.setText("事务提交成功，共插入 " + userList.size() + " 条");
        } else {
            // 教学代码：UserDBHelper 里故意抛异常，所以这里当前必然走到失败分支
            ToastUtil.show(this, "批量插入失败，已回滚");
            tvResult.setText("事务回滚，数据库未发生变化");
        }
    }


}
