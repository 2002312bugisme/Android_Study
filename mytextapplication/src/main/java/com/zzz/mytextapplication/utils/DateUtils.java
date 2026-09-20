package com.zzz.mytextapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    /**
     * 获取当前上海时间
     *
     * 格式：
     * 2026-09-20T11:18:32.123+08:00
     */
    public static String getNowTime() {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                        Locale.US
                );

        // 设置为上海时区
        simpleDateFormat.setTimeZone(
                TimeZone.getTimeZone("Asia/Shanghai")
        );

        return simpleDateFormat.format(new Date());
    }



}
