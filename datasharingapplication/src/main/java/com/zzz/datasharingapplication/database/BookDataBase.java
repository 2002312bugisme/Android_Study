package com.zzz.datasharingapplication.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.zzz.datasharingapplication.dao.BookDao;
import com.zzz.datasharingapplication.pojo.BookInfo;

/**
 * Room 数据库定义：{@code @Database} 告诉 Room "这个抽象类描述了一个数据库"
 *
 * <p>演示要点：
 * 1. {@code entities = {BookInfo.class}} 声明库里有哪些表（每个 {@code @Entity} 一张表）；
 * 2. {@code version = 1} 是<b>数据库版本号</b>：以后改表结构必须 +1 并写 Migration，
 *    否则老用户升级 App 会直接崩（Room 会校验 schema 哈希）；
 * 3. {@code exportSchema = true} 会把建表 DDL 导出成 JSON（路径由
 *    {@code room.schemaLocation} 指定），提交进版本库才能对比版本差异；
 * 4. 本类是 <b>abstract</b> 的，实现类 {@code BookDataBase_Impl} 由注解处理器在编译期生成，
 *    不需要也不能手写。
 *
 * <p>涉及 API：{@link Database}、{@link RoomDatabase}、{@code Room.databaseBuilder(...)}（在 BookApplication 里调用）
 */
@Database(entities = {BookInfo.class}, version = 1, exportSchema = true)
public abstract class BookDataBase extends RoomDatabase {

    /**
     * 暴露 DAO：抽象方法，Room 会自动生成"返回一个 BookDao 实现"的代码
     *
     * @return 图书表的操作入口
     */
    public abstract BookDao bookDao();
}
