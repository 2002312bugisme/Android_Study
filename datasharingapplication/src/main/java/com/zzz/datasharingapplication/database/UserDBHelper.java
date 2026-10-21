package com.zzz.datasharingapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.zzz.datasharingapplication.pojo.User;

/**
 * 用户表数据库帮助器（SQLiteOpenHelper 子类）：把"建库、建表、升级、增删改查"封装成一个单例
 *
 * <p>演示要点：
 * 1. {@link SQLiteOpenHelper} 把三件事绑定在一起：库文件名、版本号、建表 SQL。
 *    {@link #onCreate} 只在<b>数据库文件第一次创建</b>时执行一次；
 *    {@link #onUpgrade} 只在<b>版本号变大</b>时执行，所以改表结构必须把 DB_VERSION +1；
 * 2. 读写连接分离（{@link #openReadLink()} / {@link #openWriteLink()}）：读用可读库、
 *    写用可写库，内部各自缓存句柄，避免反复打开；
 * 3. <b>连接的开关由使用方（Activity 的 onStart / onStop）负责</b>，
 *    这在本模块是最"教科书"的写法；
 *    但要注意它是进程级单例——一旦某处 closeLink()，别处手里的连接就都变成已关闭状态，
 *    所以商城那套（{@code ShoppingDBHelper}）改成了"每次操作前自愈式重开"，更抗误用；
 * 4. {@link #insertBatchWithTransaction} 演示事务：全部成功才提交，中途异常整体回滚。
 *
 * <p>相关文件：{@code pojo/User}（实体）、{@code SqliteHelperActivity}（增删改查界面）
 */
public class UserDBHelper extends SQLiteOpenHelper {

    // 数据库文件名：落盘为 /data/data/<包名>/databases/user.db
    private static final String DB_NAME = "user.db";
    // 表名
    private static final String TABLE_NAME = "user_info";
    // 单例对象
    private static UserDBHelper mHelper = null;
    // 读连接（懒加载）
    private SQLiteDatabase mRDB = null;
    // 写连接（懒加载）
    private SQLiteDatabase mWDB = null;

    // 数据库版本号：改表结构时必须 +1，否则 onUpgrade 不会触发
    private static final int DB_VERSION = 1;

    /**
     * 构造方法（public，便于演示直接 new；实际都走 {@link #getInstance(Context)}）
     *
     * @param context 上下文；SQLiteOpenHelper 内部只用它定位 databases 目录
     */
    public UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 使用单例模式获取数据库帮助器的唯一实例
    public static UserDBHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new UserDBHelper(context);
        }
        return mHelper;
    }

    // 打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mRDB == null || !mRDB.isOpen()) {
            mRDB = mHelper.getReadableDatabase();
        }
        return mRDB;
    }

    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = mHelper.getWritableDatabase();
        }
        return mWDB;
    }

    // 关闭数据库连接
    public void closeLink() {
        if (mRDB != null && mRDB.isOpen()) {
            mRDB.close();
            mRDB = null;
        }

        if (mWDB != null && mWDB.isOpen()) {
            mWDB.close();
            mWDB = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建数据库，执行建表语句
        // 表名：user_info
        // 字段说明：
        //   id      —— 主键，自增（INTEGER PRIMARY KEY AUTOINCREMENT）
        //   name    —— 姓名（TEXT）
        //   age     —— 年龄（INTEGER）
        //   height  —— 身高（INTEGER，对应 Java 的 long）
        //   weight  —— 体重（REAL，对应 Java 的 float）
        //   married —— 婚否（INTEGER，0 表示否，1 表示是）
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "age INTEGER, "
                + "height INTEGER, "
                + "weight REAL, "
                + "married INTEGER"
                + ")";
        db.execSQL(sql);
    }

    // DB_VERSION 发生改变，便监听，进行触发
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库版本升级时调用
        // oldVersion：升级前的版本号
        // newVersion：升级后的版本号

        // 从版本 1 升级到版本 2：新增 phone 和 password 两个字段
//        if (oldVersion < 2) {
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN phone TEXT");
//            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN password TEXT");
//        }
    }

    /**
     * 向 user_info 表中插入一条用户记录
     *
     * @param user 待插入的 User 对象。注意：
     *             - user.id 无需赋值，由数据库自增生成；
     *             - 其余字段（name/age/height/weight/married）会被写入数据库。
     *             如果 user 为 null，或关键字段为空，建议在调用前先做校验。
     * @return 插入成功时返回新行的 rowId（> 0）；
     * 插入失败时返回 -1。
     */
    public long insert(User user) {
        // ContentValues 用于以键值对的形式存放要写入的列和值
        // 键：数据库表中的列名
        // 值：对应要写入的数据
        ContentValues values = new ContentValues();

        // 姓名 —— TEXT
        values.put("name", user.name);

        // 年龄 —— INTEGER
        values.put("age", user.age);

        // 身高 —— INTEGER（Java 的 long 在 SQLite 中用 INTEGER 存储）
        values.put("height", user.height);

        // 体重 —— REAL（Java 的 float 在 SQLite 中用 REAL 存储）
        values.put("weight", user.weight);

        // 婚否 —— INTEGER（boolean 转成 0/1，true→1，false→0）
        // SQLite 没有 boolean 类型，所以这里手动转换
        values.put("married", user.married ? 1 : 0);

        // id 不写入，交给数据库自增

        // 调用 SQLiteDatabase.insert() 执行插入
        // 参数1：表名
        // 参数2：nullColumnHack。当 values 为空时，如果传 null，会插入一条所有列都为 NULL 的记录；
        //       传入一个列名（通常传 null 即可），可避免插入空行。这里传 null 即可。
        // 参数3：要插入的键值对集合
        // 返回值：新插入行的 rowId，失败返回 -1
        return mWDB.insert(TABLE_NAME, null, values);
    }

    /**
     * 按 id 删除一条记录
     *
     * <p>whereClause 里的 {@code ?} 是占位符，真正的值放在 whereArgs 数组里，
     * 这样能避免把用户输入直接拼进 SQL（SQL 注入）。
     *
     * @param id 要删除记录的主键
     * @return 受影响行数：1 = 删掉了，0 = 没有这个 id
     */
    public int delete(int id) {
        return mWDB.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
    }

    /**
     * 按 id 更新一条记录（用传入对象的所有字段覆盖原行）
     *
     * @param user 要更新的用户；其中 {@code user.id} 决定"改哪一行"，必须已存在
     * @return 受影响行数：1 = 改成功，0 = 没有这个 id
     */
    public int update(User user) {
        ContentValues values = new ContentValues();
        values.put("name", user.name);
        values.put("age", user.age);
        values.put("height", user.height);
        values.put("weight", user.weight);
        values.put("married", user.married ? 1 : 0);
        return mWDB.update(TABLE_NAME, values, "id = ?",
                new String[]{String.valueOf(user.id)});
    }

    /**
     * 按 id 查询单条记录
     *
     * <p>{@code query()} 的七个参数依次是：表名、要查的列（null=全部）、where、where 的占位值、
     * groupBy、having、orderBy。
     *
     * @param id 主键
     * @return 查到的用户；不存在时返回 null
     */
    public User queryById(int id) {
        User user = null;
        android.database.Cursor cursor = mRDB.query(
                TABLE_NAME, null, "id = ?",
                new String[]{String.valueOf(id)},
                null, null, null);
        // Cursor 必须关闭，否则会泄漏游标（系统限制每个进程的游标数）
        if (cursor != null) {
            if (cursor.moveToFirst()) {      // moveToFirst 返回 false 表示结果集为空
                user = readUser(cursor);
            }
            cursor.close();
        }
        return user;
    }

    /**
     * 查询全部记录（按 id 升序）
     *
     * @return 用户列表；表为空时返回空 List（不是 null）
     */
    public java.util.List<User> queryAll() {
        java.util.List<User> list = new java.util.ArrayList<>();
        android.database.Cursor cursor = mRDB.query(
                TABLE_NAME, null, null, null, null, null, "id ASC");
        if (cursor != null) {
            // moveToNext 从第一行开始往下走，走到没有数据为止
            while (cursor.moveToNext()) {
                list.add(readUser(cursor));
            }
            cursor.close();
        }
        return list;
    }

    /**
     * 从 Cursor 的当前行读出一个 User 对象（三处查询共用的解析逻辑）
     *
     * <p>用 {@code getColumnIndexOrThrow} 而不是 {@code getColumnIndex}：
     * 列名写错时前者会立刻抛异常，后者只会返回 -1 然后读出莫名其妙的值。
     *
     * @param c 已经 moveToFirst / moveToNext 到某一行的游标
     * @return 该行对应的用户对象
     */
    private User readUser(android.database.Cursor c) {
        User u = new User();
        u.id = c.getInt(c.getColumnIndexOrThrow("id"));
        u.name = c.getString(c.getColumnIndexOrThrow("name"));
        u.age = c.getInt(c.getColumnIndexOrThrow("age"));
        u.height = c.getLong(c.getColumnIndexOrThrow("height"));
        u.weight = c.getFloat(c.getColumnIndexOrThrow("weight"));
        // SQLite 没有 boolean，约定 1=已婚、0=未婚
        u.married = c.getInt(c.getColumnIndexOrThrow("married")) == 1;
        return u;
    }

    /**
     * 批量插入，带事务，返回是否全部成功
     *
     * @param userList 要插入的用户列表
     * @return true 全部成功并提交；false 出现异常并回滚
     */
    public boolean insertBatchWithTransaction(java.util.List<User> userList) {
        mWDB.beginTransaction();
        try {
            for (int i = 0; i < userList.size(); i++) {
                User user = userList.get(i);

                // 故意在第 2 条时抛异常，模拟失败
                if (i == 1) {
                    int x = 10 / 0;   // ArithmeticException
                }

                ContentValues values = new ContentValues();
                values.put("name", user.name);
                values.put("age", user.age);
                values.put("height", user.height);
                values.put("weight", user.weight);
                values.put("married", user.married ? 1 : 0);

                long rowId = mWDB.insert(TABLE_NAME, null, values);
                if (rowId == -1) {
                    // 任何一条插入失败，直接抛异常触发回滚
                    throw new RuntimeException("插入失败：" + user.name);
                }
            }
            // 全部成功，标记提交
            mWDB.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            mWDB.endTransaction();
        }
    }


}
