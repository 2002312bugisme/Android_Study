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

/**
 * Room 图书录入页：表单校验 → 子线程落库 → 主线程刷新列表
 *
 * <p>演示要点：
 * 1. <b>DAO 是同步阻塞的</b>，必须在子线程调用。这里用一个
 *    {@link Executors#newSingleThreadExecutor()} 单线程池：既保证串行（不会并发写同一张表），
 *    又不用每次 new Thread；
 * 2. 子线程拿到结果后要回主线程刷 UI —— 用 {@link #runOnUiThread(Runnable)}，
 *    这是最简单的主线程切换方式（等价于 Handler.post）；
 * 3. 列表用 {@link SimpleAdapter} + {@code List<Map<String,Object>>}：
 *    "Map 的 key" 与 "item 布局里控件的 id" 由 from/to 两个数组建立映射，
 *    适合字段少、结构固定的场景；字段多或需要复杂交互时应该换 RecyclerView + 自定义 Adapter；
 * 4. 页面销毁时 {@code executor.shutdown()}，否则线程池会一直活着（虽然进程结束也会回收）。
 *
 * <p>对应布局：{@code activity_book_entry.xml}（列表项 {@code item_book_list.xml}）
 * <p>涉及 API：{@link SimpleAdapter}、{@link ExecutorService}、{@link #runOnUiThread}、
 * {@code BookDao#insertOne} / {@code BookDao#queryAll} / {@code BookDao#deleteAll}
 */
public class BookEntryActivity extends AppCompatActivity {

    // ===== 表单控件 =====
    private EditText etBookName, etAuthor, etPublisher, etPrice, etPageCount;
    // 按钮：保存 / 清空输入 / 删除全部
    private Button btnSaveBook, btnClearInput, btnDeleteAllBooks;
    // 已录入本数统计
    private TextView tvBookCount;
    // 图书列表
    private ListView lvBookList;

    // SimpleAdapter 的数据源：一行 = 一个 Map，key 与 item 布局的控件靠 from/to 数组对应
    private SimpleAdapter simpleAdapter;
    private final List<Map<String, Object>> listData = new ArrayList<>();

    // Room 的 DAO（所有增删查都走它）
    private BookDao bookDao;

    // 单线程池：保证数据库操作串行、且不占用主线程
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 页面创建：绑定控件 → 建列表 → 取 DAO 并首次加载 → 绑定按钮
     *
     * @param savedInstanceState 系统保存的临时状态（列表数据来自数据库，无需恢复）
     */
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

        // 拆成四步，顺序不能乱：先有控件 → 再有列表 → 再取 DAO 查数据 → 最后绑事件
        initViews();
        initList();
        initData();
        initListeners();
    }

    /** 绑定控件（id 全部来自 activity_book_entry.xml） */
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
        // key 是 Map 的键（下面 loadBooks 里 put 的名字），value 是 item 布局中 TextView 的 id
        String[] from = {"bookName", "author", "publisher", "pricePage"};
        int[] to = {R.id.tvItemBookName, R.id.tvItemAuthor,
                R.id.tvItemPublisher, R.id.tvItemPricePage};

        // 参数：上下文 / 数据源 / item 布局 / 从哪些 key 取 / 填到哪些控件
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
        // DAO 从 Application 里取（Room 数据库实例全应用唯一）
        bookDao = BookApplication.getBookDao();
        loadBooks();
    }

    /** 按钮事件 */
    private void initListeners() {
        btnSaveBook.setOnClickListener(v -> saveBook());
        btnClearInput.setOnClickListener(v -> clearInputs());

        // 删除全部：同样是"子线程操作库 → 主线程刷新界面"
        btnDeleteAllBooks.setOnClickListener(v -> {
            executor.execute(() -> {              // 子线程：执行 SQL
                bookDao.deleteAll();
                runOnUiThread(() -> {             // 主线程：更新 UI
                    Toast.makeText(this, "已删除全部书籍", Toast.LENGTH_SHORT).show();
                    loadBooks();
                });
            });
        });
    }

    /**
     * 表单校验 + 插入
     *
     * <p>校验顺序：必填项 → 数字格式。任何一步失败都直接 return，不进入写库流程。
     */
    private void saveBook() {
        // 1. 取输入值（trim 去首尾空格）
        String name      = etBookName.getText().toString().trim();
        String author    = etAuthor.getText().toString().trim();
        String publisher = etPublisher.getText().toString().trim();
        String priceStr  = etPrice.getText().toString().trim();
        String pageStr   = etPageCount.getText().toString().trim();

        // 2. 必填校验：书名、作者不能为空
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "书名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(author)) {
            Toast.makeText(this, "作者不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. 数值校验：价格与页数允许留空（留空按 0 处理），但填了就必须能解析
        double price;
        int pageCount;
        try {
            price = TextUtils.isEmpty(priceStr) ? 0 : Double.parseDouble(priceStr);
            pageCount = TextUtils.isEmpty(pageStr) ? 0 : Integer.parseInt(pageStr);
        } catch (NumberFormatException e) {
            // 例如用户输入 "12.3.4" 或 "abc"
            Toast.makeText(this, "价格或页数格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. 组装实体（id 不赋值，交给 Room 自增）
        BookInfo book = new BookInfo(name, author, publisher, price, pageCount);

        // 5. 子线程写库，成功后回主线程清空输入并刷新列表
        executor.execute(() -> {
            bookDao.insertOne(book);          // 同步阻塞，所以放在子线程
            runOnUiThread(() -> {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                clearInputs();
                loadBooks();                  // 重新查库刷新，保证界面和数据一致
            });
        });
    }

    /**
     * 子线程查询 → 主线程刷新 ListView
     *
     * <p>注意：{@code listData} 是 Adapter 的数据源，必须"先 clear 再 add"然后
     * {@code notifyDataSetChanged()}，Adapter 才知道要重绘。
     */
    private void loadBooks() {
        executor.execute(() -> {
            // 1. 子线程查库（Room 的同步方法）
            List<BookInfo> books = bookDao.queryAll();

            // 2. 回到主线程操作 UI
            runOnUiThread(() -> {
                listData.clear();
                for (BookInfo b : books) {
                    // 每行数据装进 Map，key 必须与 initList() 里的 from 数组完全一致
                    Map<String, Object> row = new HashMap<>();
                    row.put("bookName", b.getBookName());
                    row.put("author", "作者：" + b.getAuthor());
                    row.put("publisher", "出版社：" + b.getPublisher());
                    row.put("pricePage", "价格：" + b.getPrice() + " 元   页数：" + b.getPageCount());
                    listData.add(row);
                }
                simpleAdapter.notifyDataSetChanged();   // 通知列表重绘
                tvBookCount.setText("已录入：" + books.size() + " 本");
            });
        });
    }

    /** 清空所有输入框，并把焦点移回第一个输入框（方便连续录入） */
    private void clearInputs() {
        etBookName.setText("");
        etAuthor.setText("");
        etPublisher.setText("");
        etPrice.setText("");
        etPageCount.setText("");
        etBookName.requestFocus();
    }

    /**
     * 页面销毁：关闭线程池
     *
     * <p>{@code shutdown()} 不再接受新任务，但已提交的任务会执行完；
     * 不像 {@code shutdownNow()} 那样直接中断线程。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
