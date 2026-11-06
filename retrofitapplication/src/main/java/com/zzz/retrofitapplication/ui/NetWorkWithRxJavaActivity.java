package com.zzz.retrofitapplication.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.retrofitapplication.R;
import com.zzz.retrofitapplication.data.api.ApiWithRxJavaService;
import com.zzz.retrofitapplication.data.model.ResponseData;
import com.zzz.retrofitapplication.data.model.User;
import com.zzz.retrofitapplication.net.exception.ApiException;
import com.zzz.retrofitapplication.net.exception.ErrorConsumer;
import com.zzz.retrofitapplication.net.response.ResponseTransformer;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * 第三步：Hilt + Retrofit + RxJava 三者合起来用。
 *
 * <p>对应布局：res/layout/activity_net_work_with_rx_java.xml
 * <p>对应接口：{@link ApiWithRxJavaService}（返回 Observable 而不是 Call）
 * <p>统一处理：{@link ResponseTransformer}（剥壳 + 判码 + 异常归一 + 切线程）、
 * {@link ErrorConsumer}（错误回调只面对 ApiException）
 *
 * <p>和第二步的区别只在"调用方式"，网络层的东西一点没变：
 * 还是那个 OkHttpClient、那个 Retrofit、那个 ApiService 的注解；
 * 变的只是"什么时候发请求"——Call 要你手动 execute，Observable 等你 subscribe。
 */
@AndroidEntryPoint
public class NetWorkWithRxJavaActivity extends AppCompatActivity {

    private static final String TAG = "NetWorkRxJava";

    /** Hilt 注入接口，页面里不需要知道 Retrofit 怎么建的 */
    @Inject
    ApiWithRxJavaService api;

    /**
     * 订阅的"容器"：每 subscribe 一次就往里塞一个 Disposable。
     * 页面销毁时全部 dispose，否则请求回来时页面已经没了 → 内存泄漏，甚至回调里碰空控件崩溃。
     * （compose 里的 observeOn(main) 让回调在主线程，如果没有 dispose，旋转屏幕就会重复回调）
     */
    private final CompositeDisposable disposables = new CompositeDisposable();

    private TextView tvResult;
    private ScrollView scrollPage;
    private boolean firstOutput = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_net_work_with_rx_java);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvResult = findViewById(R.id.tv_result);
        scrollPage = findViewById(R.id.main);

        // 每个按钮只负责"拿到那条流"，subscribe 统一走 call()
        findViewById(R.id.btn_rx_get).setOnClickListener(v ->
                call("GET", api.getUser("RxJava", 30)));

        findViewById(R.id.btn_rx_post).setOnClickListener(v ->
                call("POST", api.postUser(new User("王五", 22))));

        findViewById(R.id.btn_rx_form).setOnClickListener(v -> {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("name", "李四");
            fields.put("age", "20");
            fields.put("city", "深圳");
            call("表单", api.postForm(fields));
        });

        // 故意打 404，看统一异常怎么兜住（不写这个按钮，你就看不到 ErrorConsumer 在工作）
        findViewById(R.id.btn_rx_error).setOnClickListener(v ->
                call("404", api.notExists()));

        append("已注入 ApiWithRxJavaService：" + api.getClass().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 页面没了就别再往回调里送数据，同时掐断还在飞的请求
        disposables.clear();
    }

    // ==================== 统一的订阅入口 ====================

    /**
     * 把「订阅 + 成功回调 + 失败回调 + 保存 Disposable」这套重复代码收在一处。
     *
     * <p>这就是你截图里那段代码的通用版：
     * <pre>
     * api.xxx()
     *    .compose(ResponseTransformer.obtain())   // Observable&lt;ResponseData&lt;T&gt;&gt; → Observable&lt;T&gt;
     *    .subscribe(data -&gt; {...}, new ErrorConsumer() {...});
     * </pre>
     *
     * @param action 功能名，只用于输出
     * @param source Retrofit 返回的原始流（带 code/message/data 外壳）
     * @param <T>    剥壳后的业务类型
     */
    private <T> void call(String action, Observable<ResponseData<T>> source) {
        append("→ " + action + "（订阅后才会发请求）");
        long start = System.currentTimeMillis();

        Disposable disposable = source
                // 关键一步：所有接口都要的那套规则，写在 ResponseTransformer 里，页面只写一次 compose
                .compose(ResponseTransformer.<T>obtain())
                // 第一个参数：成功。到这里 data 已经是 User / Map 本身，不再是 ResponseData
                .subscribe(
                        data -> append("【" + action + "】成功　耗时 "
                                + (System.currentTimeMillis() - start) + "ms\n" + data),
                        // 第二个参数：失败。异常已被翻译成 ApiException，不用再判类型
                        new ErrorConsumer() {
                            @Override
                            protected void error(ApiException e) {
                                append("【" + action + "】失败　code=" + e.getCode()
                                        + "\n" + e.getMessage());
                            }
                        });

        // 不存起来就取消不了；页面一销毁 clear() 会连请求一起取消
        disposables.add(disposable);
    }

    // ==================== 输出 ====================

    /**
     * 追加一行到输出区。observeOn(main) 之后回调已在主线程，这里仍 runOnUiThread 兜底。
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
}
