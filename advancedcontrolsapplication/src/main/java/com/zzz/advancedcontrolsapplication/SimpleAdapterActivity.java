package com.zzz.advancedcontrolsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图文列表（ListView + SimpleAdapter）演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li>行与行共用同一个布局。只要每行内容都能拆成「几张图 + 几段字」的固定格子，
 *       就不必像 {@code BaseAdapter} 那样自己写 {@code getView()} 去复用 convertView——
 *       {@link SimpleAdapter} 内部已经把「创建行视图 / 复用旧行 / 按 key 填值」全做完了。</li>
 *   <li>它的数据集是 {@code List<Map<String, Object>>}：外层 {@link List} 的每个元素 = 一行，
 *       里层 {@link Map} = 这一行的字段集合，key 是自己起的字符串（这里用常量避免拼错）。</li>
 *   <li>装配三要素：{@code resource} 是行布局，{@code from} 是 Map 的 key 数组，
 *       {@code to} 是行布局里目标控件的 id 数组。<b>from 与 to 必须顺序一一对应</b>，
 *       写反了不会报错，只会把文字塞进图片、图片塞进文字——表现为「空白」或崩溃。</li>
 *   <li>图片能直接把 {@code R.drawable.xxx}（int）塞进 Map 生效，是因为 bindView 遇到
 *       {@link android.widget.ImageView} 时走的是 {@code setViewImage()}：先把值 {@code toString()}，
 *       再 {@code Integer.parseInt()}，解析成功就 {@code setImageResource()}。
 *       所以 Bitmap、文件路径这类「转不成数字」的值，得用 {@code setViewBinder()} 自己写几行绑定逻辑。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_simple_adapter.xml}（页面本体）、
 * {@code res/layout/item_simple_adapter.xml}（行布局）
 * <p>对应图片：{@code res/drawable/ic_item_person.xml} / {@code ic_item_star.xml} /
 * {@code ic_item_diamond.xml} / {@code ic_item_triangle.xml} / {@code ic_item_plus.xml}
 * <p>涉及 API：{@code SimpleAdapter#SimpleAdapter}、{@code SimpleAdapter#setViewBinder}、
 * {@code SimpleAdapter#getItem}、{@code ListView#setAdapter}、{@code AdapterView.OnItemClickListener}
 */
public class SimpleAdapterActivity extends AppCompatActivity {

    /**
     * Map 的 key：一行里的三个字段。
     * 抽成常量是为了让 {@code from} 数组与 {@code buildRows()} 里的 put 用同一份字符串，
     * 避免一处写成 "icon"、另一处写成 "Icon" 这种静默失败（key 对不上时 SimpleAdapter 不抛异常，
     * 只是取到 null，控件显示为空）。
     */
    private static final String KEY_ICON = "icon";   // 图片：值放 R.drawable.xxx 的 int
    private static final String KEY_NAME = "name";   // 主标题：String
    private static final String KEY_DESC = "desc";   // 副标题：String

    /**
     * Activity 创建入口（本页唯一覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距，然后把数据集装配给 ListView。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口 / 主题等基础初始化
        super.onCreate(savedInstanceState);
        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_simple_adapter);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding（覆盖式 setPadding，所以视觉留白写在子容器上）
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListView();

        // 跳到 BaseAdapter 版：同一份列表，换成自己继承 BaseAdapter 写的适配器。
        // 两页并排跑一遍，才能直观看出「框架替你做了什么」。
        findViewById(R.id.btn_open_base_adapter).setOnClickListener(
                v -> startActivity(new Intent(this, BaseAdapterActivity.class)));
    }

    /**
     * 把数据集装配给 ListView，并挂上点击监听。
     *
     * <p>四步固定顺序：造数据 → new SimpleAdapter → setAdapter → 监听。前三步任一步缺了，
     * 列表都是空白；监听与数据无关，只是拿到点击事件。
     */
    private void setupListView() {
        // 列表控件本体
        ListView listView = findViewById(R.id.list_view);
        // 底部回显控件，点击回调里要写它
        TextView tvResult = findViewById(R.id.tv_result);

        // 第一步：数据。外层 List 有几个元素就有几行，顺序即显示顺序
        List<Map<String, Object>> rows = buildRows();

        // 第二步：造适配器。五个构造形参依次是：
        //   1) context  —— 用来拿 Resources / 主题，把资源 id 真正解析成文字与图片
        //   2) data     —— List<Map<String, Object>>，一行一个 Map
        //   3) resource —— 行布局 id，即每个 Map 被渲染成的那个 View
        //   4) from     —— 从 Map 里按这些 key 取值
        //   5) to       —— 把取到的值塞进行布局里这些 id 的控件（与 from 逐位对齐）
        // 注意 Resource 里必须能 findViewById 到 to 中的每个 id，否则那一格静默为空。
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                rows,
                R.layout.item_simple_adapter,
                new String[]{KEY_ICON, KEY_NAME, KEY_DESC},
                new int[]{R.id.iv_icon, R.id.tv_name, R.id.tv_desc});

        // 第三步：把适配器交给 ListView。setAdapter 之后列表立刻按数据渲染，
        // 屏幕外的行会在滑动时由 SimpleAdapter 内部复用 convertView 现场填充。
        listView.setAdapter(adapter);

        // 第四步：点击监听（ListView 作为 AdapterView 的一种，用它通用的 OnItemClickListener）
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * 某一行被点击时回调。
             *
             * @param parent   触发回调的 AdapterView 本体，这里是 list_view
             * @param view     被点击那一行的行 View；想改行内某个控件的样式可以对它 findViewById
             * @param position 该行在数据源中的下标（从 0 开始）
             * @param id       该行的 row id；SimpleAdapter 内部恒等于 position
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // getItemAtPosition 的签名是 Object（AdapterView 并不知道数据是 Map），
                // 所以我们自己强转回 Map。转成 Map<?, ?> 而不是 Map<String, Object>，
                // 是因为带具体泛型的强转属于「未检查转换」，会多一条编译告警；取 key 用 Object 做参数，不需要具体类型。
                Map<?, ?> row = (Map<?, ?>) parent.getItemAtPosition(position);
                // 一行结果回显：核心 API 调用的结论就落在这句上
                tvResult.setText("点击了第 " + (position + 1) + " 项：" + row.get(KEY_NAME));
            }
        });
    }

    /**
     * 构造演示数据：5 行，每行一个图标 + 标题 + 描述。
     *
     * @return 可直接交给 SimpleAdapter 的数据集；外层 List 数量 = 列表行数
     */
    private List<Map<String, Object>> buildRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        // 数据实际来源可以是数据库 / 网络，这里为了聚焦适配器用法直接写死
        rows.add(newRow(R.drawable.ic_item_person, "个人中心", "查看与编辑个人资料"));
        rows.add(newRow(R.drawable.ic_item_star, "我的收藏", "已收藏 12 项内容"));
        rows.add(newRow(R.drawable.ic_item_diamond, "会员特权", "专享折扣与积分兑换"));
        rows.add(newRow(R.drawable.ic_item_triangle, "安全中心", "账号与密码设置"));
        rows.add(newRow(R.drawable.ic_item_plus, "添加快捷入口", "新建自定义功能入口"));
        return rows;
    }

    /**
     * 拼一个「行 Map」——把三个字段按约定的 key 放进去。
     *
     * @param iconResId 图标资源 id，如 {@code R.drawable.ic_item_person}；
     *                  作为 int 放进 Map 后，SimpleAdapter 会把它当资源 id 解析（见类头第 4 条）
     * @param name      主标题文字，对应行布局里的 tv_name
     * @param desc      副标题文字，对应行布局里的 tv_desc
     * @return 一个包含三个字段的 Map，直接 add 进数据集即可成为一行
     */
    private Map<String, Object> newRow(int iconResId, String name, String desc) {
        Map<String, Object> row = new HashMap<>();
        // 值类型是 Object，所以 int 会被自动装箱成 Integer —— 这正是 setViewImage 能 parseInt 成功的前提
        row.put(KEY_ICON, iconResId);
        row.put(KEY_NAME, name);
        row.put(KEY_DESC, desc);
        return row;
    }
}
