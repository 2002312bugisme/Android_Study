package com.zzz.retrofitapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.retrofitapplication.R;
import com.zzz.retrofitapplication.data.api.ApiService;
import com.zzz.retrofitapplication.data.model.ResponseData;
import com.zzz.retrofitapplication.data.model.User;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 第二步：用 Hilt 组装网络模块。
 *
 * <p>和第一步（RetrofitActivity）的区别只有一处：
 * 那边在 onCreate 里手写 {@code new Retrofit.Builder()...}，
 * 这边只声明 {@code @Inject ApiService apiService;}，对象由 {@link com.zzz.retrofitapplication.di.NetWorkModule} 提供。
 *
 * <p>对应布局：res/layout/activity_net_work_module.xml
 * <p>注入链路：MyApplication(@HiltAndroidApp) → SingletonComponent → NetWorkModule → 本页字段
 *
 * <p>⚠️ {@code @AndroidEntryPoint} 不是普通注解：Hilt 的 Gradle 插件会用它生成
 * {@code Hilt_NetWorkModuleActivity} 父类，并把本类的父类改成它，注入才真正发生。
 * 少了插件（或清单里没配 Application）就会在崩溃时报
 * "Hilt Activity must be attached to an @HiltAndroidApp Application"。
 */
@AndroidEntryPoint
public class NetWorkModuleActivity extends AppCompatActivity {

    private static final String TAG = "NetWorkStep2";

    /**
     * 直接注入接口对象，页面里不再出现 Retrofit / OkHttpClient 的构造代码。
     * <p>字段注入（而不是构造注入）是 Activity 的无奈之处：Activity 由系统 new，
     * 构造函数不归我们管，所以 Hilt 只能在 super.onCreate() 之后把字段填上。
     */
    @Inject
    ApiService apiService;

    /** 这两个只用于"证明是同一个实例"，页面本身不需要它们 */
    @Inject
    Retrofit retrofit;
    @Inject
    OkHttpClient okHttpClient;

    private TextView tvResult;
    private ScrollView scrollPage;
    private boolean firstOutput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_net_work_module);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvResult = findViewById(R.id.tv_result);
        scrollPage = findViewById(R.id.main);

        // 注意：这里能安全使用 apiService，是因为 super.onCreate() 已经完成了注入。
        // 如果在 super.onCreate() 之前碰它，会是 null。
        findViewById(R.id.btn_call_get).setOnClickListener(v -> callGet());
        findViewById(R.id.btn_show_instances).setOnClickListener(v -> showInstances());
        findViewById(R.id.btn_go_rxjava).setOnClickListener(v ->
                startActivity(new Intent(this, NetWorkWithRxJavaActivity.class)));
        append("已注入 ApiService：" + apiService.getClass().getName());
    }

    // ==================== 1. 用注入的 ApiService 发请求 ====================

    /**
     * 和第一步的 GET 异步一模一样，唯一区别是 apiService 不用自己造。
     */
    private void callGet() {
        Call<ResponseData<User>> call = apiService.getUser("Hilt 注入", 28);
        append("→ " + call.request().url());
        long start = System.currentTimeMillis();

        call.enqueue(new Callback<ResponseData<User>>() {
            @Override
            public void onResponse(Call<ResponseData<User>> c, Response<ResponseData<User>> response) {
                long cost = System.currentTimeMillis() - start;
                ResponseData<User> body = response.body();
                String text = response.isSuccessful() && body != null
                        ? "code=" + body.getCode() + "　message=" + body.getMessage()
                        + "\ndata=" + body.getData()
                        : "errorBody=" + readErrorBody(response);
                append("【GET】HTTP " + response.code() + "　耗时 " + cost + "ms\n" + text);
            }

            @Override
            public void onFailure(Call<ResponseData<User>> c, Throwable t) {
                append("【GET】失败：" + t.getClass().getSimpleName() + ": " + t.getMessage());
            }
        });
    }

    // ==================== 2. 打印注入对象 ====================

    /**
     * 连续点两次，两次打印的 hash 相同就说明 @Singleton 生效了：
     * 整个进程只有一份 OkHttpClient / Retrofit / ApiService，注入到别的页面也是同一份。
     */
    private void showInstances() {
        append("ApiService   : " + apiService);
        append("Retrofit     : " + retrofit);
        append("OkHttpClient : " + okHttpClient);
        append("→ baseUrl = " + retrofit.baseUrl());
        Log.d(TAG, "ApiService=" + apiService + ", Retrofit=" + retrofit + ", Client=" + okHttpClient);
    }

    // ==================== 输出 ====================

    /**
     * 追加一行到输出区。Retrofit 的回调已经在主线程，这里仍然 runOnUiThread 兜底。
     *
     * @param text 要追加的内容
     */
    private void append(String text) {
        runOnUiThread(() -> {
            if (firstOutput) {
                tvResult.setText("");
                firstOutput = false;
            }
            tvResult.append(text + "\n\n");
            scrollPage.post(() -> scrollPage.fullScroll(View.FOCUS_DOWN));
        });
    }

    /**
     * 读 HTTP 4xx/5xx 时的响应体原文。
     *
     * @param response 失败的响应
     * @return 原文，读不到给个说明
     */
    private String readErrorBody(Response<?> response) {
        try {
            return response.errorBody() == null ? "(空)" : response.errorBody().string();
        } catch (IOException e) {
            return "(读取失败: " + e.getMessage() + ")";
        }
    }
}
