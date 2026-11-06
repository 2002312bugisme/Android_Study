package com.zzz.retrofitapplication.di;

import android.util.Log;

import com.zzz.retrofitapplication.data.api.ApiService;
import com.zzz.retrofitapplication.data.api.ApiWithRxJavaService;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 网络模块：把「OkHttpClient / Retrofit / ApiService 怎么造」这件事从页面里搬到 Hilt。
 *
 * <p>第一步是在 Activity 里手写 {@code new Retrofit.Builder()...}，第二步改成：
 * 页面只声明 {@code @Inject ApiService apiService;}，创建过程全部由这里提供。
 * 好处是 OkHttpClient 天然只有一份（连接池/线程池复用），换 baseUrl、加拦截器只改一处。
 *
 * <h3>两个注解各管什么</h3>
 * <ul>
 *   <li>{@code @Module}：标明"这个类负责提供依赖"；</li>
 *   <li>{@code @InstallIn(SingletonComponent.class)}：把模块装进哪个组件。
 *       SingletonComponent 是跟 Application 同生命周期的组件——注意老写法 {@code ApplicationComponent}
 *       从 Hilt 2.28 起已废弃并改名，现在只有 SingletonComponent。</li>
 * </ul>
 *
 * <h3>@Provides 的三个约定</h3>
 * <ul>
 *   <li>方法返回值 = 能提供什么；参数 = 需要什么（Hilt 会按类型自动找其他 @Provides）；</li>
 *   <li>加 {@code @Singleton} 表示全应用只建一次，不加则每次注入都新建；</li>
 *   <li>方法写成 {@code static}：模块本身无状态时，Hilt 官方推荐静态方法，省掉一次模块实例化。</li>
 * </ul>
 */
@Module
@InstallIn(SingletonComponent.class)
public class NetWorkModule {

    private static final String TAG = "NetWorkModule";

    /**
     * 服务端地址。⚠️ 必须以 / 结尾，Retrofit 的 baseUrl 是"目录"语义。
     * <p>模拟器用 10.0.2.2（指向宿主机）；真机改成电脑局域网 IP，或 adb reverse 后用 127.0.0.1。
     * <p>真实项目里这个值一般来自 BuildConfig（debug/release 两套），不会写死在代码里。
     */
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    /**
     * 日志拦截器：Retrofit 自己不打印请求，要看请求必须给底层 OkHttp 装拦截器。
     *
     * @return Level.BODY 的拦截器（连请求体、响应体一起打）
     */
    @Provides
    @Singleton
    public static HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    /**
     * OkHttp 客户端。单例的意义：它内部有连接池和线程池，反复 new 会让连接无法复用。
     *
     * @param loggingInterceptor 由上面的方法提供（Hilt 按类型自动传进来）
     * @return 配置好超时的客户端
     */
    @Provides
    @Singleton
    public static OkHttpClient provideOkHttpClient(HttpLoggingInterceptor loggingInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)      // 上传图片要放宽
                .build();
    }

    /**
     * Retrofit 实例。同样全局一份：它内部缓存了接口方法的注解解析结果，重复创建纯属浪费。
     *
     * <p>第三步新增了 {@code addCallAdapterFactory}：Converter 负责"数据长什么样"，
     * CallAdapter 负责"调用长什么样"——加上这一行，接口才能返回 Observable。
     * 没有它，写 {@code Observable<...>} 会在创建代理时抛
     * "Unable to create call adapter for io.reactivex.rxjava3.core.Observable"。
     *
     * @param okHttpClient 上面提供的客户端，不传就会用 Retrofit 内部的默认实例（没有日志）
     * @return 配好 Gson 转换器 + RxJava3 适配器的 Retrofit
     */
    @Provides
    @Singleton
    public static Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    /**
     * 接口实现。Retrofit.create() 返回的是动态代理对象，不是真的实现类。
     *
     * <p>把它也做成单例，注入到各个页面里的就是同一个对象——
     * 这也是 {@code @Inject ApiService apiService;} 能直接用的原因。
     *
     * @param retrofit 上面提供的 Retrofit
     * @return ApiService 的代理实例
     */
    @Provides
    @Singleton
    public static ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }

    /**
     * 第三步的接口实现。注意：它和 ApiService 共用同一个 Retrofit 实例——
     * Converter（Gson）和 CallAdapter（RxJava3）是可以并存的，返回 Call 的方法走默认适配器，
     * 返回 Observable 的方法走 RxJava3 适配器，互不影响。
     *
     * @param retrofit 同一个 Retrofit
     * @return ApiWithRxJavaService 的动态代理实例
     */
    @Provides
    @Singleton
    public static ApiWithRxJavaService provideApiWithRxJavaService(Retrofit retrofit) {
        return retrofit.create(ApiWithRxJavaService.class);
    }
}
