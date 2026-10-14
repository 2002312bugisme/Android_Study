package com.zzz.myactivityapplication.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 日期时间工具类（与 {@code :mytextapplication} 模块下的同名类内容一致）。
 *
 * <p>用途：生成带时区偏移的 ISO 8601 时间字符串，固定格式
 * {@code 2026-09-20T11:18:32.123+08:00}。
 * 本模块里它被 {@code BundleSendActivity} / {@code BundleReceiveActivity} 用来
 * 给 Bundle 里的 "response_time" 字段生成时间戳。
 *
 * <p>两个容易踩的点：
 * <ul>
 *   <li>{@link SimpleDateFormat} <b>不是线程安全</b>的；这里每次调用都新建实例，所以可并发调用。
 *       若想优化成静态字段复用，必须额外加同步（或用 {@code DateTimeFormatter}）；</li>
 *   <li>时区必须显式设置，否则用手机当前时区，同一段代码在不同手机上输出不同偏移量。</li>
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
        //      yyyy 年 / MM 月 / dd 日 / 'T' 字面量字符 T（单引号转义，否则 T 会被当占位符）
        //      HH 24 小时制 / mm 分 / ss 秒 / SSS 毫秒（3 位）
        //      XXX 时区偏移，形如 +08:00（注意是三个 X，一个 X 只输出 +08）
        //   ② Locale.US 指定语言环境，保证输出 ASCII 数字与符号，
        //      避免在阿拉伯语等区域设置下变成当地数字字符
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                        Locale.US
                );

        // 设置为上海时区
        // 形参是时区 ID 字符串（IANA 标准名）；不要写 "GMT+8"——那样不带规范规则
        simpleDateFormat.setTimeZone(
                TimeZone.getTimeZone("Asia/Shanghai")
        );

        // format 的形参是待格式化的 Date（这里取当前时刻），返回格式化后的字符串
        return simpleDateFormat.format(new Date());
    }



}
