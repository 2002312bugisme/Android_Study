package com.zzz.datasharingapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.datasharingapplication.util.FileUtil;

import java.io.File;

public class ExternalStorageActivity extends AppCompatActivity {

    private EditText etName, etAge;
    private TextView tvResult;

    // 公共存储目录下的文件名
    private static final String PUBLIC_FILE_NAME = "public_form.txt";
    private static final String PRIVATE_FILE_NAME = "private_form.txt";
    private static final String INTERNAL_FILE_NAME = "internal_form.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_external_storage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        tvResult = findViewById(R.id.tvResult);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnRead = findViewById(R.id.btnRead);

        btnSave.setOnClickListener(v -> saveAll());
        btnRead.setOnClickListener(v -> readAll());

        // 动态申请存储权限（Android 6.0+）
        requestStoragePermission();
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Android 6.0 ~ 9.0 需要动态申请
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "未授予存储权限，无法保存到公共空间",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 拼接表单内容
     */
    private String getFormText() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        return "姓名: " + name + "\n年龄: " + age + "\n";
    }

    /**
     * 保存到三种存储空间
     */
    private void saveAll() {
        String content = getFormText();

        // 1. 内部存储空间: /data/data/<包名>/files/internal_form.txt
        File internalFile = new File(getFilesDir(), INTERNAL_FILE_NAME);
        FileUtil.saveText(internalFile.getAbsolutePath(), content);

        // 2. 外部私有存储空间:
        //    /storage/emulated/0/Android/data/<包名>/files/private_form.txt
        File privateDir = getExternalFilesDir(null); // 参数 null 表示根目录
        File privateFile = new File(privateDir, PRIVATE_FILE_NAME);
        FileUtil.saveText(privateFile.getAbsolutePath(), content);

        // 3. 外部公共存储空间:
        //    /storage/emulated/0/Documents/public_form.txt (Android 10+)
        //    /storage/emulated/0/public_form.txt (Android 9-)
        File publicDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用分区存储的公共目录
            publicDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
        } else {
            publicDir = Environment.getExternalStorageDirectory();
        }
        if (!publicDir.exists()) {
            publicDir.mkdirs();
        }
        File publicFile = new File(publicDir, PUBLIC_FILE_NAME);
        FileUtil.saveText(publicFile.getAbsolutePath(), content);

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 从三种存储空间读取
     */
    private void readAll() {
        StringBuilder sb = new StringBuilder();

        // 1. 内部存储
        File internalFile = new File(getFilesDir(), INTERNAL_FILE_NAME);
        sb.append("【内部存储】\n路径: ").append(internalFile.getAbsolutePath()).append("\n");
        sb.append(internalFile.exists() ? FileUtil.openText(internalFile.getAbsolutePath())
                : "文件不存在").append("\n\n");

        // 2. 外部私有存储
        File privateFile = new File(getExternalFilesDir(null), PRIVATE_FILE_NAME);
        sb.append("【外部私有存储】\n路径: ").append(privateFile.getAbsolutePath()).append("\n");
        sb.append(privateFile.exists() ? FileUtil.openText(privateFile.getAbsolutePath())
                : "文件不存在").append("\n\n");

        // 3. 外部公共存储
        File publicDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            publicDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS);
        } else {
            publicDir = Environment.getExternalStorageDirectory();
        }
        File publicFile = new File(publicDir, PUBLIC_FILE_NAME);
        sb.append("【外部公共存储】\n路径: ").append(publicFile.getAbsolutePath()).append("\n");
        sb.append(publicFile.exists() ? FileUtil.openText(publicFile.getAbsolutePath())
                : "文件不存在").append("\n\n");

        tvResult.setText(sb.toString());
    }
}