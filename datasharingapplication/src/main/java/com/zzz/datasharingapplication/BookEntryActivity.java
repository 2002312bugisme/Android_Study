package com.zzz.datasharingapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.datasharingapplication.application.BookApplication;
import com.zzz.datasharingapplication.dao.BookDao;
import com.zzz.datasharingapplication.pojo.BookInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookEntryActivity extends AppCompatActivity {
    // 表单控件
    private EditText etBookName, etAuthor, etPublisher, etPrice, etPageCount;
    private Button btnSaveBook, btnClearInput, btnDeleteAllBooks;
    private TextView tvBookCount;
    private ListView lvBookList;

    private SimpleAdapter simpleAdapter;
    private final List<Map<String, Object>> listData = new ArrayList<>();

    private BookDao bookDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_entry);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        initViews();
        initList();
        initData();
        initListeners();
    }

    /** 绑定控件 */
    private void initViews() {
        etBookName         = findViewById(R.id.etBookName);
        etAuthor           = findViewById(R.id.etAuthor);
        etPublisher        = findViewById(R.id.etPublisher);
        etPrice            = findViewById(R.id.etPrice);
        etPageCount        = findViewById(R.id.etPageCount);
        btnSaveBook        = findViewById(R.id.btnSaveBook);
        btnClearInput      = findViewById(R.id.btnClearInput);
        btnDeleteAllBooks  = findViewById(R.id.btnDeleteAllBooks);
        tvBookCount        = findViewById(R.id.tvBookCount);
        lvBookList         = findViewById(R.id.lvBookList);
    }

    /** 初始化 ListView + SimpleAdapter */
    private void initList() {
        // key 是 Map 的键，value 是 item 布局中 TextView 的 id
        String[] from = {"bookName", "author", "publisher", "pricePage"};
        int[] to = {R.id.tvItemBookName, R.id.tvItemAuthor,
                R.id.tvItemPublisher, R.id.tvItemPricePage};

        simpleAdapter = new SimpleAdapter(
                this,
                listData,
                R.layout.item_book_list,
                from,
                to);

        lvBookList.setAdapter(simpleAdapter);
    }

    /** 获取 DAO 并首次加载 */
    private void initData() {
        bookDao = BookApplication.getBookDao();
        loadBooks();
    }

    /** 按钮事件 */
    private void initListeners() {
        btnSaveBook.setOnClickListener(v -> saveBook());
        btnClearInput.setOnClickListener(v -> clearInputs());
        btnDeleteAllBooks.setOnClickListener(v -> {
            executor.execute(() -> {
                bookDao.deleteAll();
                runOnUiThread(() -> {
                    Toast.makeText(this, "已删除全部书籍", Toast.LENGTH_SHORT).show();
                    loadBooks();
                });
            });
        });
    }

    /** 表单校验 + 插入 */
    private void saveBook() {
        String name      = etBookName.getText().toString().trim();
        String author    = etAuthor.getText().toString().trim();
        String publisher = etPublisher.getText().toString().trim();
        String priceStr  = etPrice.getText().toString().trim();
        String pageStr   = etPageCount.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "书名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(author)) {
            Toast.makeText(this, "作者不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        double price;
        int pageCount;
        try {
            price = TextUtils.isEmpty(priceStr) ? 0 : Double.parseDouble(priceStr);
            pageCount = TextUtils.isEmpty(pageStr) ? 0 : Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "价格或页数格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        BookInfo book = new BookInfo(name, author, publisher, price, pageCount);

        executor.execute(() -> {
            bookDao.insertOne(book);
            runOnUiThread(() -> {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                clearInputs();
                loadBooks();
            });
        });
    }

    /** 子线程查询 → 主线程刷新 ListView */
    private void loadBooks() {
        executor.execute(() -> {
            List<BookInfo> books = bookDao.queryAll();
            runOnUiThread(() -> {
                listData.clear();
                for (BookInfo b : books) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("bookName", b.getBookName());
                    row.put("author", "作者：" + b.getAuthor());
                    row.put("publisher", "出版社：" + b.getPublisher());
                    row.put("pricePage", "价格：" + b.getPrice() + " 元   页数：" + b.getPageCount());
                    listData.add(row);
                }
                simpleAdapter.notifyDataSetChanged();   // 刷新列表
                tvBookCount.setText("已录入：" + books.size() + " 本");
            });
        });
    }

    private void clearInputs() {
        etBookName.setText("");
        etAuthor.setText("");
        etPublisher.setText("");
        etPrice.setText("");
        etPageCount.setText("");
        etBookName.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}