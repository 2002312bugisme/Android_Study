package com.zzz.datasharingapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 简易封装（只处理 boolean，够商城 demo 的"是否首次打开"用）
 *
 * <p>演示要点：
 * 1. {@code getSharedPreferences("shopping", MODE_PRIVATE)} 会在
 *    {@code /data/data/<包名>/shared_prefs/shopping.xml} 下生成一个 XML 文件，键值对以文本形式存放；
 * 2. {@code MODE_PRIVATE} 表示只有本应用可读写（其他几种模式早已废弃，一律用这个）；
 * 3. 注意单例是"懒汉式且非线程安全"的：只在主线程（Application.onCreate / 页面点击）里用没问题，
 *    多线程并发首次调用时可能创建出两个实例——本类只做演示，未加 synchronized。
 *
 * <p>涉及 API：{@link Context#getSharedPreferences(String, int)}、
 * {@link SharedPreferences.Editor#putBoolean(String, boolean)}、
 * {@link SharedPreferences.Editor#commit()}
 */
public class SharedUtil {

    // 单例对象；null 表示还没初始化。注意这里注释的是"单例"，不是"是否首次打开"
    private static SharedUtil sharedUtil = null;

    // 真正的存储对象，第一次 getInstance 时按传入的 Context 拿到
    private SharedPreferences sharedPreferences;

    /**
     * 获取单例（懒汉式）
     *
     * @param context 任意 Context；只在第一次调用时用于定位 prefs 文件，后续调用传入的 context 会被忽略
     * @return 全进程共用的 SharedUtil
     */
    public static SharedUtil getInstance(Context context) {
        if (sharedUtil == null) {                       // 第一次调用才创建
            sharedUtil = new SharedUtil();
            // 文件名 "shopping" → 落盘为 shared_prefs/shopping.xml
            sharedUtil.sharedPreferences = context.getSharedPreferences("shopping", Context.MODE_PRIVATE);
        }
        return sharedUtil;
    }

    /**
     * 写入一个 boolean 值
     *
     * @param key   键名（写进 XML 后会成为 {@code <boolean name="key" value="..."/>}）
     * @param value 要保存的值
     */
    public void writeBoolean(String key, boolean value) {
        // 1. 拿到编辑器（所有修改都写在 Editor 里）
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // 2. 放入键值对（此时还没落盘）
        editor.putBoolean(key, value);
        // 3. commit() 是同步写入磁盘并返回是否成功；apply() 是异步写内存 + 后台落盘。
        //    这里的值是"首次初始化完成"这种不能丢的标记，所以用 commit()
        editor.commit();
    }

    /**
     * 读取一个 boolean 值
     *
     * @param key          键名
     * @param defaultValue 该键不存在时返回的默认值（读取"是否首次打开"时传 true 就是这个道理）
     * @return 保存的值，或默认值
     */
    public boolean readBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
}
