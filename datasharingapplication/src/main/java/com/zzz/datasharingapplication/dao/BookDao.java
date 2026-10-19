package com.zzz.datasharingapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.zzz.datasharingapplication.pojo.BookInfo;

import java.util.List;

@Dao
public interface BookDao {
    // 1. 可变长参数插入（一次传多本）
    @Insert
    void insertBooks(BookInfo... books);

    // 2. 单条插入，返回行号
    @Insert
    long insertOne(BookInfo book);

    // 3. 批量插入
    @Insert
    void insertList(List<BookInfo> books);

    // 4. 更新（可变长）
    @Update
    void updateBooks(BookInfo... books);

    // 5. 删除（可变长）
    @Delete
    void deleteBooks(BookInfo... books);

    // 6. 查询全部
    @Query("SELECT * FROM book_info")
    List<BookInfo> queryAll();

    // 7. 按书名模糊查询（占位符用 :keyword）
    @Query("SELECT * FROM book_info WHERE bookName LIKE '%' || :keyword || '%'")
    List<BookInfo> queryByName(String keyword);

    // 8. 按 id 查询单条
    @Query("SELECT * FROM book_info WHERE id = :id")
    BookInfo queryById(int id);

    // 9. 删除全部
    @Query("DELETE FROM book_info")
    void deleteAll();
}