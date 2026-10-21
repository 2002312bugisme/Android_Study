package com.zzz.datasharingapplication.application;

import android.app.Application;

import androidx.room.Room;

import com.zzz.datasharingapplication.dao.BookDao;
import com.zzz.datasharingapplication.database.BookDataBase;

/**
 * Room 数据库的宿主 Application：负责在进程启动时把数据库实例建好，供全应用复用
 *
 * <p>演示要点：
 * 1. <b>一个 APK 只能注册一个 Application</b>（清单里 android:name 只认一个）。
 *    本模块同时演示 Room 和商城，所以真正的注册类是 {@code ShopApplication}，
 *    它 <b>继承</b> 本类——继承后 {@link #onCreate()} 依旧会被调用，Room 照常建库；
 * 2. {@code Room.databaseBuilder(...).build()} 只是"配置 + 建对象"，真正的数据库文件
 *    要到第一次执行 SQL 时才会被创建（延迟初始化）；
 * 3. {@code allowMainThreadQueries()} 允许主线程直接查库——<b>只为演示方便</b>，
 *    正式项目必须去掉，强制把查询放到子线程（本仓库 BookEntryActivity 就是自己开了单线程池）；
 * 4. 数据库实例要全局唯一：重复 build 会拿到多个连接，容易触发锁冲突甚至数据不一致。
 *
 * <p>涉及 API：{@link Room#databaseBuilder}、{@link BookDataBase}、{@link BookDao}
 */
public class BookApplication extends Application {

    // 单例：由系统调用 onCreate 时赋值，页面通过 getInstance() 取
    private static BookApplication instance;

    // Room 数据库实例（构建一次，全应用共用）
    private BookDataBase bookDataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        // 1. 保存自身引用，供静态方法 getInstance() 使用
        instance = this;

        // 2. 构建数据库唯一实例（单例）
        //    参数 1 = Context（用 applicationContext 防止持有 Activity）
        //    参数 2 = 数据库类（Room 生成的 _Impl）
        //    参数 3 = 数据库文件名，落盘在 /data/data/<包名>/databases/book_database
        bookDataBase = Room.databaseBuilder(getApplicationContext(),
                        BookDataBase.class, "book_database")
                .allowMainThreadQueries()   // 仅演示/测试用，正式项目应放到子线程
                .build();
    }

    /**
     * 获取 Application 单例
     *
     * @return 全进程唯一的 BookApplication；若本类未被注册进清单，这里会返回 null
     */
    public static BookApplication getInstance() {
        return instance;
    }

    /**
     * 取数据库实例
     *
     * @return Room 数据库对象，可继续 .bookDao() 拿 DAO
     */
    public BookDataBase getBookDataBase() {
        return bookDataBase;
    }

    /**
     * 便捷获取 DAO（省去每个页面都写 getInstance().getBookDataBase().bookDao()）
     *
     * @return 图书表操作入口
     */
    public static BookDao getBookDao() {
        return instance.bookDataBase.bookDao();
    }
}
