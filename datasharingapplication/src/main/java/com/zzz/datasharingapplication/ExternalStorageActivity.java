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

/**
 * 文件存储演示：一次把同一份表单内容写进三种目录，再读回来对比路径
 *
 * <p>演示要点：
 * 1. 三种目录的区别（重点记"谁能看到"和"卸载后是否保留"）：
 *    <ul>
 *      <li><b>内部存储</b> {@code getFilesDir()} → /data/data/&lt;包名&gt;/files，
 *          仅本应用可见，卸载即删，不需要任何权限；</li>
 *      <li><b>外部私有目录</b> {@code getExternalFilesDir(null)} →
 *          /storage/emulated/0/Android/data/&lt;包名&gt;/files，
 *          Android 10+ 无需权限、其它应用（除文件管理器）看不到，卸载即删；</li>
 *      <li><b>外部公共目录</b> {@code getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS)} →
 *          Documents 目录，用户与其它应用都能看到，Android 10+ 走分区存储受限。</li>
 *    </ul>
 * 2. 权限只在 <b>Android 6.0 ~ 9.0</b> 需要动态申请（{@code WRITE_EXTERNAL_STORAGE}）；
 *    Android 10 起分区存储生效，写自己的私有目录不再需要权限，所以本页把申请范围限制在 M~P；
 * 3. Android 10 之前公共目录是 {@code getExternalStorageDirectory()}（存储根目录），
 *    10 之后改成按类型划分的 {@code DIRECTORY_DOCUMENTS}——代码里用 SDK_INT 做了分支。
 *
 * <p>对应布局：{@code activity_external_storage.xml}
 * <p>涉及 API：{@link ContextCompat#checkSelfPermission}、{@link ActivityCompat#requestPermissions}、
 * {@link Environment#getExternalStoragePublicDirectory}、{@link FileUtil#saveText} / {@link FileUtil#openText}
 */
public class ExternalStorageActivity extends AppCompatActivity {

    // ===== 表单控件 =====
    private EditText etName, etAge;
    private TextView tvResult;      // 读取结果展示

    // 三个目录下各自使用的文件名（放在一起方便对照）
    private static final String PUBLIC_FILE_NAME = "public_form.txt";
    private static final String PRIVATE_FILE_NAME = "private_form.txt";
    private static final String INTERNAL_FILE_NAME = "internal_form.txt";

    /**
     * 页面创建：绑定控件 → 绑定保存 / 读取按钮 → 申请权限
     *
     * @param savedInstanceState 系统保存的临时状态（无需恢复）
     */
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

    /**
     * 动态申请存储权限（只在 Android 6.0 ~ 9.0 需要）
     *
     * <p>为什么限定这个区间：Android 10（Q）起启用分区存储，
     * 写自己的外部私有目录不再需要权限；Android 6（M）之前是安装即授权。
     */
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Android 6.0 ~ 9.0 需要动态申请
            // checkSelfPermission 先查，已授权就不要再弹框（否则每次进页面都弹）
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // 第二个参数是权限数组（读写一起要），第三个是请求码，回调里用它区分是哪个请求
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 100);
            }
        }
    }

    /**
     * 权限申请结果回调（用户点"允许 / 拒绝"后由系统调用）
     *
     * @param requestCode  请求码，与 requestPermissions 时传入的一致（这里是 100）
     * @param permissions  本次申请的权限名数组（注解 @NonNull 是给编译器的非空提示）
     * @param grantResults 与 permissions 一一对应的授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // 逐个检查：只要有一个被拒绝就提示（本例中读、写是配套的）
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "未授予存储权限，无法保存到公共空间",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * 把两个输入框拼成要保存的文本
     *
     * @return 形如 "姓名: 张三\n年龄: 20\n" 的字符串
     */
    private String getFormText() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        return "姓名: " + name + "\n年龄: " + age + "\n";
    }

    /**
     * 保存到三种存储空间（同一份内容写三次，方便对比路径差异）
     *
     * <p>注意 {@code getExternalFilesDir(null)} 可能返回 <b>null</b>（外部存储未挂载），
     * 真实项目里必须判空，否则 {@code new File(null, name)} 会直接 NPE；
     * 本页为了保持示例简洁未加判断。
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
     * 从三种存储空间读回内容并显示
     *
     * <p>每个文件都先 {@code exists()} 判断再读，避免直接读不存在的文件抛异常；
     * 三元表达式让"路径 + 内容或提示"一次性拼进 StringBuilder。
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