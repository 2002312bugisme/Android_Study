package com.zzz.retrofitapplication.net.exception;

import com.google.gson.JsonParseException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;

/**
 * 业务异常：把「HTTP 失败 / 业务码非 200 / 网络异常 / 解析异常」统一成一种类型。
 *
 * <p>为什么不直接把 Throwable 抛给页面：页面只关心"哪错了、给用户看什么"。
 * 如果不管，页面上就会出现 IOException、HttpException、JsonSyntaxException、NullPointerException
 * 各种分支，而且每个页面都要写一遍。统一成 ApiException 后，页面只有一个
 * {@code catch (ApiException e)}，打印 {@code e.getCode() + e.getMessage()} 就行。
 *
 * <p>继承 Exception（受检）而不是 RuntimeException：逼调用方处理，避免异常被静默吞掉。
 */
public class ApiException extends Exception {

    /** 网络层失败（连不上、超时、断网）——不是服务端给的码，所以用负数区分 */
    public static final int CODE_NETWORK = -1;
    /** 响应体解析失败（JSON 结构和实体对不上） */
    public static final int CODE_PARSE = -2;
    /** 其他没归类的异常 */
    public static final int CODE_UNKNOWN = -3;

    /** 业务码或自定义错误码 */
    private final int code;

    /**
     * @param code    错误码，成功时不会创建这个对象
     * @param message 给用户看的提示
     */
    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param code    错误码
     * @param message 提示
     * @param cause   原始异常，排查堆栈时用
     */
    public ApiException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 把任意异常翻译成 ApiException（幂等：本来就是这个类型就原样返回）。
     *
     * <p>判断顺序有讲究：SocketTimeoutException / UnknownHostException 都是 IOException 的子类，
     * 必须先判子类，否则永远进不到具体分支。
     *
     * @param throwable 上游抛出来的任意异常
     * @return 统一后的业务异常
     */
    public static ApiException wrap(Throwable throwable) {
        if (throwable instanceof ApiException) {
            return (ApiException) throwable;
        }
        if (throwable instanceof HttpException) {
            // 服务器回了但 HTTP 状态不是 2xx（404、500…），错误体在 e.response().errorBody()
            HttpException e = (HttpException) throwable;
            // Tomcat 常常不回 reason phrase（message() 是空串），别拼出 "HTTP 404 （）" 这种空括号
            String reason = e.message();
            String text = (reason == null || reason.isEmpty())
                    ? "HTTP " + e.code()
                    : "HTTP " + e.code() + "（" + reason + "）";
            return new ApiException(e.code(), text, throwable);
        }
        if (throwable instanceof UnknownHostException) {
            return new ApiException(CODE_NETWORK, "域名解析失败，检查 baseUrl 或网络", throwable);
        }
        if (throwable instanceof SocketTimeoutException) {
            return new ApiException(CODE_NETWORK, "请求超时，稍后重试", throwable);
        }
        if (throwable instanceof IOException) {
            // 服务端没启动、手机没网、明文 HTTP 被拦，都会落到这里
            return new ApiException(CODE_NETWORK, "网络异常：" + throwable.getMessage(), throwable);
        }
        if (throwable instanceof JsonParseException) {
            return new ApiException(CODE_PARSE, "数据解析失败：JSON 结构与实体不匹配", throwable);
        }
        return new ApiException(CODE_UNKNOWN, "未知错误：" + throwable.getMessage(), throwable);
    }

    @Override
    public String toString() {
        return "ApiException{code=" + code + ", message=" + getMessage() + '}';
    }
}
