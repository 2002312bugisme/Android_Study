package com.zzz.okhttpapplication;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * OkHttp 基础用法演示页。
 *
 * <p>对应布局：res/layout/activity_ok_http.xml
 * <p>对应服务端：D:\Code\Android\HttpClient（Spring Boot，接口都在 /api 下）
 *
 * <p>六个按钮分别演示：
 * <ol>
 *   <li>GET 同步 —— {@code newCall(request).execute()}，阻塞调用线程</li>
 *   <li>GET 异步 —— {@code newCall(request).enqueue(callback)}，回调在 OkHttp 线程</li>
 *   <li>POST 同步 —— {@code RequestBody} 发 JSON 字符串</li>
 *   <li>POST 异步 —— 同上，只换了提交方式</li>
 *   <li>表单提交 —— {@code FormBody}，即 application/x-www-form-urlencoded</li>
 *   <li>文件上传 —— 系统相册选图，{@code MultipartBody} 以 multipart/form-data 上传</li>
 * </ol>
 *
 * <p>涉及 API：OkHttpClient / Request / RequestBody / FormBody / MultipartBody /
 * Call.execute / Call.enqueue / Response。
 */
public class OkHttpActivity extends AppCompatActivity {

    private static final String TAG = "OkHttpDemo";

    /**
     * 服务端地址默认值。
     * <p>模拟器：http://10.0.2.2:8080 —— 10.0.2.2 是模拟器内部指向宿主机的固定别名，
     * 用 127.0.0.1 会指向模拟器自己，连不上电脑上的服务。
     * <p>真机：改成电脑的局域网 IP（例如 http://192.168.0.45:8080），手机和电脑要在同一个 Wi-Fi 下；
     * 也可以用 adb reverse tcp:8080 tcp:8080 把手机的 localhost:8080 反向映射到电脑，
     * 这时地址写 http://127.0.0.1:8080（真机免局域网、免防火墙）。
     */
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080";

    /** JSON 的媒体类型：服务端靠它决定用 @RequestBody 还是表单方式解析请求体 */
    private static final MediaType JSON_TYPE = Objects.requireNonNull(
            MediaType.get("application/json; charset=utf-8"));

    /**
     * OkHttpClient 应该是全局单例：
     * 它内部维护连接池、线程池和缓存，每发一个请求就 new 一个会造成连接无法复用、线程泄漏。
     * 这里放在 Activity 字段上便于演示，真实项目放在 Application 或单例工具类里。
     */
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)    // 建立 TCP 连接的时限
            .readTimeout(10, TimeUnit.SECONDS)      // 等响应的时限
            .writeTimeout(20, TimeUnit.SECONDS)     // 写请求体的时限（上传图片要放宽）
            .build();

    /**
     * 同步请求专用线程池。
     * execute() 是阻塞调用，而 Android 主线程禁止网络操作
     * （直接调用会抛 NetworkOnMainThreadException），所以同步请求必须放到子线程。
     * 单线程池还能顺带演示「同步请求会排队、一个做完才做下一个」。
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 系统相册选择器。
     * 用 ActivityResultContracts.GetContent 拉起 SAF（存储访问框架），拿到的是 content:// Uri，
     * 不需要申请任何存储权限；回调里的 Uri 就是用户选中的图片。
     * 必须在 Activity 创建完成（STARTED 之前）注册，所以声明成字段并用 registerForActivityResult 初始化。
     */
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadImage);

    private EditText etBaseUrl;
    private TextView tvResult;
    private ScrollView scrollPage;
    /** 输出区里现在还是布局文件写的占位文案，第一次追加结果时要先把它清掉 */
    private boolean firstOutput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ok_http);
        // 让内容避开状态栏/导航栏
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etBaseUrl = findViewById(R.id.et_base_url);
        tvResult = findViewById(R.id.tv_result);
        scrollPage = findViewById(R.id.main);
        etBaseUrl.setText(DEFAULT_BASE_URL);

        // 每个按钮只做一件事，具体流程在下面的方法里
        findViewById(R.id.btn_get_sync).setOnClickListener(v -> getSync());
        findViewById(R.id.btn_get_async).setOnClickListener(v -> getAsync());
        findViewById(R.id.btn_post_sync).setOnClickListener(v -> postJsonSync());
        findViewById(R.id.btn_post_async).setOnClickListener(v -> postJsonAsync());
        findViewById(R.id.btn_form).setOnClickListener(v -> postForm());
        // 上传：先拉起相册，选完图才真正发请求（回调见 uploadImage）
        findViewById(R.id.btn_upload).setOnClickListener(v -> pickImage.launch("image/*"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 页面销毁时关掉线程池，避免线程一直挂着
        executor.shutdown();
    }

    // ==================== 1. GET 同步 ====================

    /**
     * GET 同步请求：{@code execute()} 会一直阻塞到拿到响应（或超时/失败）。
     */
    private void getSync() {
        // HttpUrl 负责拼装查询参数，中文、空格会自动做百分号编码，不用自己 URLEncoder
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl() + "/api/get"))
                .newBuilder()
                .addQueryParameter("name", "张三")
                .addQueryParameter("age", "18")
                .build();

        // GET 请求不需要请求体，build() 后默认就是 GET
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        append("→ GET 同步 " + url);

        // 关键点：execute() 不能写在主线程，交给 executor 的线程执行
        executor.execute(() -> {
            long start = System.currentTimeMillis();
            // Response 持有连接和响应体，必须关闭；try-with-resources 会在结束时自动 close()
            try (Response response = client.newCall(request).execute()) {
                // body() 只能读一次；string() 会把响应体按 Content-Type 的编码转成字符串
                String body = response.body() == null ? "" : response.body().string();
                showResult("GET 同步", response.code(), System.currentTimeMillis() - start, body);
            } catch (IOException e) {
                // 连不上、超时、DNS 失败等都会走到这里
                showError("GET 同步", e);
            }
        });
    }

    // ==================== 2. GET 异步 ====================

    /**
     * GET 异步请求：{@code enqueue()} 立刻返回，请求交给 OkHttp 的线程池，
     * 结果通过 Callback 回调（回调线程不是主线程，更新 UI 要切回主线程）。
     */
    private void getAsync() {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl() + "/api/get"))
                .newBuilder()
                .addQueryParameter("name", "李四")
                .addQueryParameter("age", "20")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        append("→ GET 异步 " + url);
        // 计时起点要在 enqueue 之前取，否则算不出真实耗时
        long start = System.currentTimeMillis();

        client.newCall(request).enqueue(new Callback() {
            /** 请求没成功：网络异常、超时、连接被拒等 */
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showError("GET 异步", e);
            }

            /** 请求成功：只要服务器有响应就走这里（4xx/5xx 也算成功，要看 code 判断） */
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    String body = resp.body() == null ? "" : resp.body().string();
                    showResult("GET 异步", resp.code(), System.currentTimeMillis() - start, body);
                }
            }
        });
    }

    // ==================== 3. POST 同步（RequestBody 发 JSON） ====================

    /**
     * POST 同步请求，请求体是 JSON 字符串。
     * RequestBody 就是「请求体」的统一抽象，JSON、纯文本、表单、文件都由它的子类/工厂方法产生。
     */
    private void postJsonSync() {
        // 手写 JSON 只为演示；真实项目用 Gson / Moshi 把对象序列化
        String json = "{\"name\":\"张三\",\"age\":18,\"from\":\"android-okhttp\"}";

        // 注意 Java 里的调用顺序：内容在前，媒体类型在后。
        // 媒体类型写错（比如漏掉 application/json）服务端会返回 415 Unsupported Media Type
        RequestBody body = RequestBody.create(json, JSON_TYPE);

        Request request = new Request.Builder()
                .url(baseUrl() + "/api/post")
                .post(body)             // 换成 .post(body) 后请求方法才是 POST
                .build();

        append("→ POST 同步 " + request.url());
        append("  请求体：" + json);

        executor.execute(() -> {
            long start = System.currentTimeMillis();
            try (Response response = client.newCall(request).execute()) {
                String result = response.body() == null ? "" : response.body().string();
                showResult("POST 同步", response.code(), System.currentTimeMillis() - start, result);
            } catch (IOException e) {
                showError("POST 同步", e);
            }
        });
    }

    // ==================== 4. POST 异步（RequestBody 发 JSON） ====================

    /**
     * POST 异步请求，和上面唯一的区别是 execute() 换成 enqueue()。
     */
    private void postJsonAsync() {
        String json = "{\"name\":\"王五\",\"age\":22,\"from\":\"android-okhttp\"}";
        RequestBody body = RequestBody.create(json, JSON_TYPE);
        Request request = new Request.Builder()
                .url(baseUrl() + "/api/post")
                .post(body)
                .build();

        append("→ POST 异步 " + request.url());
        append("  请求体：" + json);
        long start = System.currentTimeMillis();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showError("POST 异步", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    String result = resp.body() == null ? "" : resp.body().string();
                    showResult("POST 异步", resp.code(), System.currentTimeMillis() - start, result);
                }
            }
        });
    }

    // ==================== 5. 表单提交（FormBody） ====================

    /**
     * 表单提交：FormBody 是 RequestBody 的子类，Content-Type 固定为
     * application/x-www-form-urlencoded，多个字段会被拼成 name=张三&amp;age=20 的形式。
     * 中文和空格由 FormBody 自动做 URL 编码。
     */
    private void postForm() {
        FormBody form = new FormBody.Builder()
                .add("name", "李四")
                .add("age", "20")
                .add("city", "深圳")
                .build();

        Request request = new Request.Builder()
                .url(baseUrl() + "/api/form")
                .post(form)
                .build();

        append("→ 表单提交 " + request.url());
        long start = System.currentTimeMillis();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showError("表单提交", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    String result = resp.body() == null ? "" : resp.body().string();
                    showResult("表单提交", resp.code(), System.currentTimeMillis() - start, result);
                }
            }
        });
    }

    // ==================== 6. 文件上传（MultipartBody） ====================

    /**
     * 上传相册里选中的图片。
     * MultipartBody 会把请求体拼成 multipart/form-data：每个 addFormDataPart 是一个「部分」，
     * 服务端用 @RequestParam("file") MultipartFile 接收同名的那个部分。
     *
     * @param uri 相册选择器回调给的 content:// Uri；用户点返回键取消时为 null
     */
    private void uploadImage(Uri uri) {
        if (uri == null) {
            return;     // 没有选图，什么都不做
        }

        ContentResolver resolver = getContentResolver();
        String fileName = queryDisplayName(uri);        // 相册里的原始文件名
        long fileSize = queryFileSize(uri);             // 字节数，未知时为 -1
        String mimeType = resolver.getType(uri);        // 例如 image/jpeg，可能为 null
        MediaType mediaType = MediaType.get(mimeType == null ? "application/octet-stream" : mimeType);

        MultipartBody body = new MultipartBody.Builder()
                // setType(FORM) 决定 Content-Type 里的 boundary 生成方式
                .setType(MultipartBody.FORM)
                // 普通文本字段：字段名 desc
                .addFormDataPart("desc", "来自相册的图片")
                // 文件字段：字段名 file、文件名、内容（RequestBody）
                .addFormDataPart("file", fileName,
                        new UriRequestBody(resolver, uri, fileSize, mediaType))
                .build();

        Request request = new Request.Builder()
                .url(baseUrl() + "/api/upload")
                .post(body)
                .build();

        append("→ 上传 " + fileName + "（" + fileSize + " 字节，" + mediaType + "）");
        long start = System.currentTimeMillis();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showError("上传图片", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    String result = resp.body() == null ? "" : resp.body().string();
                    showResult("上传图片", resp.code(), System.currentTimeMillis() - start, result);
                }
            }
        });
    }

    /**
     * 查询相册文件的显示名（ContentResolver 的标准查法）。
     *
     * @param uri content:// 形式的图片地址
     * @return 文件名；查不到时拼一个带时间戳的兜底名
     */
    private String queryDisplayName(Uri uri) {
        // projection 只取要用的那一列，OpenableColumns.DISPLAY_NAME 是所有 DocumentsProvider 都支持的列
        try (Cursor cursor = getContentResolver().query(
                uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null) {
                    return name;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "查询文件名失败", e);
        }
        return "upload_" + System.currentTimeMillis() + ".jpg";
    }

    /**
     * 查询相册文件的字节数。
     *
     * @param uri content:// 形式的图片地址
     * @return 文件大小；查不到返回 -1（OkHttp 会退化成 chunked 分块上传）
     */
    private long queryFileSize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
                uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getLong(0);
            }
        } catch (Exception e) {
            Log.w(TAG, "查询文件大小失败", e);
        }
        return -1;
    }

    // ==================== 内部工具 ====================

    /**
     * 从输入框读服务端地址，去掉结尾多余的 /，避免拼出 //api/get。
     *
     * @return 形如 http://192.168.0.45:8080 的地址
     */
    private String baseUrl() {
        String base = etBaseUrl.getText().toString().trim();
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    /**
     * 把一条结果追加到输出区（必须在主线程调用）。
     *
     * @param text 完整的一行/多行结果
     */
    private void append(String text) {
        if (firstOutput) {
            // 清掉 XML 里的占位提示，否则第一条结果会和它连在一起
            tvResult.setText("");
            firstOutput = false;
        }
        tvResult.append(text + "\n\n");
        // 新内容在底部：等这一帧布局完成后再滚到底，直接调用会滚到旧位置
        scrollPage.post(() -> scrollPage.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * 统一的结果输出：把状态码、耗时、执行线程一起打出来，方便对比同步/异步。
     * 内部会切回主线程，因此可以在 OkHttp 的回调线程里直接调用。
     *
     * @param action   哪个演示功能，例如 "GET 异步"
     * @param httpCode HTTP 状态码，200 表示成功
     * @param costMs   从发请求到读完响应体的耗时（毫秒）
     * @param body     响应体原文
     */
    private void showResult(String action, int httpCode, long costMs, String body) {
        // 线程名要在切线程之前取，否则永远拿到 main
        String thread = Thread.currentThread().getName();
        Log.d(TAG, action + " -> " + httpCode + " (" + costMs + "ms) on " + thread);
        runOnUiThread(() -> append("【" + action + "】HTTP " + httpCode
                + "　耗时 " + costMs + "ms　线程 " + thread + "\n" + body));
    }

    /**
     * 统一的失败输出。
     *
     * @param action 哪个演示功能
     * @param e      捕获到的 IO 异常（连接失败、超时、读写中断等）
     */
    private void showError(String action, IOException e) {
        Log.e(TAG, action + " 失败", e);
        String thread = Thread.currentThread().getName();
        // 常见原因：地址写错、服务端没启动、明文 HTTP 被拦、手机和电脑不在同一网段
        runOnUiThread(() -> append("【" + action + "】失败　线程 " + thread + "\n"
                + e.getClass().getSimpleName() + ": " + e.getMessage()));
    }

    /**
     * 把 content:// Uri 的字节流包装成 OkHttp 能用的 RequestBody。
     *
     * <p>OkHttp 只认 RequestBody，不认识相册的 Uri，所以要自己实现三个方法。
     * 好处是边读边发，不用把几 MB 的图片先整个读进内存。
     */
    private static class UriRequestBody extends RequestBody {

        private final ContentResolver resolver;
        private final Uri uri;
        private final long length;
        private final MediaType type;

        /**
         * @param resolver 用来打开 Uri 的 ContentResolver
         * @param uri      相册图片地址
         * @param length   文件字节数，-1 表示未知
         * @param type     媒体类型，写进每个 part 的 Content-Type
         */
        UriRequestBody(ContentResolver resolver, Uri uri, long length, MediaType type) {
            this.resolver = resolver;
            this.uri = uri;
            this.length = length;
            this.type = type;
        }

        /** 告诉 OkHttp 这段数据的 MIME 类型 */
        @Override
        public MediaType contentType() {
            return type;
        }

        /** 告诉 OkHttp 数据有多长；返回 -1 时改用 chunked 分块传输 */
        @Override
        public long contentLength() {
            return length;
        }

        /**
         * 真正的写数据动作：把 Uri 背后的输入流原样倒进请求体。
         *
         * @param sink OkHttp 提供的输出缓冲，写进去的字节最终会发到服务端
         * @throws IOException 打开或读取 Uri 失败
         */
        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            try (InputStream in = resolver.openInputStream(uri)) {
                if (in == null) {
                    throw new IOException("无法打开 " + uri);
                }
                // Okio.source 把 InputStream 包成 Okio 的 Source，writeAll 会循环搬运直到读完
                sink.writeAll(Okio.source(in));
            }
        }
    }
}
