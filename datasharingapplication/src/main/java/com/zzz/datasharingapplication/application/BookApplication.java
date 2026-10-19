package com.zzz.datasharingapplication.application;

import android.app.Application;

import androidx.room.Room;

import com.zzz.datasharingapplication.dao.BookDao;
import com.zzz.datasharingapplication.database.BookDataBase;

public class BookApplication extends Application {

    private static BookApplication instance;
    private BookDataBase bookDataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 构建数据库唯一实例（单例）
        bookDataBase = Room.databaseBuilder(getApplicationContext(),
                        BookDataBase.class, "book_database")
                .allowMainThreadQueries()   // 仅演示/测试用，正式项目应放到子线程
                .build();
    }

    public static BookApplication getInstance() {
        return instance;
    }

    public BookDataBase getBookDataBase() {
        return bookDataBase;
    }

    // 便捷获取 DAO
    public static BookDao getBookDao() {
        return instance.bookDataBase.bookDao();
    }
}