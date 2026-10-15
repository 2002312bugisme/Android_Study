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

public class SqliteActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_create_database;
    private Button btn_delete_database;
    private TextView tv_database_result;

    // 数据库名称
    private static final String DATABASE_NAME = "student.db";

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

        // 绑定点击事件
        btn_create_database.setOnClickListener(this);
        btn_delete_database.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_create_database) {
            // 创建或打开数据库
            SQLiteDatabase db = openOrCreateDatabase(
                    DATABASE_NAME,
                    MODE_PRIVATE,
                    null
            );

            // 获取应用内部文件目录
            String filesDir = getFilesDir().getAbsolutePath();

            // 获取数据库实际路径
            String dbPath = db.getPath();

            // 显示路径
            tv_database_result.setText(
                    "数据库创建或打开成功\n\n"
                            + "应用文件目录：\n" + filesDir
                            + "\n\n数据库实际路径：\n" + dbPath
            );

            // 关闭数据库
            db.close();

        } else if (v.getId() == R.id.btn_delete_database) {

            // 删除数据库
            boolean result = deleteDatabase(DATABASE_NAME);

            if (result) {
                tv_database_result.setText("数据库删除成功");
            } else {
                tv_database_result.setText("数据库不存在或删除失败");
            }
        }
    }
}