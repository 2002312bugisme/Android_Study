package com.zzz.server_application.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.zzz.server_application.database.UserDBHelper;

import java.util.Arrays;

/**
 * 服务端 ContentProvider：把本应用的 SQLite 表 {@code user_info} 暴露给其它应用
 *
 * <p>演示要点：
 * 1. ContentProvider 是<b>跨进程</b>被调用的：客户端的 insert/query 会通过 Binder 进入
 *    本应用的进程，在这里执行 SQL，再把结果返回给客户端；
 * 2. {@link UriMatcher} 用来区分"访问整张表"还是"访问某一行"：
 *    <ul>
 *      <li>{@code content://<authority>/user}     → 整张表（USERS）</li>
 *      <li>{@code content://<authority>/user/3}   → id=3 那一行（USER_ID）</li>
 *    </ul>
 * 3. 四个方法必须实现：insert / query / update / delete，另外 {@link #getType(Uri)}
 *    返回 MIME 类型（有些框架/工具会调它，抛异常会导致调用方出错）；
 * 4. 每次增删改之后调 {@code notifyChange()}，订阅了这个 Uri 的观察者（CursorLoader 等）才会收到通知；
 * 5. 清单里 {@code android:exported="true"} 才允许其它应用访问——
 *    这意味着<b>任何应用都能读写本表</b>，真实产品应配合
 *    {@code android:readPermission / writePermission} 自定义权限做限制。
 *
 * <p>相关文件：{@code database/UserDBHelper}（建表与连接）、客户端 {@code UserInfoContent}（契约类）
 */
public class UserInfoContentProvider extends ContentProvider {

    private static final String TAG = "UserInfoProvider";

    /**
     * 必须与清单里 android:authorities 完全一致（这是 ContentProvider 的"域名"）。
     *
     * <p>⚠️ 这个常量<b>必须</b>跟着清单一起改：清单里改对了、这里忘改，
     * 会出现"客户端能连上 provider，但 provider 内部 UriMatcher 匹配不上"的怪现象
     * （报 {@code IllegalArgumentException: 未知 Uri}），因为系统是按清单里的 authority 分发请求的。
     */
    public static final String AUTHORITY = "com.zzz.server_application.provider.UserInfoContentProvider";

    /** Uri 匹配码：整张表 */
    private static final int USERS = 1;
    /** Uri 匹配码：单行（路径末尾是 id） */
    private static final int USER_ID = 2;

    /** Uri 匹配器：构造时注册规则，后面每个方法用它判断本次请求访问的是整表还是单行 */
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // 注意：# 表示"任意数字"，会被当作 id 解析
        URI_MATCHER.addURI(AUTHORITY, "user", USERS);
        URI_MATCHER.addURI(AUTHORITY, "user/#", USER_ID);
    }

    /** 数据库帮助器（本进程内的单例） */
    private UserDBHelper userDBHelper = null;

    public UserInfoContentProvider() {
    }

    /**
     * Provider 被创建时回调（应用启动后第一次被访问时触发，早于任何一次数据操作）
     *
     * @return true 表示创建成功；返回 false 时后续调用会失败
     */
    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        // 注意：这里必须用 getContext()（Provider 自己的 Context），不能用某个 Activity 的
        userDBHelper = UserDBHelper.getInstance(getContext());
        return true;
    }

    /**
     * 返回指定 Uri 对应的 MIME 类型
     *
     * <p>约定格式：目录用 {@code vnd.android.cursor.dir/...}，单条用 {@code vnd.android.cursor.item/...}。
     * 客户端如果调用 {@code getType()} 拿到类型，就能判断"这是一个列表还是单个对象"。
     *
     * @param uri 请求的 Uri
     * @return MIME 类型字符串
     */
    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case USERS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".user";
            case USER_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".user";
            default:
                // 不认识的 Uri 明确抛异常，比返回 null 更容易排查
                throw new IllegalArgumentException("未知 Uri：" + uri);
        }
    }

    /**
     * 插入一条记录（客户端调 resolver.insert 时执行）
     *
     * @param uri    目标 Uri（本 demo 只支持整表 Uri）
     * @param values 要插入的列与值
     * @return 新行对应的 Uri（形如 content://.../user/7）；插入失败返回 null
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert: " + uri + " , values = " + values);
        if (URI_MATCHER.match(uri) != USERS) {
            throw new IllegalArgumentException("insert 只支持整表 Uri：" + uri);
        }
        if (values == null || values.size() == 0) {
            // 空的 ContentValues 会插入一条全 NULL 的记录，这里直接拒绝
            return null;
        }

        SQLiteDatabase db = userDBHelper.getWritableDatabase();
        // insert 返回新行 rowId，失败返回 -1（不抛异常，所以要自己判断）
        long rowId = db.insert(UserDBHelper.TABLE_NAME, null, values);
        if (rowId == -1) {
            Log.e(TAG, "insert 失败");
            return null;
        }

        // 把 rowId 拼到 Uri 末尾：content://.../user/7
        Uri newUri = ContentUris.withAppendedId(uri, rowId);
        // 通知所有观察这个 Uri 的监听者（CursorLoader / ContentObserver）数据变了
        getContext().getContentResolver().notifyChange(newUri, null);
        Log.d(TAG, "insert 成功，新行 Uri = " + newUri);
        return newUri;
    }

    /**
     * 查询（客户端调 resolver.query 时执行）
     *
     * @param uri          目标 Uri，整表或单行
     * @param projection   要查的列，null 表示全部
     * @param selection    WHERE 条件，null 表示不加条件
     * @param selectionArgs WHERE 里 ? 的取值
     * @param sortOrder    排序方式，null 表示默认顺序
     * @return 查询结果游标；调用方（客户端）负责 close()
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query: " + uri);

        // 根据 Uri 决定是"查整表"还是"按 id 查单行"
        String where = selection;
        String[] whereArgs = selectionArgs;
        if (URI_MATCHER.match(uri) == USER_ID) {
            // 取出路径最后一段作为 id，注意列名是 id（建表时没写成 _id）
            String id = uri.getLastPathSegment();
            where = "id = ?";
            whereArgs = new String[]{id};
        } else if (URI_MATCHER.match(uri) != USERS) {
            throw new IllegalArgumentException("未知 Uri：" + uri);
        }

        SQLiteDatabase db = userDBHelper.getReadableDatabase();
        // 注意：db.query 的参数顺序是 (表, 列, where, whereArgs, groupBy, having, orderBy)
        Cursor cursor = db.query(UserDBHelper.TABLE_NAME, projection,
                where, whereArgs, null, null, sortOrder);
        // 让游标挂上"数据变更通知"，客户端那边 registerContentObserver 才能收到
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * 更新记录（客户端调 resolver.update 时执行）
     *
     * @param uri           目标 Uri
     * @param values        要修改的列与值
     * @param selection     WHERE 条件
     * @param selectionArgs WHERE 里 ? 的取值
     * @return 受影响行数
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update: " + uri);
        if (values == null || values.size() == 0) {
            return 0;
        }

        String where = selection;
        String[] whereArgs = selectionArgs;
        if (URI_MATCHER.match(uri) == USER_ID) {
            where = "id = ?";
            whereArgs = new String[]{uri.getLastPathSegment()};
        } else if (URI_MATCHER.match(uri) != USERS) {
            throw new IllegalArgumentException("未知 Uri：" + uri);
        }

        SQLiteDatabase db = userDBHelper.getWritableDatabase();
        int rows = db.update(UserDBHelper.TABLE_NAME, values, where, whereArgs);
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    /**
     * 删除记录（客户端调 resolver.delete 时执行）
     *
     * <p>安全提示：如果 Uri 是整表且 selection 为 null，会删掉全部数据。
     * 真实产品里通常禁止"无条件删除"，或者要求必须带 id。
     *
     * @param uri           目标 Uri
     * @param selection     WHERE 条件
     * @param selectionArgs WHERE 里 ? 的取值
     * @return 被删除的行数
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: " + uri);

        String where = selection;
        String[] whereArgs = selectionArgs;
        if (URI_MATCHER.match(uri) == USER_ID) {
            where = "id = ?";
            // 拿到id：路径最后一段就是 id，例如 content://.../user/3 → "3"
            whereArgs = new String[]{uri.getLastPathSegment()};
        } else if (URI_MATCHER.match(uri) != USERS) {
            throw new IllegalArgumentException("未知 Uri：" + uri);
        }

        // 日志要打在"分支判断之后"：打在前面永远只能看到入参 null，
        // 会让人误以为"这次删除没有条件"，实际上单行 Uri 的条件是下面才拼出来的
        Log.d(TAG, "delete 最终条件: where = " + where + " , whereArgs = " + Arrays.toString(whereArgs));

        SQLiteDatabase db = userDBHelper.getWritableDatabase();
        int rows = db.delete(UserDBHelper.TABLE_NAME, where, whereArgs);
        if (rows > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }
}
