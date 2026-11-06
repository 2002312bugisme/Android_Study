package com.zzz.retrofitapplication.ui;

import android.content.ContentResolver;
import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.retrofitapplication.R;
import com.zzz.retrofitapplication.data.api.ApiService;
import com.zzz.retrofitapplication.data.model.ResponseData;
import com.zzz.retrofitapplication.data.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 用法演示页。
 *
 * <p>对应布局：res/layout/activity_retrofit.xml
 * <p>对应接口定义：retrofitapplication/.../retrofit/ApiService.java
 * <p>对应服务端：D:\Code\Android\HttpClient（Spring Boot，接口都在 /api 下）
 *
 * <p>九个按钮各演示一件事：
 * <ol>
 *   <li>GET 同步 —— {@code Call.execute()}，阻塞，必须放子线程</li>
 *   <li>GET 异步 —— {@code Call.enqueue()}，回调由 Retrofit 切回主线程（实测线程名 main）</li>
 *   <li>GET 路径变量 —— {@code @Path}，值替换 URL 里的一段（中文自动编码）</li>
 *   <li>GET 自定义头 —— {@code @Header}，把参数塞进请求头</li>
 *   <li>POST JSON —— {@code @Body} 传实体，靠 GsonConverterFactory 序列化</li>
 *   <li>POST 原始请求体 —— {@code @Body} 直接传 RequestBody，绕过 Gson</li>
 *   <li>表单 —— {@code @FormUrlEncoded + @Field}</li>
 *   <li>表单 —— {@code @FormUrlEncoded + @FieldMap}</li>
 *   <li>上传图片 —— {@code @Multipart + @Part}，文件流边读边发</li>
 * </ol>
 *
 * <p>涉及 API：Retrofit / Retrofit.Builder / GsonConverterFactory / ApiService /
 * Call.execute / Call.enqueue / Response / ResponseData。
 */
public class RetrofitActivity extends AppCompatActivity {

    private static final String TAG = "RetrofitDemo";

    /**
     * 服务端地址默认值，注意**结尾必须带 /**：
     * Retrofit 要求 baseUrl 是"目录"形式，否则 build() 时直接抛
     * IllegalArgumentException: baseUrl must end in /。
     * <p>模拟器用 10.0.2.2 指向宿主机；真机改电脑局域网 IP，或 adb reverse 后用 127.0.0.1。
     */
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/";

    /** JSON 媒体类型：只在「手动传 RequestBody」和上传的文本字段里用到，@Body 传实体时由 Gson 自己设 */
    private static final MediaType JSON_TYPE = Objects.requireNonNull(
            MediaType.parse("application/json; charset=utf-8"));

    /**
     * 同步请求线程池。execute() 是阻塞调用，主线程调用会抛 NetworkOnMainThreadException，
     * 所以只能丢到子线程：execute() 本身不切线程（和 enqueue 把回调 post 回主线程正好相反）。
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 系统相册选择器（SAF），拿到的 Uri 不需要任何存储权限。
     * 必须先注册再使用，所以声明成字段用 registerForActivityResult 初始化。
     */
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::uploadImage);

    private EditText etBaseUrl;
    private TextView tvResult;
    private ScrollView scrollPage;
    /** 输出区还是布局里的占位文案，第一次追加时要先清掉 */
    private boolean firstOutput = true;

    private OkHttpClient client;
    /** Retrofit 创建的接口实现（动态代理对象），全页面复用一个 */
    private ApiService apiService;
    /** 当前 apiService 是用哪个 baseUrl 建的；地址一变就得重建 */
    private String builtBaseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_retrofit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etBaseUrl = findViewById(R.id.et_base_url);
        tvResult = findViewById(R.id.tv_result);
        scrollPage = findViewById(R.id.main);
        etBaseUrl.setText(DEFAULT_BASE_URL);

        // 日志拦截器：Retrofit 自己不打日志，它只是把注解翻译成 OkHttp 的 Request，
        // 所以要"看见请求"必须给底层的 OkHttpClient 装拦截器，再用 .client(...) 塞给 Retrofit。
        // Level.BODY 会连请求体、响应体一起打印，调试期够用了。
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)      // 上传图片要放宽
                .build();

        findViewById(R.id.btn_get_sync).setOnClickListener(v -> getSync());
        findViewById(R.id.btn_get_async).setOnClickListener(v -> getAsync());
        findViewById(R.id.btn_get_path).setOnClickListener(v -> getByPath());
        findViewById(R.id.btn_get_header).setOnClickListener(v -> getWithHeader());
        findViewById(R.id.btn_post_json).setOnClickListener(v -> postJson());
        findViewById(R.id.btn_post_body).setOnClickListener(v -> postRawBody());
        findViewById(R.id.btn_form_field).setOnClickListener(v -> postForm());
        findViewById(R.id.btn_form_map).setOnClickListener(v -> postFormMap());
        findViewById(R.id.btn_upload).setOnClickListener(v -> pickImage.launch("image/*"));
        // 第二步页面 exported=false，只能从这里点进去
        findViewById(R.id.btn_go_network).setOnClickListener(v ->
                startActivity(new Intent(this, NetWorkModuleActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // ==================== Retrofit 实例 ====================

    /**
     * 取接口实现，懒创建。
     *
     * <p>Retrofit 是"重量级"对象（内部缓存方法注解解析结果），应该复用而不是每次点击都 new；
     * 但 baseUrl 在 build() 时就定死了，输入框里的地址变了只能重建，所以这里按地址判断。
     *
     * @return ApiService 的动态代理实现
     */
    private ApiService api() {
        String base = baseUrl();
        if (apiService == null || !base.equals(builtBaseUrl)) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(base)                                  // 必须以 / 结尾
                    .client(client)                                 // 不写会用默认 OkHttpClient，也就没有日志
                    .addConverterFactory(GsonConverterFactory.create())  // 负责 JSON ⇄ Java 对象
                    .build();
            // create() 不是反射 new 实例，而是生成 Proxy：调用接口方法 → 解析注解 → 发 HTTP 请求
            apiService = retrofit.create(ApiService.class);
            builtBaseUrl = base;
            Log.d(TAG, "重建 Retrofit，baseUrl = " + base);
        }
        return apiService;
    }

    /**
     * 读输入框里的地址，补上结尾的 /。
     *
     * @return 形如 http://10.0.2.2:8080/ 的地址
     */
    private String baseUrl() {
        String base = etBaseUrl.getText().toString().trim();
        return base.endsWith("/") ? base : base + "/";
    }

    // ==================== 1. GET 同步 ====================

    /**
     * GET 同步：{@code execute()} 阻塞到拿到响应，所以整段丢进 executor。
     * 注意 Call 对象可以提前创建（此时还没发请求），真正发出去的是 execute()。
     */
    private void getSync() {
        Call<ResponseData<User>> call = api().getUser("张三", 18);
        append("→ GET 同步 " + call.request().url());

        executor.execute(() -> {
            long start = System.currentTimeMillis();
            try {
                Response<ResponseData<User>> response = call.execute();
                showResult("1. GET 同步", response, System.currentTimeMillis() - start);
            } catch (IOException e) {
                showError("1. GET 同步", e);
            }
        });
    }

    // ==================== 2. GET 异步 ====================

    /**
     * GET 异步：{@code enqueue()} 立刻返回，结果走 Callback。
     *
     * <p>⚠️ 线程和 OkHttp 原生 enqueue 不一样：Retrofit 在 Android 平台内置了
     * MainThreadExecutor（Platform.Android），回调会被 post 到主线程执行，
     * 所以这里能直接碰控件。实测输出里的「线程 main」就是证据。
     * 换成纯 OkHttp 的 {@code call.enqueue()} 则是在 OkHttp 的工作线程里回调，
     * 那时就必须自己 runOnUiThread。
     */
    private void getAsync() {
        enqueue("2. GET 异步", api().getUser("李四", 20));
    }

    // ==================== 3. GET 路径变量（@Path） ====================

    /**
     * @Path 把值直接塞进 URL 路径：{@code api/path/{category}} → api/path/服装。
     *
     * <p>中文不用自己编码，Retrofit 默认会 encode（服务端收到的是解码后的原文）。
     * 打印出来的 URL 里能看到 %E6%9C%8D%E8%A3%85，就是编码后的「服装」。
     */
    private void getByPath() {
        enqueue("3. GET @Path", api().getByPath("服装"));
    }

    // ==================== 4. GET 自定义请求头（@Header） ====================

    /**
     * @Header 把参数加到请求头里，和 URL、请求体都无关，常用来传 token、设备号、追踪 ID。
     *
     * <p>日志拦截器会把请求头整段打出来，正好对照着看这两个头有没有加上。
     */
    private void getWithHeader() {
        enqueue("4. GET @Header", api().getWithHeader(
                "token-abc-123",                          // 对应 @Header("token")
                "trace-" + System.currentTimeMillis()));   // 对应 @Header("X-Trace-Id")
    }

    // ==================== 5. POST JSON（@Body 实体） ====================

    /**
     * 只把一个 User 对象交给 @Body，序列化成 JSON、设置 Content-Type 都由 Gson 转换器包办。
     */
    private void postJson() {
        User user = new User("王五", 22);
        enqueue("5. POST JSON", api().postUser(user));
        // enqueue 里已经先打了 URL，这里补一行说明请求体长什么样
        append("　 请求体（Gson 生成）：{\"name\":\"" + user.getName() + "\",\"age\":" + user.getAge() + "}");
    }

    // ==================== 6. POST 原始 RequestBody ====================

    /**
     * 手动拼 JSON 字符串再交给 @Body。Retrofit 看到参数是 RequestBody 就不再走 Gson，
     * 原样发出去 —— 想完全控制请求体（或传流式数据）时用这种方式。
     */
    private void postRawBody() {
        String json = "{\"name\":\"RequestBody\",\"age\":30}";
        RequestBody body = RequestBody.create(json, JSON_TYPE);     // Java 里是「内容在前，媒体类型在后」
        enqueue("6. POST 原始 RequestBody", api().postRawBody(body));
        append("　 请求体（手写）：" + json);
    }

    // ==================== 7. POST 表单（@Field） ====================

    /**
     * 表单提交，一个 @Field 对应一个键，Retrofit 拼成 name=李四&amp;age=20 放进请求体。
     */
    private void postForm() {
        enqueue("7. POST 表单 @Field", api().postForm("李四", 20, "深圳"));
    }

    // ==================== 8. POST 表单（@FieldMap） ====================

    /**
     * 字段数量不固定时用 Map 传，Map 有几对就拼几个字段。
     */
    private void postFormMap() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", "赵六");
        fields.put("age", "26");
        fields.put("city", "杭州");
        enqueue("8. POST 表单 @FieldMap", api().postFormMap(fields));
        append("　 表单字段：" + fields);
    }

    // ==================== 9. 上传图片（@Multipart） ====================

    /**
     * 相册选完图后上传。
     *
     * <p>Retrofit 传文件要靠 {@link MultipartBody.Part}：
     * {@code createFormData(字段名, 文件名, RequestBody)} 三个参数分别对应
     * 服务端 @RequestParam 的名字、保存时的文件名、文件内容。
     *
     * @param uri 相册回调给的 content:// 地址；用户取消时是 null
     */
    private void uploadImage(Uri uri) {
        if (uri == null) {
            return;
        }

        String fileName = queryDisplayName(uri);
        long fileSize = queryFileSize(uri);                           // 字节数，未知时 -1
        String mimeType = getContentResolver().getType(uri);          // 例如 image/jpeg，可能为 null
        MediaType mediaType = MediaType.parse(mimeType == null ? "application/octet-stream" : mimeType);

        // 文件部分：字段名 file 要和服务端 @RequestParam("file") 对上。
        // 第三个参数是 RequestBody —— Retrofit 不认识相册的 Uri，得自己包一层：
        // UriRequestBody 在 writeTo() 里把输入流倒进 socket，几 MB 的图也不会进内存。
        MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                "file", fileName, new UriRequestBody(getContentResolver(), uri, fileSize, mediaType));
        // 文本部分：@Part("desc") 括号里的就是字段名，必须写，否则服务端收不到
        RequestBody descPart = RequestBody.create("来自相册的图片", MediaType.parse("text/plain"));

        enqueue("9. 上传图片", api().upload(filePart, descPart));
        append("　 待上传：" + fileName + "（" + fileSize + " 字节，" + mediaType + "，流式读取）");
    }

    /**
     * 查相册文件的字节数。
     *
     * @param uri content:// 地址
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

    /**
     * 查相册文件的显示名（ContentResolver 标准查法）。
     *
     * @param uri content:// 地址
     * @return 文件名；查不到时给个带时间戳的兜底名
     */
    private String queryDisplayName(Uri uri) {
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

    // ==================== 异步请求的统一入口 ====================

    /**
     * 把「发请求 + 处理回调」这段重复代码收在一处，各按钮只管"怎么构造 Call"。
     *
     * <p>回调已经在主线程（Retrofit 的 MainThreadExecutor 干的），所以 showResult
     * 里可以放心读控件；append() 里那句 runOnUiThread 只是兜底，同步请求的调用链会用到。
     *
     * @param action 功能名，用于输出
     * @param call   已经由 ApiService 创建好的请求
     * @param <T>    data 的具体类型，和接口方法返回类型保持一致
     */
    private <T> void enqueue(String action, Call<ResponseData<T>> call) {
        append("→ " + action + " " + call.request().url());
        // 计时起点必须在 enqueue 之前取
        long start = System.currentTimeMillis();
        call.enqueue(new Callback<ResponseData<T>>() {
            /** 只要服务器回了响应就走这里，4xx/5xx 也算"成功"，要看 code 判断 */
            @Override
            public void onResponse(Call<ResponseData<T>> c, Response<ResponseData<T>> response) {
                showResult(action, response, System.currentTimeMillis() - start);
            }

            /** 请求根本没跑完：连不上、超时、JSON 和实体对不上导致解析失败等 */
            @Override
            public void onFailure(Call<ResponseData<T>> c, Throwable t) {
                showError(action, t);
            }
        });
    }

    // ==================== 输出 ====================

    /**
     * 统一的结果输出：HTTP 状态码、耗时、线程 + 业务字段。
     * 内部会切主线程，所以可以在回调线程里直接调。
     *
     * @param action 功能名
     * @param response Retrofit 包装过的响应
     * @param costMs 发请求到解析完的耗时
     * @param <T>    data 的具体类型
     */
    private <T> void showResult(String action, Response<ResponseData<T>> response, long costMs) {
        String thread = Thread.currentThread().getName();
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(action).append("】HTTP ").append(response.code())
                .append("　耗时 ").append(costMs).append("ms　线程 ").append(thread).append("\n");

        ResponseData<T> body = response.body();
        if (!response.isSuccessful() || body == null) {
            // HTTP 层就失败了（404/500），此时 body() 是 null，原文在 errorBody() 里
            sb.append("errorBody: ").append(readErrorBody(response));
        } else {
            // @SerializedName 生效的话 message 才有值；data 的 toString 由实体自己决定
            sb.append("code=").append(body.getCode())
                    .append("　message=").append(body.getMessage())
                    .append("\ndata=").append(body.getData());
        }
        Log.d(TAG, sb.toString());
        append(sb.toString());
    }

    /**
     * 统一的失败输出。
     *
     * @param action 功能名
     * @param t      异常。常见原因：地址写错、服务端没启动、明文 HTTP 被拦、JSON 结构和实体不符
     */
    private void showError(String action, Throwable t) {
        Log.e(TAG, action + " 失败", t);
        String thread = Thread.currentThread().getName();
        append("【" + action + "】失败　线程 " + thread + "\n"
                + t.getClass().getSimpleName() + ": " + t.getMessage());
    }

    /**
     * 读取错误响应体原文（HTTP 4xx/5xx 时才有内容）。
     *
     * @param response 失败的响应
     * @return 原文；读不出来给一句说明
     */
    private String readErrorBody(Response<?> response) {
        try {
            return response.errorBody() == null ? "(空)" : response.errorBody().string();
        } catch (IOException e) {
            return "(读取失败: " + e.getMessage() + ")";
        }
    }

    /**
     * 追加一行结果到输出区，内部切主线程，任何线程都能调。
     *
     * @param text 要追加的内容
     */
    private void append(String text) {
        runOnUiThread(() -> {
            if (firstOutput) {
                // 清掉 XML 里的占位提示，否则第一条结果会和它粘在一起
                tvResult.setText("");
                firstOutput = false;
            }
            tvResult.append(text + "\n\n");
            // 等这一帧布局完成再滚到底，直接调用会滚到旧位置
            scrollPage.post(() -> scrollPage.fullScroll(View.FOCUS_DOWN));
        });
    }

    /**
     * 把相册的 content:// Uri 包装成 OkHttp 能用的 RequestBody。
     *
     * <p>Retrofit 只认 RequestBody，不认识 Uri，所以要自己实现三个方法，
     * 由 {@link MultipartBody.Part#createFormData} 当成文件内容塞进 multipart 表单。
     *
     * <p>好处是 {@code writeTo()} 里边读边写：几 MB 的图片不会整块进内存，
     * 只有 OkHttp 的 8KB 缓冲在流转。
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
         * @param type     媒体类型，会写进这个 part 的 Content-Type
         */
        UriRequestBody(ContentResolver resolver, Uri uri, long length, MediaType type) {
            this.resolver = resolver;
            this.uri = uri;
            this.length = length;
            this.type = type;
        }

        /** 这段数据的 MIME 类型 */
        @Override
        public MediaType contentType() {
            return type;
        }

        /** 数据长度；返回 -1 时 OkHttp 改用 chunked 分块传输 */
        @Override
        public long contentLength() {
            return length;
        }

        /**
         * 真正的写数据动作：把 Uri 背后的输入流原样倒进请求体。
         *
         * @param sink OkHttp 提供的输出缓冲
         * @throws IOException 打开或读取 Uri 失败
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            try (InputStream in = resolver.openInputStream(uri)) {
                if (in == null) {
                    throw new IOException("无法打开 " + uri);
                }
                // Okio.source 把 InputStream 包成 Source，writeAll 循环搬运直到读完
                sink.writeAll(Okio.source(in));
            }
        }
    }
}
