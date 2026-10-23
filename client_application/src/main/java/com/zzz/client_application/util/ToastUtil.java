package com.zzz.client_application.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast 工具类：把"弹出短提示"这个高频操作封装成一行静态方法
 *
 * <p>演示要点：
 * 1. 用 {@code static} 方法而不是 new 对象——工具类不需要状态，静态调用最省事；
 * 2. 内部固定用 {@link Toast#LENGTH_SHORT}，避免每个调用点各写一遍；
 * 3. 参数要 Context 而不是 Activity，这样在工具类、Adapter、Service 里都能复用。
 *
 * <p>涉及 API：{@link Toast#makeText(Context, CharSequence, int)}、{@link Toast#show()}
 */
public class ToastUtil {

    /**
     * 弹一个短 Toast
     *
     * @param ctx  上下文，通常是 Activity；Toast 内部只会拿它取应用资源，不会长期持有
     * @param desc 要显示的文字
     */
    public static void show(Context ctx, String desc) {
        // makeText 创建 Toast 对象，show() 才真正显示；不调用 show() 是不会弹出的
        Toast.makeText(ctx, desc, Toast.LENGTH_SHORT).show();
    }
}
