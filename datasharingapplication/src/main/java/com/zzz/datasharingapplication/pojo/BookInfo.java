package com.zzz.datasharingapplication.pojo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 图书实体：Room 的 {@code @Entity} 注解声明"这个类对应一张表"
 *
 * <p>演示要点：
 * 1. {@code @Entity(tableName = "book_info")} 指定表名；不写 tableName 时默认用类名；
 * 2. {@code @PrimaryKey(autoGenerate = true)} 表示主键由 SQLite 自增（等价于 AUTOINCREMENT），
 *    插入时把 id 留 0 即可，Room 会回填真实 id；
 * 3. Room 要求"每个字段要么是 public，要么有 getter/setter"——它靠这些方法读写列，
 *    所以这里保持了标准的 getter/setter；
 * 4. 有两个构造方法时，编译会提示 {@code multiple good constructors and Room will pick the no-arg constructor}：
 *    这是<b>警告不是错误</b>，Room 会选无参构造；想消掉警告就给全参构造加 {@code @Ignore}。
 *
 * <p>相关文件：{@code dao/BookDao}（SQL 语句都写在那里）、
 * {@code database/BookDataBase}（@Database 声明）、{@code BookEntryActivity}（录入界面）
 */
@Entity(tableName = "book_info")
public class BookInfo {

    /** 主键：自增。新增时不要手动赋值 */
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String bookName;  // 书名（列名默认取字段名 bookName）
    private String author;    // 作者
    private String publisher; // 出版社
    private double price;     // 价格（SQLite 存为 REAL）
    private int pageCount;    // 页数（SQLite 存为 INTEGER）

    /**
     * 无参构造：Room 查询时用它来 new 对象，再逐字段反射赋值
     */
    public BookInfo() {}

    /**
     * 全参构造（不含 id）：新增一本图书时用，id 交给数据库自增
     *
     * @param bookName  书名
     * @param author    作者
     * @param publisher 出版社
     * @param price     价格（元）
     * @param pageCount 页数
     */
    public BookInfo(String bookName, String author, String publisher, double price, int pageCount) {
        this.bookName = bookName;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.pageCount = pageCount;
    }

    /** @return 主键 id */
    public int getId() { return id; }

    /** @param id 主键 id（一般由数据库回填，不手动设置） */
    public void setId(int id) { this.id = id; }

    /** @return 书名 */
    public String getBookName() { return bookName; }

    /** @param bookName 书名 */
    public void setBookName(String bookName) { this.bookName = bookName; }

    /** @return 作者 */
    public String getAuthor() { return author; }

    /** @param author 作者 */
    public void setAuthor(String author) { this.author = author; }

    /** @return 出版社 */
    public String getPublisher() { return publisher; }

    /** @param publisher 出版社 */
    public void setPublisher(String publisher) { this.publisher = publisher; }

    /** @return 价格 */
    public double getPrice() { return price; }

    /** @param price 价格 */
    public void setPrice(double price) { this.price = price; }

    /** @return 页数 */
    public int getPageCount() { return pageCount; }

    /** @param pageCount 页数 */
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
}
