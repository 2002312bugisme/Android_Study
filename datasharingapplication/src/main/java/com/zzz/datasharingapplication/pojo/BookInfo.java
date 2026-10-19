package com.zzz.datasharingapplication.pojo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "book_info")
public class BookInfo {

    @PrimaryKey(autoGenerate = true)
    private int id;           // 自增主键

    private String bookName;  // 书名
    private String author;    // 作者
    private String publisher; // 出版社
    private double price;     // 价格
    private int pageCount;    // 页数

    public BookInfo() {}

    public BookInfo(String bookName, String author, String publisher, double price, int pageCount) {
        this.bookName = bookName;
        this.author = author;
        this.publisher = publisher;
        this.price = price;
        this.pageCount = pageCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
}
