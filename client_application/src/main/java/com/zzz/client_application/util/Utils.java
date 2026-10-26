package com.zzz.client_application.util;

import android.content.Context;

/**
 * 通用工具类（与业务无关的小工具集中放这里）。
 *
 * <p>目前只提供 dp → px 的换算。为什么需要它：
 * 布局 XML 里的尺寸单位是 dp（与屏幕密度无关），但 Java 里 {@code ViewGroup.LayoutParams}
 * 的 width / height 只认 <b>px</b>（真实像素），所以用代码改尺寸时必须先换算，
 * 否则在高密度屏幕上会小得离谱。
 */
public class Utils {
    // 根据手机的分辨率从dp 的单位 转换为 px pixel像素
    /**
     * 把 dp 值换算成当前设备上的像素值。
     *
     * @param context 上下文，用来获取 Resources/DisplayMetrics（屏幕信息挂在它的资源上）；
     *                传 Activity 或 ApplicationContext 都可以，只要不为 null。
     * @param dpValue 要换算的 dp 数值，允许小数（所以是 float 而不是 int）。
     * @return 换算后的像素值（已四舍五入为整数），可直接赋给 LayoutParams 的 width / height。
     */
    public static int dp2px(Context context, float dpValue){
        // 获取当前手机的像素密度 （1个dp 对应 几个px）
        // density 是一个倍率：mdpi ≈ 1.0、xhdpi = 2.0、xxhdpi = 3.0
        float density = context.getResources().getDisplayMetrics().density;
        // 四舍五入取整
        // 加 0.5f 再强制转 int（截断小数）就是"四舍五入"的经典写法：
        // 例如 2.4 + 0.5 = 2.9 → 截断得 2；2.6 + 0.5 = 3.1 → 截断得 3
        return (int) (dpValue * density + 0.5f);
    }
}
