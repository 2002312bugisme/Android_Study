package com.zzz.mytextapplication.utils;

import android.content.Context;

public class Utils {
    // 根据手机的分辨率从dp 的单位 转换为 px pixel像素
    public static int dp2px(Context context, float dpValue){
        // 获取当前手机的像素密度 （1个dp 对应 几个px）
        float density = context.getResources().getDisplayMetrics().density;
        // 四舍五入取整
        return (int) (dpValue * density + 0.5f);
    }
}
