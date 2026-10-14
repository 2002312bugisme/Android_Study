package com.zzz.mytextapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期时间工具类。
 *
 * <p>用途：生成"带时区偏移的 ISO 8601 时间字符串"，格式固定为
 * {@code 2026-09-20T11:18:32.123+08:00}，适合做接口参数或日志时间戳。
 *
 * <p>两个容易踩的点：
 * <ul>
 *   <li>{@link SimpleDateFormat} <b>不是线程安全</b>的，这里每次调用都新建实例，
 *       所以本方法可以并发调用；若把它抽成静态成员复用就要加同步；</li>
 *   <li>时区必须显式设置，否则用的是手机当前时区——同一段代码在不同手机上会输出不同的偏移量。</li>
 * </ul>
 */
public class DateUtils {
    /**
     * 获取当前上海时间
     *
     * 格式：
     * 2026-09-20T11:18:32.123+08:00
     *
     * @return 按上述格式格式化好的当前时间字符串，时区固定为 Asia/Shanghai。
     *         例：{@code 2026-09-20T11:18:32.123+08:00}
     */
    public static String getNowTime() {
        // 创建格式化器，形参含义：
        //   ① 格式串 "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        //      yyyy 年 / MM 月 / dd 日 / 'T' 字面量字符 T（用单引号转义，否则 T 会被当占位符）
        //      HH 24 小时制 / mm 分 / ss 秒 / SSS 毫秒（3 位）
        //      XXX 时区偏移，形如 +08:00（注意是三个 X，一个 X 只会输出 +08）
        //   ② Locale.US 指定语言环境，保证输出的是 ASCII 数字与符号，
        //      避免在阿拉伯语等区域设置下变成当地数字字符
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                        Locale.US
                );

        // 设置为上海时区
        // 形参是时区 ID 字符串（IANA 标准名），不要写成 "GMT+8" —— 那样不带夏令时规则、也不规范
        simpleDateFormat.setTimeZone(
                TimeZone.getTimeZone("Asia/Shanghai")
        );

        // format 的形参是待格式化的 Date（这里取当前时刻），返回格式化后的字符串
        return simpleDateFormat.format(new Date());
    }



}
