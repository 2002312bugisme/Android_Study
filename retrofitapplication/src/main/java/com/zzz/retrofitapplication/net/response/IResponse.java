package com.zzz.retrofitapplication.net.response;

/**
 * 服务端统一响应体的抽象。
 *
 * <p>为什么要这层接口：每个接口返回的 data 类型都不一样（User、Map、List…），
 * 但外层「code / message / data」是固定的。把这些共性抽成接口后，
 * {@link ResponseTransformer} 只面对 {@code IResponse<T>} 就能统一判成功、统一抛异常，
 * 不用每个接口都写一遍 {@code if (code == 200)}。
 *
 * <p>注意：方法名必须和 {@link com.zzz.retrofitapplication.data.model.ResponseData} 已有的
 * getter 对齐（getMessage / getCode 返回 int），否则 ResponseData 实现不了这个接口。
 *
 * @param <T> 业务数据类型
 */
public interface IResponse<T> {

    /** 业务成功的约定值。HTTP 200 只代表传输成功，业务是否成功要看这个 code */
    int CODE_SUCCESS = 200;

    /**
     * 业务数据。
     *
     * @return 服务端返回的 data，可能为 null
     */
    T getData();

    /**
     * 提示语（对应 JSON 里的 msg 字段）。
     *
     * @return 服务端给的提示
     */
    String getMessage();

    /**
     * 业务码。服务端给的是数字 200，所以用 int；
     * 用 String 会埋 "200" 与 "200 " 不相等这种隐雷。
     *
     * @return 业务码
     */
    int getCode();

    /**
     * 是否业务成功。
     *
     * @return code == 200 时为 true
     */
    boolean isSuccess();
}
