package com.zzz.retrofitapplication.data.api;

import com.zzz.retrofitapplication.data.model.ResponseData;
import com.zzz.retrofitapplication.data.model.User;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit 的「接口即请求」写法：不写实现类，只在方法上标注解，
 * 运行时 Retrofit 用动态代理（Proxy.newProxyInstance）生成实现，把注解翻译成 OkHttp 的 Request。
 *
 * <p>对应服务端：D:\Code\Android\HttpClient（Spring Boot，接口都在 /api 下）
 * <p>对应页面：RetrofitActivity，一个方法对应页面上的一个按钮。
 *
 * <h3>注解速查</h3>
 * <ul>
 *   <li>{@code @GET}/{@code @POST}：请求方法，括号里是**相对路径**（不能以 / 开头，否则会丢掉 baseUrl 里的路径前缀）</li>
 *   <li>{@code @Query}：拼到 URL 查询串后面，?name=张三&amp;age=18，中文自动 URL 编码</li>
 *   <li>{@code @Body}：整个请求体交给你给的对象，由 Converter（这里是 Gson）序列化成 JSON</li>
 *   <li>{@code @FormUrlEncoded} + {@code @Field}：表单 application/x-www-form-urlencoded，一个 @Field 一个键</li>
 *   <li>{@code @FormUrlEncoded} + {@code @FieldMap}：同上，但键值来自一个 Map</li>
 *   <li>{@code @Multipart} + {@code @Part}：multipart/form-data，用来传文件</li>
 * </ul>
 */
public interface ApiService {

    // ==================== 1/2. GET：@Query 拼 URL ====================

    /**
     * GET /api/get?name=张三&amp;age=18
     *
     * <p>返回 {@code Call<ResponseData<User>>}：ResponseData 是统一外壳，User 是 data 的具体类型。
     * Retrofit 会把整个泛型交给 Gson，所以 {"code":200,"data":{"name":"...","age":18}} 能一层层还原。
     *
     * @param name 姓名，落到 URL 的 name 参数
     * @param age  年龄，会先被 String.valueOf 转成字符串再拼
     * @return 还没发出的请求；调用 execute()/enqueue() 才真正发出去
     */
    @GET("api/get")
    Call<ResponseData<User>> getUser(@Query("name") String name, @Query("age") int age);

    // ==================== 3. GET + @Path 路径变量 ====================

    /**
     * GET /api/path/服装 —— 值直接替换 URL 里的 {category} 占位符。
     *
     * <p>和 @Query 的区别只看 URL：@Query 是 ?k=v（查询串），@Path 是路径中的一段。
     * 方法签名里的占位符名字必须和 {@code @Path("category")} 的括号一致，否则运行时报
     * "URL ... does not contain {category}"。
     *
     * <p>默认会对值做 URL 编码：传 "服装" 实际发出去的是 %E6%9C%8D%E8%A3%85，服务端收到的仍是原文；
     * 如果值本身已经是编码过的（比如从别处拿到的 URL 片段），要写
     * {@code @Path(value = "category", encoded = true)} 关掉二次编码，否则会出现 %25E8 这种双重编码。
     *
     * @param category 路径变量，可以带中文
     * @return data 里回显服务端解码后的值
     */
    @GET("api/path/{category}")
    Call<ResponseData<Map<String, Object>>> getByPath(@Path("category") String category);

    // ==================== 4. GET + @Header 自定义请求头 ====================

    /**
     * GET /api/header —— 往请求头里塞自定义字段。
     *
     * <p>{@code @Header} 写在参数上表示"值由调用方决定"；写在方法上方的 {@code @Headers} 则是写死的常量头。
     * 头很多时可以用 {@code @HeaderMap Map<String, String>} 一次性传。
     *
     * <p>⚠️ 头名写错不会报错：服务端读不到就用默认值，比 @Query 写错更难发现，联调时先看日志里的请求头。
     *
     * @param token    自定义头 token
     * @param traceId  自定义头 X-Trace-Id，服务端 required=false，可以不传
     * @return data 里回显服务端读到的两个头
     */
    @GET("api/header")
    Call<ResponseData<Map<String, Object>>> getWithHeader(
            @Header("token") String token,
            @Header("X-Trace-Id") String traceId);

    // ==================== 5. POST + @Body 实体 ====================

    /**
     * POST /api/post，请求体是 JSON。
     *
     * <p>只写 {@code @Body User user} 就行，GsonConverterFactory 会把它变成
     * {"name":"王五","age":22}，并且自动带上 Content-Type: application/json。
     * 如果忘了 {@code addConverterFactory(GsonConverterFactory.create())}，这里会抛
     * "Unable to create converter for class ..."。
     *
     * <p>注意一个方法只能有一个 @Body 参数，且它不能和 @Field/@Part 混用。
     *
     * @param user 要提交的对象
     * @return 服务端会把请求体原样回显在 data 里
     */
    @POST("api/post")
    Call<ResponseData<User>> postUser(@Body User user);

    // ==================== 6. POST + @Body RequestBody（手动版） ====================

    /**
     * 同样是 POST JSON，但请求体由自己拼。
     *
     * <p>Retrofit 对 {@code RequestBody} 这种类型有特殊处理（BuiltInConverters）：
     * 不会再走 Gson，直接原样发出去。所以想自己控制 JSON、或者传流式数据时用它。
     *
     * @param body 已经拼好的请求体，媒体类型要自己指定，写错会得到 415 Unsupported Media Type
     * @return 回显
     */
    @POST("api/post")
    Call<ResponseData<User>> postRawBody(@Body RequestBody body);

    // ==================== 7. POST 表单：@Field ====================

    /**
     * POST /api/form，Content-Type 是 application/x-www-form-urlencoded。
     *
     * <p>⚠️ {@code @Field} 必须配 {@code @FormUrlEncoded}，少写会抛
     * "Form-encoded method must contain at least one @Field"（反过来也是）。
     * 表单和后端的区别看请求体：一个在 body 里（name=李四&amp;age=20），一个在 URL 上。
     *
     * @param name 字段 name
     * @param age  字段 age
     * @param city 字段 city
     * @return data 是服务端收到的全部表单键值，类型是 Map
     */
    @FormUrlEncoded
    @POST("api/form")
    Call<ResponseData<Map<String, String>>> postForm(
            @Field("name") String name,
            @Field("age") int age,
            @Field("city") String city);

    // ==================== 8. POST 表单：@FieldMap ====================

    /**
     * 表单字段数量不固定时用 {@code @FieldMap}：Map 里的每一对都会被摊成 name=value。
     *
     * @param fields 字段名 → 字段值，值为 null 的键会被跳过（不会发出去）
     * @return 回显
     */
    @FormUrlEncoded
    @POST("api/form")
    Call<ResponseData<Map<String, String>>> postFormMap(@FieldMap Map<String, String> fields);

    // ==================== 9. 文件上传：@Multipart ====================

    /**
     * POST /api/upload，Content-Type 是 multipart/form-data。
     *
     * <p>{@code @Part} 有两种形态：
     * <ul>
     *   <li>{@code MultipartBody.Part}：带文件名和数据，对应服务端的 MultipartFile；</li>
     *   <li>{@code @Part("desc") RequestBody}：纯文本字段，括号里的字符串就是字段名（必须写，否则服务端收不到）。</li>
     * </ul>
     * 字段名 "file" 要和 Spring 那边 {@code @RequestParam("file") MultipartFile file} 对上。
     *
     * @param file 图片部分，由 MultipartBody.Part.createFormData("file", 文件名, RequestBody) 生成
     * @param desc 附带的文本字段
     * @return data 里有文件名、大小、类型和落盘路径
     */
    @Multipart
    @POST("api/upload")
    Call<ResponseData<Map<String, Object>>> upload(
            @Part MultipartBody.Part file,
            @Part("desc") RequestBody desc);
}
