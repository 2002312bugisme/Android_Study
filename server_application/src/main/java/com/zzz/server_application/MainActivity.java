package com.zzz.server_application;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.server_application.provider.UserInfoContentProvider;

/**
 * 服务端首页：通过 ContentResolver 查询<b>自己</b>的 ContentProvider，把表内容显示出来
 *
 * <p>演示要点：
 * 1. 同一应用内也可以走 ContentResolver + Provider 这条路（不必直接开 SQLite），
 *    好处是和客户端的调用方式完全一致，能当"自检页面"用；
 * 2. Uri 由 authority 拼出来：{@code content://<authority>/user}；
 * 3. 为了让列名不写错，这里直接定义了与建表语句对应的常量（客户端那边也有一份同样的契约类）。
 *
 * <p>对应布局：{@code activity_main.xml}
 * <p>涉及 API：{@link ContentResolver#query}、{@link ContentUris}、{@link Cursor}
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ServerMain";

    /** 表名（与 UserDBHelper.TABLE_NAME 一致） */
    private static final String PATH_USER = "user";

    /** 完整访问 Uri：content://<authority>/user */
    private static final Uri CONTENT_URI =
            Uri.parse("content://" + UserInfoContentProvider.AUTHORITY + "/" + PATH_USER);

    // 列名常量：与 UserDBHelper#onCreate 里的建表语句逐个对应
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_AGE = "age";
    private static final String COL_HEIGHT = "height";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_MARRIED = "married";

    private TextView tvData;

    /**
     * 页面创建：绑定控件 + 首次加载数据
     *
     * @param savedInstanceState 系统保存的临时状态（数据在库里，无需恢复）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvData = findViewById(R.id.tv_data);

        Button btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(v -> loadData());

        // 进页面就查一次，省得手动点
        loadData();
    }

    /**
     * 通过 ContentResolver 查询本应用的 provider，并把结果渲染到 TextView
     */
    private void loadData() {
        Cursor cursor = null;
        try {
            // 5 个参数的 query：uri / 列 / where / whereArgs / 排序
            cursor = getContentResolver().query(
                    CONTENT_URI, null, null, null, COL_ID + " ASC");

            if (cursor == null || cursor.getCount() == 0) {
                tvData.setText("user_info 表暂无数据\n（到客户端录入一条试试）");
                return;
            }

            StringBuilder sb = new StringBuilder("共 " + cursor.getCount() + " 条记录：\n");
            while (cursor.moveToNext()) {
                sb.append("\nID：").append(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)))
                        .append("\n姓名：").append(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)))
                        .append("\n年龄：").append(cursor.getInt(cursor.getColumnIndexOrThrow(COL_AGE)))
                        .append("\n身高：").append(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HEIGHT)))
                        .append("\n体重：").append(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_WEIGHT)))
                        .append("\n已婚：").append(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COL_MARRIED)) == 1 ? "是" : "否")
                        .append("\n");
            }
            tvData.setText(sb.toString());
        } catch (Exception e) {
            // 理论上同应用内不会失败，真失败多半是 provider 没在清单注册 / Uri 拼错
            Log.e(TAG, "查询失败: " + CONTENT_URI, e);
            tvData.setText("查询失败：" + e.getMessage());
            Toast.makeText(this, "查询失败，请检查 provider 注册", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
