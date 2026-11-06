package com.zzz.retrofitapplication.net.exception;

import io.reactivex.rxjava3.functions.Consumer;

/**
 * 统一的错误回调：把 RxJava 的 {@code Consumer<Throwable>} 收窄成 {@code ApiException}。
 *
 * <p>用起来是这样的（注意第二个参数传的是子类实例，只需要实现 error）：
 * <pre>
 * .subscribe(
 *     data -&gt; { ... },                      // 成功
 *     new ErrorConsumer() {                // 失败：异常已经被翻译成 ApiException
 *         &#64;Override
 *         protected void error(ApiException e) {
 *             Log.e(TAG, e.getCode() + " " + e.getMessage());
 *         }
 *     });
 * </pre>
 *
 * <p>⚠️ 注意 import 的是 {@code io.reactivex.rxjava3.functions.Consumer}，
 * 不是 JDK 的 {@code java.util.function.Consumer}——后者 RxJava 不认，编译能过但传参处类型不匹配。
 */
public abstract class ErrorConsumer implements Consumer<Throwable> {

    /**
     * RxJava 在 onError 时回调这里。final 是刻意的：
     * 翻译逻辑不该被子类改掉，子类只实现 {@link #error(ApiException)}。
     *
     * @param throwable 上游抛出的任意异常
     */
    @Override
    public final void accept(Throwable throwable) {
        error(ApiException.wrap(throwable));
    }

    /**
     * 子类实现：处理已经统一过的业务异常。
     *
     * @param e 统一后的异常（code 可能是业务码，也可能是 CODE_NETWORK 之类的负数）
     */
    protected abstract void error(ApiException e);
}
