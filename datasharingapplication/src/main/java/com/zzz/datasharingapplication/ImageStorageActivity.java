package com.zzz.datasharingapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片存储演示：把 Bitmap 写成 PNG 文件，再读回来显示
 *
 * <p>演示要点：
 * 1. 目标目录用 {@code getExternalFilesDir(DIRECTORY_PICTURES)}——应用专属外部目录，
 *    <b>不需要任何运行时权限</b>（Android 10+ 尤其重要，公共目录已被分区存储限制）；
 * 2. {@code getExternalFilesDir()} 可能返回 <b>null</b>（外部存储未挂载），
 *    必须判空，否则 {@code new File(null, name)} 直接 NPE；
 * 3. 写文件要处理三件事：目录不存在（先 mkdirs）、IO 异常（try/catch）、
 *    流必须关闭（finally）；
 * 4. 读回来有至少三种写法（{@code decodeFile} / 工具类封装 / {@code setImageURI}），
 *    代码里保留了两条注释掉的写法，方便对照。
 *
 * <p>对应布局：{@code activity_image_storage.xml}
 * <p>涉及 API：{@link Environment#DIRECTORY_PICTURES}、{@link Bitmap#compress}、
 * {@link BitmapFactory#decodeFile}、{@link FileOutputStream}
 */
public class ImageStorageActivity extends AppCompatActivity {

    // 存储卡上的文件名（与项目里已有的 drawable 同名，方便识别）
    private static final String FILE_NAME = "checkbox_checked.png";

    // ===== 控件 =====
    private ImageView ivContent;   // 显示读取到的图片
    private Button btnSave;        // 保存
    private Button btnRead;        // 读取

    // 图片文件的绝对路径；为 null 表示外部存储不可用
    private String filePath;

    /**
     * 页面创建：绑定控件 → 计算文件路径 → 注册按钮事件
     *
     * @param savedInstanceState 系统保存的临时状态（无需恢复）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_storage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 1. 绑定布局中的三个控件
        //    注意：成员变量不会自动赋值，漏掉 findViewById 时它们恒为 null，
        //    下面注册监听器会抛 NullPointerException，表现为"一启动就闪退"
        ivContent = findViewById(R.id.iv_content);
        btnSave = findViewById(R.id.btn_save);
        btnRead = findViewById(R.id.btn_read);

        // 2. 存储卡上的图片文件路径（应用外部私有目录，无需存储权限）
        //    getExternalFilesDir 拿到的是 /storage/emulated/0/Android/data/<包名>/files/Pictures，
        //    这个目录归属于本应用，系统自动授予读写权，所以整个流程不需要申请任何运行时权限。
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // getExternalFilesDir 在外部存储不可用时（如 SD 卡未挂载）会返回 null，
        // 直接 new File(null, xxx) 会抛 NPE，所以这里做一次判空
        if (dir != null && !dir.exists()) {
            dir.mkdirs();   // 首次运行时目录通常还没被创建
        }
        filePath = (dir == null) ? null : new File(dir, FILE_NAME).getAbsolutePath();

        // 3. 注册按钮点击事件
        btnSave.setOnClickListener(v -> saveImage());

        btnRead.setOnClickListener(v -> readImage());
    }

    /**
     * 保存图片到存储卡
     *
     * <p>这里用资源图片模拟，实际可以是拍照、网络下载得到的 Bitmap。
     * {@code compress()} 返回 boolean：true 表示压缩并写入成功。
     */
    private void saveImage() {
        // 0. filePath 为 null 说明外部存储不可用（getExternalFilesDir 返回了 null）
        if (filePath == null) {
            Toast.makeText(this, "外部存储不可用，无法保存", Toast.LENGTH_LONG).show();
            return;
        }

        // 1. 获取要保存的 Bitmap（这里从资源中解码）
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.checkbox_checked);

        // 2. 写入文件
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            // 资源本身是 PNG，这里以 PNG 格式压缩（无损），quality 对 PNG 无效
            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (success) {
                Toast.makeText(this, "保存成功：\n" + filePath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "保存异常：" + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从存储卡读取图片并显示（三种写法，见方法内注释）
     *
     * <p>逐层校验：路径是否可用 → 文件是否存在 → 再解码。
     * {@code decodeFile} 解码失败会返回 null（例如文件损坏），此时 setImageBitmap(null) 不会崩，
     * 只是界面空白，所以这里没有再判空。
     */
    private void readImage() {
        if (filePath == null) {
            Toast.makeText(this, "外部存储不可用，无法读取", Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在，请先保存", Toast.LENGTH_SHORT).show();
            return;
        }

        // 方式一：通过自定义工具类 FileUtil.openImage(path)
        // Bitmap b2 = FileUtil.openImage(filePath);
        // ivContent.setImageBitmap(b2);

        // 方式二：通过 BitmapFactory.decodeFile(path)
        Bitmap b2 = BitmapFactory.decodeFile(filePath);
        ivContent.setImageBitmap(b2);

        // 方式三：直接调用 setImageURI 方法，设置图像视图的路径对象
        //         注意要传 Uri 而不是 String，正确写法是 Uri.fromFile(file)
        // ivContent.setImageURI(Uri.fromFile(file));

        Toast.makeText(this, "读取完成", Toast.LENGTH_SHORT).show();
    }
}