package com.zzz.datasharingapplication.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 文件读写工具类：文本与图片的"存 / 取"各两个方法
 *
 * <p>演示要点：
 * 1. 两套关闭流的写法都写了，可以直接对照：
 *    - {@link #saveText(String, String)} 用 <b>try-with-resources</b>（Java 7+），流会自动 close，最省心；
 *    - 其余三个用传统的 <b>try / catch / finally</b>，用于演示"资源必须在 finally 里关闭"的老写法；
 * 2. 这里统一把受检异常（{@link IOException} 等）包成 {@link RuntimeException} 抛出，
 *    调用方不必层层 throws，但代价是<b>异常会被上层当成崩溃</b>——生产代码应该返回 Result 或回调错误；
 * 3. 所有路径都是绝对路径，调用方自己负责保证父目录存在（本类不会帮你 mkdirs）。
 *
 * <p>涉及 API：FileWriter / BufferedWriter / FileReader / BufferedReader /
 * FileOutputStream / FileInputStream / {@link Bitmap#compress} / {@link BitmapFactory#decodeStream}
 */
public class FileUtil {

    /**
     * 把字符串保存到指定路径的文本文件（覆盖写）
     *
     * @param path 目标文件绝对路径
     * @param txt  要写入的内容
     */
    public static void saveText(String path, String txt) {
        // try-with-resources：括号里声明的流在离开 try 块时自动 close()，即使抛异常也会关
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path))) {
            bufferedWriter.write(txt);       // FileWriter 默认覆盖模式，不会追加
        } catch (Exception e) {
            // 统一包装成运行时异常向上抛，调用方不用写 try-catch
            throw new RuntimeException(e);
        }
    }

    /**
     * 从指定路径的文本文件中读取全部内容
     *
     * <p>注意：这里用 {@code readLine()} 逐行读且没有补回 {@code \n}，
     * 所以原文的换行符会丢掉（本仓库只拿它读单行内容，故未处理）。
     *
     * @param path 源文件绝对路径
     * @return 文件内容拼成的字符串（不含换行符）
     */
    public static String openText(String path) {
        BufferedReader bufferedReader = null;      // 声明在 try 外面，finally 里才拿得到引用
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(path));
            String line = null;
            // readLine() 读到文件尾返回 null，这也是循环的结束条件
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // finally 一定会执行：不管上面是正常结束还是抛了异常，都要把流关掉
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 把位图数据保存到指定路径的图片文件
     *
     * <p>注意：目标目录必须已存在，否则 FileOutputStream 会抛 FileNotFoundException
     * （可以用 {@code getExternalFilesDir()} 拿到的目录，系统会保证它存在）。
     *
     * @param path   目标文件绝对路径，例如 .../files/Download/goods_0.png
     * @param bitmap 要保存的位图
     */
    public static void saveImage(String path, Bitmap bitmap) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // 把位图数据压缩到文件输出流中：
            // 参数 1 = 压缩格式（PNG 无损、支持透明；JPEG 有损、不支持透明）
            // 参数 2 = 质量 0~100（PNG 是无损格式，这个参数会被忽略）
            // 参数 3 = 输出流
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    // close() 里才会真正把缓冲区刷到磁盘，不关可能丢数据
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 把指定路径的图片文件读成位图
     *
     * @param path 源文件绝对路径
     * @return 解码得到的 Bitmap；文件损坏或不是图片时返回 null
     */
    public static Bitmap openImage(String path) {
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            // decodeStream 直接从流里解码图像，比先读成 byte[] 再 decodeByteArray 省一次内存拷贝
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bitmap;
    }
}
