package com.zzz.server_application.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBHelper extends SQLiteOpenHelper {
    // 数据库文件名：落盘为 /data/data/<包名>/databases/user.db
    private static final String DB_NAME = "user.db";
    // 表名
    public static final String TABLE_NAME = "user_info";
    // 单例对象
    private static UserDBHelper mHelper = null;
    // 读连接（懒加载）
    private SQLiteDatabase mRDB = null;
    // 写连接（懒加载）
    private SQLiteDatabase mWDB = null;

    // 数据库版本号：改表结构时必须 +1，否则 onUpgrade 不会触发
    private static final int DB_VERSION = 1;

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
            // 用 this 而不是静态的 mHelper：万一有人直接 new UserDBHelper(ctx) 再调这个方法，
            // 静态字段可能还是 null，用 this 永远指向自己，不会 NPE
            mRDB = getReadableDatabase();
        }
        return mRDB;
    }

    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = getWritableDatabase();
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
