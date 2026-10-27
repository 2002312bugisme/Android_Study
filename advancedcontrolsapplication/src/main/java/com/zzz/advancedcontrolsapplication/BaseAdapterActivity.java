package com.zzz.advancedcontrolsapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.advancedcontrolsapplication.adapter.PlanetBaseAdapter;
import com.zzz.advancedcontrolsapplication.pojo.Planet;

import java.util.List;

/**
 * 图文列表（ListView + 自建 BaseAdapter）演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li>本页与 {@code SimpleAdapterActivity} 的装配代码几乎一样，只有「new 出来的适配器」不同：
 *       那边是框架给的 {@code SimpleAdapter}，这边是自己写的 {@link PlanetBaseAdapter}。
 *       这就是 Adapter 模式的意义——ListView 只依赖 {@code Adapter} 接口，
 *       谁来填行、怎么填，随你换。</li>
 *   <li>数据从 {@link Planet#getDefaultPlanets()} 拿，是 {@code List<Planet>} 而不是
 *       {@code List<Map<String, Object>>}，字段有类型、有名字，编译期就能查错。</li>
 *   <li>点击回调里取值不用强转：{@code PlanetBaseAdapter#getItem} 的返回值已经收窄成
 *       {@code Planet}，直接 {@code adapter.getItem(position)} 即可。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_base_adapter.xml}（页面本体）、
 * {@code res/layout/item_base_adapter.xml}（行布局）
 * <p>涉及 API：{@code BaseAdapter}、{@code ListView#setAdapter}、
 * {@code AdapterView.OnItemClickListener}
 */
public class BaseAdapterActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_base_adapter);
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
    }

    /**
     * 把数据集装配给 ListView，并挂上点击监听。
     *
     * <p>三步固定顺序：造数据 → new 适配器 → setAdapter。前两步顺序可换，
     * 但 setAdapter 必须在适配器准备好之后；监听与数据无关，随时挂都行。
     */
    private void setupListView() {
        // 列表控件本体
        ListView listView = findViewById(R.id.list_view);
        // 底部回显控件，点击回调里要写它
        TextView tvResult = findViewById(R.id.tv_result);

        // 第一步：数据。List 有几个元素，列表就有几行
        List<Planet> planets = Planet.getDefaultPlanets();

        // 第二步：造适配器。BaseAdapter 的构造参数没有框架约束，完全由自己定义 ——
        // 这里收了 context（getView 里 inflate 要用）和数据集
        PlanetBaseAdapter adapter = new PlanetBaseAdapter(this, planets);

        // 第三步：把适配器交给 ListView。setAdapter 之后列表立刻按 getCount 的行数渲染，
        // 每一行的样子由 adapter.getView() 决定
        listView.setAdapter(adapter);

        // 第四步：点击监听。ListView 是 AdapterView 的一种，用通用的 OnItemClickListener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * 某一行被点击时回调。
             *
             * @param parent   触发回调的 AdapterView 本体，这里是 list_view
             * @param view     被点击那一行的行 View
             * @param position 该行在数据源中的下标（从 0 开始）
             * @param id       该行的 row id，由 adapter.getItemId(position) 提供，这里等于 position
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 直接用适配器的 getItem(position)：返回类型已经是 Planet，不需要强转。
                // 若写成 (Planet) parent.getItemAtPosition(position) 也能跑
                // （getItemAtPosition 内部就是调 adapter.getItem），只是多了一次强转。
                Planet planet = adapter.getItem(position);
                // 一行结果回显：核心 API 调用的结论就落在这句上
                tvResult.setText("点击了第 " + (position + 1) + " 项：" + planet.getTitle());
            }
        });
    }
}
