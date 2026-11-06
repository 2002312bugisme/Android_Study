package com.zzz.retrofitapplication.data.model;

import com.google.gson.annotations.SerializedName;
import com.zzz.retrofitapplication.net.response.IResponse;

/**
 * 统一响应外壳：把「业务码 + 提示语 + 数据」包一层，所有接口共用。
 *
 * <p>{@code <T>} 是泛型占位，真正解析时由 Retrofit 把接口方法的返回类型
 * （例如 {@code Call<ResponseData<User>>} 或 {@code Observable<ResponseData<User>>}）交给 Gson，
 * Gson 再把 T 还原成 User / Map。这也是为什么 Converter 不能把泛型写死。
 *
 * <p>实现 {@link IResponse} 之后，{@code ResponseTransformer} 就能把它当"任意接口的响应"处理，
 * 于是「判 code、取 data、抛异常」这三件事只需要写一遍。
 *
 * <p>服务端返回的 JSON 里还有 serverTime / serverThread 两个字段，本类没有对应字段，
 * Gson 默认会**忽略**多余的字段，不会报错。
 *
 * @param <T> 业务数据类型
 */
public class ResponseData<T> implements IResponse<T> {

    /** 业务码：200 表示业务成功，和 HTTP 状态码是两回事（HTTP 可能 200 但 code=400） */
    private int code;

    /**
     * 提示语。
     * <p>服务端字段名是 {@code msg}，这里叫 message，两者不一致时用 @SerializedName 建立映射；
     * 不加这个注解，Gson 按字段名匹配不到，message 会一直是 null（且不会报错，很难查）。
     */
    @SerializedName("msg")
    private String message;

    /** 业务数据，类型由接口方法决定 */
    private T data;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public T getData() {
        return data;
    }

    /**
     * 业务是否成功。
     *
     * @return code == 200 时返回 true
     */
    @Override
    public boolean isSuccess() {
        return code == IResponse.CODE_SUCCESS;
    }

    @Override
    public String toString() {
        return "code=" + code + ", message=" + message + ", data=" + data;
    }
}
