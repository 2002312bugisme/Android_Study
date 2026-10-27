package com.zzz.advancedcontrolsapplication;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.advancedcontrolsapplication.adapter.PlanetWithButtonBaseAdapter;
import com.zzz.advancedcontrolsapplication.pojo.Planet;

import java.util.List;

/**
 * 列表项焦点抢占实验页。
 *
 * <p>要观察的现象：行里有 Button 时，
 * <ul>
 *   <li>点按钮 → 走按钮自己的 OnClickListener（应为正常）；</li>
 *   <li>点行的文字 / 空白区域 → {@code OnItemClickListener} 能不能触发，取决于
 *       行根布局的 {@code descendantFocusability}。</li>
 * </ul>
 * 顶部 CheckBox 就是那个开关：勾上 = {@code FOCUS_BLOCK_DESCENDANTS}，取消 = 默认值。
 *
 * <p>对应布局：{@code res/layout/activity_list_focus.xml}、行布局 {@code res/layout/item_list_with_button.xml}
 * <p>涉及 API：{@code AdapterView.OnItemClickListener}、
 * {@code ViewGroup#setDescendantFocusability}、{@code BaseAdapter#notifyDataSetChanged}
 */
public class ListFocusActivity extends AppCompatActivity {

    /** 数据集，回调里按 position 取标题 */
    private List<Planet> planetList;

    /** 回显控件：用来区分"到底是行被点了还是按钮被点了" */
    private TextView tvResult;

    /** 适配器，CheckBox 切换时要通知它刷新 */
    private PlanetWithButtonBaseAdapter adapter;

    /**
     * Activity 创建入口（本页唯一覆写的方法）。
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
        setContentView(R.layout.activity_list_focus);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding（覆盖式 setPadding，所以视觉留白写在子容器上）
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvResult = findViewById(R.id.tv_result);
        ListView lvList = findViewById(R.id.lv_list);

        // 数据 + 适配器。第三个参数是「行内按钮被点击」的回调，
        // 用方法引用把回调实现写在 Activity 里，适配器不必知道页面的存在。
        planetList = Planet.getDefaultPlanets();
        adapter = new PlanetWithButtonBaseAdapter(this, planetList, this::onItemButtonClick);
        lvList.setAdapter(adapter);

        // ★ 本页的重点：行点击监听。
        // 行里有 Button 时，这个回调能不能触发，取决于行根布局的 descendantFocusability。
        lvList.setOnItemClickListener((parent, view, position, id) ->
                tvResult.setText("行被点击 → 第 " + (position + 1) + " 行："
                        + planetList.get(position).getTitle()));

        // 开关：是否阻止子控件抢焦点
        CheckBox cbBlockFocus = findViewById(R.id.cb_block_focus);
        cbBlockFocus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 改适配器里的开关
            adapter.setBlockDescendants(isChecked);
            // 已 inflate 的行不会自己重跑 getView，必须通知刷新才能让新设置生效
            adapter.notifyDataSetChanged();
            tvResult.setText(isChecked
                    ? "已设为 blocksDescendants：子控件不再抢焦点，点行应该能触发了"
                    : "已恢复默认 FOCUS_BEFORE_DESCENDANTS：点行可能没反应");
        });
    }

    /**
     * 行内按钮被点击时的回调（由适配器通过接口转发过来）。
     *
     * @param position 被点击按钮所在行的下标
     */
    private void onItemButtonClick(int position) {
        tvResult.setText("按钮被点击 → 第 " + (position + 1) + " 行："
                + planetList.get(position).getTitle());
    }
}
