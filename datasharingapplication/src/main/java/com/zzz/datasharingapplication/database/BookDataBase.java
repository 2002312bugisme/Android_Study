package com.zzz.datasharingapplication.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.zzz.datasharingapplication.dao.BookDao;
import com.zzz.datasharingapplication.pojo.BookInfo;


@Database(entities = {BookInfo.class}, version = 1, exportSchema = true)
public abstract class BookDataBase extends RoomDatabase {

    // 暴露 DAO，Room 会自动生成 _Impl 实现
    public abstract BookDao bookDao();
}