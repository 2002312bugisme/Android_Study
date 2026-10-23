package com.zzz.client_application;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.client_application.content.UserInfoContent;
import com.zzz.client_application.util.ToastUtil;

/**
 * 客户端录入页：通过 {@link ContentResolver} 跨应用读写服务端（server_application）的数据库
 *
 * <p>演示要点（跨应用通信的完整链路）：
 * <pre>
 *   本页 ContentResolver.insert/query
 *        ↓  按 Uri 里的 authority 找到 provider
 *   server 应用的 UserInfoContentProvider（运行在 server 进程里）
 *        ↓  内部直接用 SQLiteDatabase
 *   server 应用私有目录下的 user.db
 * </pre>
 * 1. 客户端<b>没有</b>导入服务端的任何类，也不直接开数据库；
 *    两边只靠"authority 字符串 + 列名"这个约定通信，这就是 ContentProvider 的解耦价值；
 * 2. {@link ContentValues} 的 key 必须是服务端表的列名，写错不会报错，只是那一列存进去是 NULL；
 * 3. {@code insert()} 返回新行的 Uri，失败返回 null；{@code query()} 返回的 Cursor 必须 close()；
 * 4. 跨应用调用要处理两种典型异常：
 *    - {@link IllegalArgumentException}：authority 写错，或服务端没安装（Unknown URL）；
 *    - {@link SecurityException}：服务端 provider 未 exported，或对方加了权限限制。
 *
 * <p>对应布局：{@code activity_content_write.xml}
 * <p>涉及 API：{@link ContentResolver#insert}、{@link ContentResolver#query}、
 * {@link ContentValues}、{@link Cursor}、{@link Uri}
 */
public class ContentWriteActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ContentWrite";

    // ===== 控件 =====
    private EditText etName;      // 姓名
    private EditText etAge;       // 年龄
    private EditText etHeight;    // 身高
    private EditText etWeight;    // 体重
    private CheckBox cbMarried;   // 婚否
    private EditText etId;        // 要更新 / 删除的行 ID
    private TextView tvResult;    // 查询结果

    /**
     * 页面创建：绑定控件 → 给两个按钮挂同一个监听器（本类实现了 OnClickListener）
     *
     * @param savedInstanceState 系统保存的临时状态（数据都在服务端库里，无需恢复）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_content_write);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. 绑定控件（id 与 activity_content_write.xml 一一对应）
        etName    = findViewById(R.id.et_name);
        etAge     = findViewById(R.id.et_age);
        etHeight  = findViewById(R.id.et_height);
        etWeight  = findViewById(R.id.et_weight);
        cbMarried = findViewById(R.id.cb_married);
        etId      = findViewById(R.id.et_id);
        tvResult  = findViewById(R.id.tv_result);

        // 2. 四个按钮都交给本类处理，onClick 里按 id 分流
        Button btnSave   = findViewById(R.id.btn_save);
        Button btnQuery  = findViewById(R.id.btn_query);
        Button btnUpdate = findViewById(R.id.btn_update);
        Button btnDelete = findViewById(R.id.btn_delete);
        btnSave.setOnClickListener(this);
        btnQuery.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    /**
     * 统一点击回调：保存 → 写服务端；查询 → 读服务端；更新 / 删除 → 改服务端某一行
     *
     * @param v 被点击的按钮
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            saveToServer();
        } else if (v.getId() == R.id.btn_query) {
            queryFromServer();
        } else if (v.getId() == R.id.btn_update) {
            updateRow();
        } else if (v.getId() == R.id.btn_delete) {
            confirmDeleteRow();
        }
    }

    // ==================== 写：insert ====================

    /**
     * 把表单内容插进服务端的 user_info 表
     *
     * <p>流程：读输入 → 校验 → 装 ContentValues → resolver.insert → 按返回值提示。
     */
    private void saveToServer() {
        // 1. 读输入（trim 去首尾空格）
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        boolean married = cbMarried.isChecked();

        // 2. 校验：姓名必填（服务端表里 name 允许 NULL，但业务上不该空）
        if (name.isEmpty()) {
            ToastUtil.show(this, "请输入姓名");
            return;
        }

        // 3. 数字转换：留空按 0 处理，填了就必须是合法数字
        int age;
        int height;
        float weight;
        try {
            age = ageStr.isEmpty() ? 0 : Integer.parseInt(ageStr);
            height = heightStr.isEmpty() ? 0 : Integer.parseInt(heightStr);
            weight = weightStr.isEmpty() ? 0f : Float.parseFloat(weightStr);
        } catch (NumberFormatException e) {
            ToastUtil.show(this, "年龄 / 身高 / 体重格式不正确");
            return;
        }

        // 4. 组装 ContentValues：key 必须与服务端表列名一致（这里用契约类常量，避免手写字符串）
        ContentValues values = new ContentValues();
        values.put(UserInfoContent.USER_NAME, name);            // name    TEXT
        values.put(UserInfoContent.USER_AGE, age);              // age     INTEGER
        values.put(UserInfoContent.USER_HEIGHT, height);        // height  INTEGER
        values.put(UserInfoContent.USER_WEIGHT, weight);        // weight  REAL
        values.put(UserInfoContent.USER_MARRIED, married ? 1 : 0);  // married INTEGER，boolean 要手动转 0/1

        // 5. 跨进程写入：交给 ContentResolver，由它按 Uri 找到 server 的 provider
        try {
            Uri newUri = getContentResolver().insert(UserInfoContent.CONTENT_URI, values);
            if (newUri == null) {
                // provider 里 insert 失败（例如 rowId == -1）时通常返回 null
                ToastUtil.show(this, "保存失败：服务端拒绝了这条数据");
                return;
            }
            Log.d(TAG, "insert 成功，新行 Uri = " + newUri);
            ToastUtil.show(this, "保存成功：" + newUri);

            // 6. 保存成功顺手清空表单并刷新列表，方便连续录入
            clearInputs();
            queryFromServer();
        } catch (IllegalArgumentException e) {
            // authority 写错 / 服务端没装，都会走到这里：Unknown URL content://...
            Log.e(TAG, "insert 失败，Uri = " + UserInfoContent.CONTENT_URI, e);
            ToastUtil.show(this, "保存失败：找不到服务端 provider（检查 authority 或先安装 server 应用）");
        } catch (SecurityException e) {
            // 对方 provider 未 exported，或被权限拦住了
            Log.e(TAG, "insert 被拒绝：权限不足", e);
            ToastUtil.show(this, "保存失败：没有访问服务端的权限");
        }
    }

    // ==================== 读：query ====================

    /**
     * 从服务端 user_info 表查出全部记录，拼成多行文本显示
     *
     * <p>Cursor 的用法要点：
     * 1. 必须用 {@code moveToNext()} 遍历，读完 {@code close()}；
     * 2. 取列用 {@code getColumnIndexOrThrow(列名)}：列名错会立刻抛异常，
     *    比 {@code getColumnIndex()}（错时返回 -1）容易排查；
     * 3. Cursor 是从另一个进程的 provider 拿到的，读之前数据库文件可能已经变化。
     */
    private void queryFromServer() {
        Cursor cursor = null;
        try {
            // 注意参数个数：query(uri, projection, selection, selectionArgs, sortOrder)
            // 之前写成 4 个参数会命中 API 26 的 query(Uri, String[], Bundle, CancellationSignal) 重载，
            // 语义完全不同（相当于"没有查询条件"），这里显式传 5 个参数避免歧义
            cursor = getContentResolver().query(
                    UserInfoContent.CONTENT_URI,
                    null,           // projection：null = 查所有列
                    null,           // selection：null = 不加 WHERE
                    null,           // selectionArgs：没有占位符，传 null
                    UserInfoContent.USER_ID + " ASC");   // sortOrder：按 id 升序

            if (cursor == null) {
                tvResult.setText("查询失败：Cursor 为 null");
                return;
            }

            if (cursor.getCount() == 0) {
                tvResult.setText("服务端暂无数据");
                ToastUtil.show(this, "查询完成，0 条");
                return;
            }

            // 逐行读取并拼接展示文本
            StringBuilder sb = new StringBuilder();
            sb.append("共 ").append(cursor.getCount()).append(" 条记录：\n");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(UserInfoContent.USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(UserInfoContent.USER_NAME));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(UserInfoContent.USER_AGE));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(UserInfoContent.USER_HEIGHT));
                float weight = cursor.getFloat(cursor.getColumnIndexOrThrow(UserInfoContent.USER_WEIGHT));
                boolean married = cursor.getInt(cursor.getColumnIndexOrThrow(UserInfoContent.USER_MARRIED)) == 1;

                sb.append("\nID：").append(id)
                        .append("\n姓名：").append(name)
                        .append("\n年龄：").append(age)
                        .append("\n身高：").append(height)
                        .append("\n体重：").append(weight)
                        .append("\n已婚：").append(married ? "是" : "否")
                        .append("\n");
            }
            tvResult.setText(sb.toString());
            ToastUtil.show(this, "查询成功，共 " + cursor.getCount() + " 条");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "query 失败，Uri = " + UserInfoContent.CONTENT_URI, e);
            tvResult.setText("查询失败：找不到服务端 provider\n" + e.getMessage());
            ToastUtil.show(this, "查询失败：请先安装 server_application");
        } catch (SecurityException e) {
            Log.e(TAG, "query 被拒绝：权限不足", e);
            tvResult.setText("查询失败：没有访问服务端的权限");
        } finally {
            // 无论成功失败都要关闭游标，否则 provider 侧的游标会一直占着资源
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==================== 改：update（单行） ====================

    /**
     * 按 ID 更新一行，用来演示 <b>item Uri</b>（{@code content://.../user/3}）的用法
     *
     * <p>和 insert 的关键区别：
     * 1. insert 用"整表 Uri"（{@code /user}），provider 走 {@code USERS} 分支；
     * 2. update 这里用"单行 Uri"（{@code /user/3}），provider 走 {@code USER_ID} 分支，
     *    自己把 id 拼成 {@code WHERE id = ?}，所以 <b>selection 传 null 也不会改到别的行</b>；
     * 3. 返回的是"受影响行数"：0 表示没有这个 id（不是异常）。
     *
     * <p>⚠️ 这是<b>整行覆盖</b>式更新：表单里没填的字段会被写成 0/null。
     */
    private void updateRow() {
        int id = readRowId();
        if (id <= 0) {
            return;     // readRowId 里已经提示过了
        }

        // 1. 校验与取值（与保存逻辑一致：姓名必填，数字留空按 0）
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            ToastUtil.show(this, "请先填写姓名（更新是整行覆盖，姓名不能空）");
            return;
        }
        int age;
        int height;
        float weight;
        try {
            age = toInt(etAge, 0);
            height = toInt(etHeight, 0);
            weight = toFloat(etWeight, 0f);
        } catch (NumberFormatException e) {
            ToastUtil.show(this, "年龄 / 身高 / 体重格式不正确");
            return;
        }

        // 2. 组装要更新的列（只放表单里的 5 个字段，id 不动）
        ContentValues values = new ContentValues();
        values.put(UserInfoContent.USER_NAME, name);
        values.put(UserInfoContent.USER_AGE, age);
        values.put(UserInfoContent.USER_HEIGHT, height);
        values.put(UserInfoContent.USER_WEIGHT, weight);
        values.put(UserInfoContent.USER_MARRIED, cbMarried.isChecked() ? 1 : 0);

        // 3. 把 id 拼到 Uri 末尾：content://.../user/3
        Uri rowUri = ContentUris.withAppendedId(UserInfoContent.CONTENT_URI, id);

        try {
            // 参数 3、4 传 null：单行 Uri 已经限定范围了，不需要再写 WHERE
            int rows = getContentResolver().update(rowUri, values, null, null);
            Log.d(TAG, "update " + rowUri + " 受影响行数 = " + rows);

            if (rows > 0) {
                ToastUtil.show(this, "更新成功，影响 " + rows + " 行");
                queryFromServer();      // 刷新列表，肉眼确认改对了
            } else {
                // 0 行说明没有这个 id —— 不抛异常，所以必须自己判断
                ToastUtil.show(this, "没有找到 ID = " + id + " 的记录");
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "update 失败：" + rowUri, e);
            ToastUtil.show(this, "更新失败：找不到服务端 provider");
        } catch (SecurityException e) {
            Log.e(TAG, "update 被拒绝：权限不足", e);
            ToastUtil.show(this, "更新失败：没有访问服务端的权限");
        }
    }

    // ==================== 删：delete（单行） ====================

    /** 删除前先确认：删除是不可逆操作 */
    private void confirmDeleteRow() {
        final int id = readRowId();
        if (id <= 0) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("删除记录")
                .setMessage("确定要删除 ID = " + id + " 这一行吗？此操作不可恢复。")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteRow(id))
                .show();
    }

    /**
     * 按 ID 删除一行（同样用 item Uri，provider 内部会拼 {@code WHERE id = ?}）
     *
     * @param id 要删除的行 ID
     */
    private void deleteRow(int id) {
        Uri rowUri = ContentUris.withAppendedId(UserInfoContent.CONTENT_URI, id);
        try {
            int rows = getContentResolver().delete(rowUri, null, null);
            Log.d(TAG, "delete " + rowUri + " 受影响行数 = " + rows);

            if (rows > 0) {
                ToastUtil.show(this, "已删除 " + rows + " 行");
                etId.setText("");       // 清掉输入，避免下次误删相邻行
                queryFromServer();
            } else {
                ToastUtil.show(this, "没有找到 ID = " + id + " 的记录");
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "delete 失败：" + rowUri, e);
            ToastUtil.show(this, "删除失败：找不到服务端 provider");
        } catch (SecurityException e) {
            Log.e(TAG, "delete 被拒绝：权限不足", e);
            ToastUtil.show(this, "删除失败：没有访问服务端的权限");
        }
    }

    // ==================== 小工具 ====================

    /**
     * 读取并校验"行 ID"输入框
     *
     * @return 合法的正整数 ID；为空 / 非数字 / ≤0 时返回 -1（并已弹出提示）
     */
    private int readRowId() {
        String idStr = etId.getText().toString().trim();
        if (idStr.isEmpty()) {
            ToastUtil.show(this, "请输入行 ID（可先点“查询全部”看结果里的 ID）");
            return -1;
        }
        try {
            int id = Integer.parseInt(idStr);
            if (id <= 0) {
                ToastUtil.show(this, "行 ID 必须是正整数");
                return -1;
            }
            return id;
        } catch (NumberFormatException e) {
            ToastUtil.show(this, "行 ID 格式不正确");
            return -1;
        }
    }

    /**
     * 把输入框内容转成 int，留空时用默认值
     *
     * @param et           输入框
     * @param defaultValue 留空时的默认值
     * @return 转换结果
     * @throws NumberFormatException 填了但不是合法整数
     */
    private int toInt(EditText et, int defaultValue) {
        String s = et.getText().toString().trim();
        return s.isEmpty() ? defaultValue : Integer.parseInt(s);
    }

    /**
     * 把输入框内容转成 float，留空时用默认值
     *
     * @param et           输入框
     * @param defaultValue 留空时的默认值
     * @return 转换结果
     * @throws NumberFormatException 填了但不是合法小数
     */
    private float toFloat(EditText et, float defaultValue) {
        String s = et.getText().toString().trim();
        return s.isEmpty() ? defaultValue : Float.parseFloat(s);
    }

    /** 清空表单，方便连续录入 */
    private void clearInputs() {
        etName.setText("");
        etAge.setText("");
        etHeight.setText("");
        etWeight.setText("");
        etId.setText("");
        cbMarried.setChecked(false);
        etName.requestFocus();
    }
}
