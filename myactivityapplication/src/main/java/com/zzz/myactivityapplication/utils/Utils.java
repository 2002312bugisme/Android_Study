package com.zzz.myactivityapplication.utils;

import android.content.Context;

/**
 * 通用工具类（与业务无关的小工具集中放这里）。
 *
 * <p>注意：本模块与 {@code :mytextapplication} 模块各有一份<b>内容完全相同</b>的 Utils，
 * 原因是三个 Java 模块彼此独立（各自是独立 application，互不依赖），
 * 没法直接复用同一个工具类。这属于教学项目的常见取舍；
 * 真做多模块工程时，规范做法是把这类公共代码抽成一个单独的 library 模块（如 :core），
 * 让各模块以 {@code implementation project(':core')} 的方式共用。
 *
 * <p>目前只提供 dp → px 换算：布局 XML 里用 dp，而 Java 里
 * {@code ViewGroup.LayoutParams} 的 width / height 只认 px，所以必须换算。
 */
public class Utils {
    // 根据手机的分辨率从dp 的单位 转换为 px pixel像素
    /**
     * 把 dp 值换算成当前设备上的像素值。
     *
     * @param context 上下文，用来获取 Resources / DisplayMetrics（屏幕信息挂在资源上）；
     *                传 Activity 或 ApplicationContext 都可以，只要不为 null。
     * @param dpValue 要换算的 dp 数值，允许小数（所以是 float）。
     * @return 换算后的像素值（已四舍五入为整数），可直接赋给 LayoutParams 的 width / height。
     */
    public static int dp2px(Context context, float dpValue){
        // 获取当前手机的像素密度 （1个dp 对应 几个px）
        // density 是倍率：mdpi ≈ 1.0、xhdpi = 2.0、xxhdpi = 3.0
        float density = context.getResources().getDisplayMetrics().density;
        // 四舍五入取整
        // 加 0.5f 再强制转 int（截断小数）就是"四舍五入"的经典写法
        return (int) (dpValue * density + 0.5f);
    }
}
