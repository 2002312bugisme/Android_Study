package com.zzz.server_application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.zzz.server_application.provider.UserInfoContentProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ContentProvider 的仪化测试（instrumented test）：在真机 / 模拟器上直接跑 update 与 delete
 *
 * <p>为什么用 instrumented 而不是普通单元测试：{@link ContentResolver}、{@link Cursor}、
 * SQLite 都是 Android 框架的实现，跑在 JVM 上的 local test 里全是 stub（调用即抛异常），
 * 必须在设备上执行。
 *
 * <p>运行方式：
 * <pre>
 *   ./gradlew :server_application:connectedDebugAndroidTest
 * </pre>
 *
 * <p>设计要点：
 * 1. 每个用例前 {@code delete(整表, null, null)} 清空表，保证用例互不影响（顺序无关）；
 * 2. 覆盖 provider 的<b>两条 Uri 分支</b>：整表 {@code /user} 与单行 {@code /user/#}；
 * 3. 既测"正常能改/能删"，也测"改不存在的行返回 0"和"未知路径抛异常"这类边界。
 */
@RunWith(AndroidJUnit4.class)
public class UserInfoContentProviderTest {

    /** 整表 Uri：content://<authority>/user */
    private static final Uri CONTENT_URI =
            Uri.parse("content://" + UserInfoContentProvider.AUTHORITY + "/user");

    // 列名（与服务端建表语句一致）
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_AGE = "age";
    private static final String COL_MARRIED = "married";

    private ContentResolver resolver;

    /**
     * 每个用例执行前：取 ContentResolver + 清空表
     */
    @Before
    public void setUp() {
        // getTargetContext() 拿到的是"被测应用"（server）的 Context，
        // 用它的 resolver 查询，走的就是本模块注册的那个 provider
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        resolver = context.getContentResolver();
        resolver.delete(CONTENT_URI, null, null);
    }

    /** 每个用例执行后：再清一次，避免把数据留给下一个用例或影响手工调试 */
    @After
    public void tearDown() {
        resolver.delete(CONTENT_URI, null, null);
    }

    // ==================== update ====================

    /**
     * 用<b>单行 Uri</b> 更新：{@code update(content://.../user/3, values, null, null)}
     *
     * <p>这就是客户端"按 ID 更新"按钮走的路径，验证返回值与列值都正确。
     */
    @Test
    public void update_byItemUri_shouldModifyMatchedRow() {
        long id = insertOne("Tom", 20);
        assertTrue("插入应返回有效 id", id > 0);

        // 1. 组装新值
        ContentValues values = new ContentValues();
        values.put(COL_NAME, "Jerry");
        values.put(COL_AGE, 31);
        values.put(COL_MARRIED, 1);

        // 2. 单行 Uri：id 拼在路径末尾
        Uri rowUri = ContentUris.withAppendedId(CONTENT_URI, id);

        // 3. 执行更新：selection 传 null，因为 Uri 已经限定了行
        int rows = resolver.update(rowUri, values, null, null);
        assertEquals("单行 Uri 更新应命中 1 行", 1, rows);

        // 4. 读回来核对内容
        Cursor cursor = resolver.query(rowUri, null, null, null, null);
        assertNotNull(cursor);
        try {
            assertTrue("应能查到这一行", cursor.moveToFirst());
            assertEquals("Jerry", cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
            assertEquals(31, cursor.getInt(cursor.getColumnIndexOrThrow(COL_AGE)));
            assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow(COL_MARRIED)));
        } finally {
            cursor.close();
        }
    }

    /**
     * 用<b>整表 Uri + selection</b> 更新：演示"条件更新"
     *
     * <p>只改 name = 'Tom' 的那一行，另一行必须保持不变 —— 这是 selection 传参的意义。
     */
    @Test
    public void update_bySelection_shouldOnlyTouchMatchedRows() {
        insertOne("Tom", 20);
        insertOne("Alice", 25);

        ContentValues values = new ContentValues();
        values.put(COL_AGE, 99);

        int rows = resolver.update(CONTENT_URI, values, COL_NAME + " = ?", new String[]{"Tom"});
        assertEquals("只有 Tom 命中", 1, rows);

        // Tom 被改成 99，Alice 保持 25
        assertEquals(99, queryAgeByName("Tom"));
        assertEquals(25, queryAgeByName("Alice"));
    }

    /**
     * 边界：更新一个不存在的 id
     *
     * <p>{@code update} 不会抛异常，只会返回 0 —— 客户端必须自己判断这个 0，
     * 否则用户会以为"点了更新就成功了"。
     */
    @Test
    public void update_unknownId_shouldReturnZero() {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, "Nobody");

        Uri missing = ContentUris.withAppendedId(CONTENT_URI, 99999);
        int rows = resolver.update(missing, values, null, null);
        assertEquals("不存在的 id 应返回 0 行", 0, rows);
    }

    // ==================== delete ====================

    /**
     * 用<b>单行 Uri</b> 删除：只删这一行，另一行必须还在
     */
    @Test
    public void delete_byItemUri_shouldRemoveOnlyThatRow() {
        long idTom = insertOne("Tom", 20);
        insertOne("Alice", 25);
        assertEquals("删之前应有 2 行", 2, countAll());

        Uri rowUri = ContentUris.withAppendedId(CONTENT_URI, idTom);
        int rows = resolver.delete(rowUri, null, null);
        assertEquals("单行 Uri 删除应命中 1 行", 1, rows);
        assertEquals("删之后应剩 1 行", 1, countAll());

        // 被删的 id 查不到了
        Cursor cursor = resolver.query(rowUri, null, null, null, null);
        assertNotNull(cursor);
        try {
            assertEquals("这一行应该已经被删掉", 0, cursor.getCount());
        } finally {
            cursor.close();
        }
    }

    /**
     * 用<b>整表 Uri + selection</b> 删除：条件删除
     */
    @Test
    public void delete_bySelection_shouldRemoveMatchedRows() {
        insertOne("Tom", 20);
        insertOne("Alice", 25);

        int rows = resolver.delete(CONTENT_URI, COL_NAME + " = ?", new String[]{"Alice"});
        assertEquals("只删 Alice", 1, rows);
        assertEquals("应剩 1 行", 1, countAll());
    }

    /**
     * 边界：删除不存在的 id，返回 0
     */
    @Test
    public void delete_unknownId_shouldReturnZero() {
        insertOne("Tom", 20);
        Uri missing = ContentUris.withAppendedId(CONTENT_URI, 99999);
        assertEquals("不存在的 id 应返回 0", 0, resolver.delete(missing, null, null));
        assertEquals("不能误删别的行", 1, countAll());
    }

    // ==================== UriMatcher 兜底 ====================

    /**
     * 边界：访问未注册的路径（{@code /nope}）应抛 {@link IllegalArgumentException}
     *
     * <p>这条能验证 {@code UriMatcher} 的 default 分支没有悄悄放过错误路径。
     */
    @Test
    public void query_unknownPath_shouldThrow() {
        Uri unknown = Uri.parse("content://" + UserInfoContentProvider.AUTHORITY + "/nope");
        try {
            Cursor cursor = resolver.query(unknown, null, null, null, null);
            if (cursor != null) {
                cursor.close();
            }
            fail("访问未注册的路径应当抛 IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // 符合预期
        }
    }

    // ==================== 测试辅助方法 ====================

    /**
     * 插入一条记录
     *
     * @param name 姓名
     * @param age  年龄
     * @return 新行的 id（从返回的 Uri 里解析）
     */
    private long insertOne(String name, int age) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_AGE, age);
        values.put("height", 170);
        values.put("weight", 60.0f);
        values.put(COL_MARRIED, 0);

        Uri inserted = resolver.insert(CONTENT_URI, values);
        assertNotNull("insert 不应返回 null", inserted);
        return ContentUris.parseId(inserted);
    }

    /**
     * 统计表内总行数
     *
     * @return 行数
     */
    private int countAll() {
        Cursor cursor = resolver.query(CONTENT_URI, null, null, null, null);
        assertNotNull(cursor);
        try {
            return cursor.getCount();
        } finally {
            cursor.close();
        }
    }

    /**
     * 按姓名查年龄（用于验证 update 结果）
     *
     * @param name 姓名
     * @return 年龄；查不到返回 -1
     */
    private int queryAgeByName(String name) {
        Cursor cursor = resolver.query(CONTENT_URI, null, COL_NAME + " = ?", new String[]{name}, null);
        assertNotNull(cursor);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(COL_AGE));
            }
            return -1;
        } finally {
            cursor.close();
        }
    }
}
