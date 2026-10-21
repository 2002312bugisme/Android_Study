package com.zzz.datasharingapplication;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * SQLite 最原始用法：直接 {@code openOrCreateDatabase} / {@code deleteDatabase}，不借助 SQLiteOpenHelper
 *
 * <p>演示要点：
 * 1. {@code openOrCreateDatabase(name, mode, factory)} 等价于"有就打开、没有就创建"，
 *    句柄用完后要自己 {@code close()}；
 * 2. 数据库文件固定落在 {@code /data/data/<包名>/databases/<name>}，
 *    页面上把"应用文件目录"和"数据库实际路径"都打印出来，方便对照目录结构；
 * 3. {@code deleteDatabase(name)} 返回 boolean：文件不存在时返回 false（不算异常）；
 * 4. 实际项目里不该这么用——没有建表/升级管理，也没法统一版本。
 *    正解是下一节的 {@link SqliteHelperActivity}（SQLiteOpenHelper 子类）。
 *
 * <p>对应布局：{@code activity_sqlite.xml}
 * <p>涉及 API：{@link android.content.Context#openOrCreateDatabase}、
 * {@link android.content.Context#deleteDatabase}、{@link SQLiteDatabase#getPath()}、
 * {@link android.content.Context#getFilesDir()}
 */
public class SqliteActivity extends AppCompatActivity implements View.OnClickListener {

    // ===== 控件 =====
    private Button btn_create_database;   // 创建 / 打开数据库
    private Button btn_delete_database;   // 删除数据库
    private TextView tv_database_result;  // 结果显示

    // 数据库名称（不用写 .db 也行，系统会自动补全为 student.db）
    private static final String DATABASE_NAME = "student.db";

    /**
     * 页面创建：绑定控件与点击事件
     *
     * @param savedInstanceState 系统保存的临时状态（无需恢复）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sqlite);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 获取组件
        btn_create_database = findViewById(R.id.btn_create_database);
        btn_delete_database = findViewById(R.id.btn_delete_database);
        tv_database_result = findViewById(R.id.tv_database_result);

        // 绑定点击事件：让本类统一处理（implements View.OnClickListener）
        btn_create_database.setOnClickListener(this);
        btn_delete_database.setOnClickListener(this);
    }

    /**
     * 统一点击回调：按 id 分发到"创建"或"删除"
     *
     * @param v 被点击的按钮
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_create_database) {
            // 创建或打开数据库：MODE_PRIVATE 表示仅本应用可访问；
            // 第三个参数 CursorFactory 传 null 表示用系统默认实现
            SQLiteDatabase db = openOrCreateDatabase(
                    DATABASE_NAME,
                    MODE_PRIVATE,
                    null
            );

            // 获取应用内部文件目录：/data/data/<包名>/files
            String filesDir = getFilesDir().getAbsolutePath();

            // 获取数据库实际路径：/data/data/<包名>/databases/student.db
            String dbPath = db.getPath();

            // 把两个路径显示出来，方便直观理解"数据库放在哪"
            tv_database_result.setText(
                    "数据库创建或打开成功\n\n"
                            + "应用文件目录：\n" + filesDir
                            + "\n\n数据库实际路径：\n" + dbPath
            );

            // 用完关闭数据库，释放句柄（这里没有其他操作，所以立刻关）
            db.close();

        } else if (v.getId() == R.id.btn_delete_database) {

            // 删除数据库：直接删文件，表结构与数据一起没
            boolean result = deleteDatabase(DATABASE_NAME);

            if (result) {
                tv_database_result.setText("数据库删除成功");
            } else {
                // 文件不存在时也返回 false，所以提示里写"不存在或删除失败"
                tv_database_result.setText("数据库不存在或删除失败");
            }
        }
    }
}
