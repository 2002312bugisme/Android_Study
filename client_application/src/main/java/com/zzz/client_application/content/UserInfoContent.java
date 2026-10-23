package com.zzz.client_application.content;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 服务端 ContentProvider 的"契约类"：把 authority、Uri、列名这三样东西固定下来
 *
 * <p>演示要点：
 * 1. ContentProvider 的访问入口是一个 <b>content://</b> 开头的 Uri，格式为
 *    {@code content://<authority>/<路径>}，这里的 authority 就是服务端清单里
 *    {@code <provider android:authorities="...">} 声明的那个字符串；
 * 2. 契约类的意义：客户端只依赖这个类，Uri 与列名只写一处。
 *    服务端若改了表结构，只改这里即可（这也是 Android 官方的推荐做法）；
 * 3. <b>authority 必须与服务端清单里的完全一致</b>（区分大小写、不能有空格），
 *    否则 insert/query 会抛 {@code IllegalArgumentException: Unknown URL content://...}——
 *    这是跨应用调试时最常见的错误，先查这两处字符串是否逐字符相同。
 *
 * <p>相关文件：服务端 {@code com.zzz.server_application.provider.UserInfoContentProvider}
 */
public class UserInfoContent implements BaseColumns {

    /**
     * 服务端 provider 的 authority。
     *
     * <p>注意：它<b>只是个字符串标识</b>，并不要求等于包名或类名。
     * 这里沿用了"包名 + 类名"的写法，与 server 模块的包名
     * {@code com.zzz.server_application} 保持一致
     * （该模块早期误拼成 applition，2026-10-22 已全仓库统一修正）。
     */
    public static final String AUTHORITIES = "com.zzz.server_application.provider.UserInfoContentProvider";

    /** 访问内容提供器的基础 URI：content://<authority>/user */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/user");

    // ==================== 列名（必须与服务端建表语句逐个对应） ====================

    /**
     * 主键列名。
     *
     * <p>⚠️ 这里<b>不能</b>直接用 {@link BaseColumns#_ID}（值是 "_id"）：
     * 服务端建表时主键列叫 {@code id}（没有下划线）。
     * 用错列名时 {@code getColumnIndexOrThrow("_id")} 会抛
     * {@code IllegalArgumentException: column '_id' does not exist}。
     *
     * <p>顺带记一个知识点：ContentProvider 的通行惯例是主键用 {@code _id}
     * （因为 {@code SimpleCursorAdapter} / {@code CursorAdapter} 默认就找这一列），
     * 本 demo 沿用了 {@code id}，所以读数据时手动取列即可。
     */
    public static final String USER_ID = "id";

    /** 姓名（TEXT） */
    public static final String USER_NAME = "name";

    /** 年龄（INTEGER） */
    public static final String USER_AGE = "age";

    /** 身高（INTEGER） */
    public static final String USER_HEIGHT = "height";

    /** 体重（REAL） */
    public static final String USER_WEIGHT = "weight";

    /** 婚否（INTEGER，0=否，1=是；SQLite 没有 boolean 类型） */
    public static final String USER_MARRIED = "married";
}
