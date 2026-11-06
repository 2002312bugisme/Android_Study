package com.zzz.retrofitapplication.data.api;

import com.zzz.retrofitapplication.data.model.ResponseData;
import com.zzz.retrofitapplication.data.model.User;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * 第三步的接口定义：和 {@link ApiService} 的注解完全一样，**只把返回类型从 Call 换成 Observable**。
 *
 * <p>区别不在写法，在"谁推动请求"：
 * <ul>
 *   <li>{@code Call}：Retrofit 自己是个普通对象，你必须调 execute()/enqueue() 才会发；</li>
 *   <li>{@code Observable}：由 RxJava3CallAdapterFactory 把 Call 包成 Observable，
 *       <b>只有被 subscribe 时才发请求</b>（冷流），不订阅就永远不发，天然避免"忘了取消"。</li>
 * </ul>
 *
 * <p>返回类型写成 {@code Observable<ResponseData<T>>}（具体类）而不是 {@code Observable<IResponse<T>>}：
 * Gson 需要知道反序列化成哪个类，接口是造不出实例的。
 * 到了页面再用 {@code ResponseTransformer} 把外壳剥掉。
 */
public interface ApiWithRxJavaService {

    /**
     * GET /api/get?name=xx&amp;age=18
     *
     * @param name 姓名
     * @param age  年龄
     * @return 订阅后才发请求的冷流
     */
    @GET("api/get")
    Observable<ResponseData<User>> getUser(@Query("name") String name, @Query("age") int age);

    /**
     * POST /api/post，@Body 实体由 Gson 序列化成 JSON。
     *
     * @param user 请求体
     * @return 冷流
     */
    @POST("api/post")
    Observable<ResponseData<User>> postUser(@Body User user);

    /**
     * POST /api/form，表单提交。
     *
     * @param fields 表单键值
     * @return 冷流
     */
    @FormUrlEncoded
    @POST("api/form")
    Observable<ResponseData<Map<String, String>>> postForm(@FieldMap Map<String, String> fields);

    /**
     * 故意请求一个不存在的路径，用来演示"HTTP 404 → ApiException"这条异常链路。
     *
     * @return 必定失败的冷流
     */
    @GET("api/not_exists")
    Observable<ResponseData<User>> notExists();
}
