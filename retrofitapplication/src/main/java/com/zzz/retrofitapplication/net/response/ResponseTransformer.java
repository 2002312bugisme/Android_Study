package com.zzz.retrofitapplication.net.response;

import com.zzz.retrofitapplication.net.exception.ApiException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 响应转换器：把「Observable&lt;IResponse&lt;T&gt;&gt;」变成「Observable&lt;T&gt;」，
 * 顺手完成切线程、判业务码、异常归一这三件每个接口都要做的事。
 *
 * <h3>为什么实现 ObservableTransformer</h3>
 * <p>RxJava 的 {@code compose(transformer)} 是"对整条流做变换"的算子，它的参数类型就是
 * {@code ObservableTransformer<上游类型, 下游类型>}。这个接口只有一个方法：
 * <pre>
 * ObservableSource&lt;Downstream&gt; apply(Observable&lt;Upstream&gt; upstream);
 * </pre>
 * 也就是"给你一条流，还我另一条流"。实现它，只是为了让 {@code .compose(...)} 能接受我们这套规则；
 * 你完全可以不实现接口，直接在页面里连着写 map/flatMap/onErrorResumeNext，效果一样——
 * 那样做的代价就是**每个接口都要重复抄一遍**，这正是你说的"每次都重复地写"。
 *
 * <h3>为什么不直接用 map</h3>
 * <p>{@code map} 只能"一个元素换一个元素"，它改变不了流的结构：不能换线程（线程是订阅时决定的，
 * 属于流级别）、也不能把上游的 onError 统一改写成另一种异常。{@code compose} 拿到的是整条流，
 * 所以这些"流级别"的事只能在这里做。
 *
 * <h3>内部四步（顺序不能乱）</h3>
 * <ol>
 *   <li>{@code subscribeOn(io)}：整条流在 IO 线程开始订阅 → 网络请求在子线程发出；</li>
 *   <li>{@code flatMap}：判业务码。成功就把 data 剥出来往下游发，失败就抛 ApiException；</li>
 *   <li>{@code onErrorResumeNext}：把 IOException / HttpException / 解析异常统一翻译成 ApiException
 *       （否则页面要面对 4 种异常类型）；</li>
 *   <li>{@code observeOn(main)}：把回调切回主线程，页面里可以直接改控件。</li>
 * </ol>
 *
 * <p>⚠️ RxJava 不允许 onNext(null)：{@code Observable.just(null)} 会直接抛 NPE，
 * 所以 data 为 null 时要发 {@code empty()}（下游 onNext 不触发，直接 onComplete）。
 *
 * @param <T> 剥壳后的业务数据类型，例如 User
 */
public class ResponseTransformer<T> implements ObservableTransformer<IResponse<T>, T> {

    /** 私有构造：统一用 obtain() 拿实例，避免外部 new 出一堆无状态对象 */
    private ResponseTransformer() {
    }

    /**
     * 工厂方法。带泛型的类用静态工厂比 new 更顺，调用处能省掉一次类型声明：
     * {@code .compose(ResponseTransformer.obtain())}
     *
     * @param <T> 业务数据类型
     * @return 转换器实例（无状态，可以放心复用）
     */
    public static <T> ResponseTransformer<T> obtain() {
        return new ResponseTransformer<>();
    }

    /**
     * 真正被 compose 调用的地方：入参是「Retrofit 返回的带壳流」，出参是「剥壳后的业务流」。
     *
     * @param upstream 上游：Observable&lt;IResponse&lt;T&gt;&gt;
     * @return 下游：Observable&lt;T&gt;
     */
    @Override
    public ObservableSource<T> apply(Observable<IResponse<T>> upstream) {
        return upstream
                // ① 订阅发生在 IO 线程 = Retrofit 的请求在子线程发出（主线程发请求会 NetworkOnMainThreadException）
                .subscribeOn(Schedulers.io())
                // ② 业务码判断：这里拿到的是"已经解析好的对象"，不是 JSON 字符串
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        T data = response.getData();
                        // RxJava 3 不允许 null 元素，data 缺失时发 empty 而不是 just(null)
                        return data == null ? Observable.<T>empty() : Observable.just(data);
                    }
                    // 业务失败：交给下游的 ErrorConsumer
                    return Observable.<T>error(
                            new ApiException(response.getCode(), response.getMessage()));
                })
                // ③ 网络层异常归一：原始的 IOException/HttpException 全部变成 ApiException
                .onErrorResumeNext(throwable ->
                        Observable.<T>error(ApiException.wrap(throwable)))
                // ④ 之后的回调都在主线程，页面可以放心碰控件
                .observeOn(AndroidSchedulers.mainThread());
    }
}
