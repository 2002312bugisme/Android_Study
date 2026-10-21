package com.zzz.datasharingapplication.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.zzz.datasharingapplication.pojo.BookInfo;

import java.util.List;

/**
 * 图书表 DAO（Data Access Object）：Room 只需要<b>声明</b>方法，SQL 与实现都由它生成
 *
 * <p>演示要点：
 * 1. {@code @Dao} 标在接口上，Room 在编译期生成实现类（{@code BookDao_Impl}），
 *    所以这里一个方法体都没有；
 * 2. 四种注解各演示一种写法：
 *    <ul>
 *      <li>{@code @Insert} 的三种返回：可变长参数（void）、单条返回 rowId（long）、List 批量（void）</li>
 *      <li>{@code @Update} / {@code @Delete} 按<b>主键</b>匹配要改 / 删的行（即 BookInfo 的 id）</li>
 *      <li>{@code @Query} 手写 SQL，参数用 {@code :参数名} 占位</li>
 *    </ul>
 * 3. {@code @Query} 里的 SQL 会被 Room 在<b>编译期解析</b>：表名、列名写错会直接编译失败，
 *    这是它比手写 SQLite 语句最大的优势之一；
 * 4. 所有方法都是同步阻塞的，调用方负责放到子线程（本仓库 BookEntryActivity 用单线程池调用）。
 *
 * <p>相关文件：{@code database/BookDataBase}（声明表）、{@code BookEntryActivity}（调用方）
 */
@Dao
public interface BookDao {

    /**
     * 1. 可变长参数插入（一次传多本）
     *
     * @param books 待插入的图书，可以传 0 到 N 本
     */
    @Insert
    void insertBooks(BookInfo... books);

    /**
     * 2. 单条插入，返回自增主键
     *
     * @param book 待插入的图书（id 字段留 0 即可）
     * @return 新插入行的 rowId，也就是自增后的 id
     */
    @Insert
    long insertOne(BookInfo book);

    /**
     * 3. 批量插入（List 形式，内部仍是一条条执行；需要原子性要自己套事务）
     *
     * @param books 待插入的列表
     */
    @Insert
    void insertList(List<BookInfo> books);

    /**
     * 4. 更新（可变长参数）：按主键 id 找行，其余字段用传入对象的值覆盖
     *
     * @param books 要更新的图书（id 必须已存在，否则这一条不生效）
     */
    @Update
    void updateBooks(BookInfo... books);

    /**
     * 5. 删除（可变长参数）：按主键 id 删除
     *
     * @param books 要删除的图书（只用到它们的 id）
     */
    @Delete
    void deleteBooks(BookInfo... books);

    /**
     * 6. 查询全部
     *
     * @return 全部图书；表为空时返回空 List（不是 null）
     */
    @Query("SELECT * FROM book_info")
    List<BookInfo> queryAll();

    /**
     * 7. 按书名模糊查询
     *
     * <p>SQL 里的 {@code ||} 是 SQLite 的字符串拼接运算符，最终拼成 {@code '%关键字%'}；
     * 用 {@code :keyword} 占位而不是字符串拼接，可以避免 SQL 注入（Room 走绑定参数）。
     *
     * @param keyword 关键字（不含 % 号）
     * @return 匹配的图书列表
     */
    @Query("SELECT * FROM book_info WHERE bookName LIKE '%' || :keyword || '%'")
    List<BookInfo> queryByName(String keyword);

    /**
     * 8. 按 id 查单条
     *
     * @param id 主键
     * @return 找到的图书；不存在时返回 null
     */
    @Query("SELECT * FROM book_info WHERE id = :id")
    BookInfo queryById(int id);

    /**
     * 9. 删除全部记录（表结构保留；SQLite 的自增序列不会因此重置）
     */
    @Query("DELETE FROM book_info")
    void deleteAll();
}
