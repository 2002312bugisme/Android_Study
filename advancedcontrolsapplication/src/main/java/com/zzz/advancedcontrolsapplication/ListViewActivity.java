package com.zzz.advancedcontrolsapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.advancedcontrolsapplication.adapter.PlanetBaseAdapter;
import com.zzz.advancedcontrolsapplication.pojo.Planet;
import com.zzz.advancedcontrolsapplication.util.ToastUtil;

import java.util.List;

/**
 * ListView 分隔线与按压背景实验页。
 *
 * <p>演示要点：
 * <ol>
 *   <li><b>两个样式都能在 XML 里静态配，也能在 Java 里动态改</b>：
 *       XML 对应 {@code android:divider} / {@code android:dividerHeight} / {@code android:listSelector}；
 *       Java 对应 {@code setDivider(Drawable)} / {@code setDividerHeight(int)} / {@code setSelector(Drawable)}。
 *       本页用 CheckBox 在运行期切换，正是为了让你看到这几个 setter 的效果。</li>
 *   <li><b>分隔线（divider）</b>本质是一张会被「横向拉满、纵向拉伸到 dividerHeight」的 drawable，
 *       所以用 {@code <shape>} 画个渐变矩形就够了；粗细完全由 dividerHeight 决定，
 *       想彻底去掉分隔线就 {@code setDividerHeight(0)} 或 {@code setDivider(null)}。</li>
 *   <li><b>按压背景（listSelector）</b>是「手指按住某一行时，铺在那行底下的一层 drawable」。
 *       必须写成 {@code <selector>} 并按状态给 item，否则这张图会常驻把每行都染色。
 *       另外 ListView 在触摸模式下没有 selected 状态，只认 pressed / focused。</li>
 *   <li><b>"未选中 = 默认状态"的正确做法是存快照，不是抄一份默认值</b>：
 *       在 onCreate 里先 {@code getDivider()} / {@code getDividerHeight()} / {@code getSelector()}
 *       把主题给的默认值读出来，取消勾选时原样塞回去。这样默认值来自当前主题（含深色模式），
 *       而不是写死一个"看起来像默认"的颜色。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_list_view.xml}
 * <p>自定义资源：{@code res/drawable/divider_custom.xml}（渐变分隔线）、
 * {@code res/drawable/list_selector_custom.xml}（按压色块）
 * <p>涉及 API：{@code ListView#setDivider}、{@code ListView#setDividerHeight}、
 * {@code ListView#setSelector}、{@code ListView#getDivider}、{@code ListView#getSelector}、
 * {@code CompoundButton.OnCheckedChangeListener}
 */
public class ListViewActivity extends AppCompatActivity {

    /** 自定义分隔线的粗细（dp）。默认主题的分隔线一般只有 1dp，这里故意做粗才看得出区别 */
    private static final int CUSTOM_DIVIDER_HEIGHT_DP = 12;

    /** 数据集，点击回调里要按 position 取标题 */
    private List<Planet> planetList;

    /** 列表控件，两个 CheckBox 的回调都要改它，所以提升为成员变量 */
    private ListView lvList;

    // ===== 默认样式快照：取消勾选时靠这三个值还原成主题原样 =====

    /** 主题给的默认分隔线 drawable（ListView 里通常来自 ?android:attr/listDivider） */
    private Drawable defaultDivider;

    /** 主题给的默认分隔线高度（单位 px；XML 里写的 dp 会被系统换算成 px） */
    private int defaultDividerHeight;

    /** 主题给的默认按压背景（Material3 下一般是个 ripple drawable） */
    private Drawable defaultSelector;

    /**
     * Activity 创建入口（本页唯一覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、装配列表，
     * 然后把两个 CheckBox 接到样式切换方法上。
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
        setContentView(R.layout.activity_list_view);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding（覆盖式 setPadding，所以视觉留白写在子容器上）
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 找到列表控件（后面两个回调里都要用，所以存成员变量）
        lvList = findViewById(R.id.lv_list);

        // 数据 + 适配器：复用上一页写好的行星适配器，本页只关心样式，不重复造数据
        planetList = Planet.getDefaultPlanets();
        lvList.setAdapter(new PlanetBaseAdapter(this, planetList));

        // ★ 关键顺序：在任何人改样式之前，先把主题给的默认值存下来。
        // 放到 setAdapter 之后、setDivider 之前都行，只要在所有修改之前即可。
        defaultDivider = lvList.getDivider();
        defaultDividerHeight = lvList.getDividerHeight();
        defaultSelector = lvList.getSelector();

        // 点击某一行弹个 Toast，顺便证明列表本身可点
        lvList.setOnItemClickListener((parent, view, position, id) ->
                ToastUtil.show(this, "你点击了：" + planetList.get(position).getTitle()));

        // 开关一：分隔线。形参 checked 就是勾选后的状态（去勾选时为 false）
        CheckBox cbCustomDivider = findViewById(R.id.cb_custom_divider);
        cbCustomDivider.setOnCheckedChangeListener((buttonView, isChecked) -> applyDivider(isChecked));

        // 开关二：按压背景
        CheckBox cbCustomSelector = findViewById(R.id.cb_custom_selector);
        cbCustomSelector.setOnCheckedChangeListener((buttonView, isChecked) -> applySelector(isChecked));

        // 跳到「列表项焦点抢占」实验页
        findViewById(R.id.btn_open_list_focus).setOnClickListener(
                v -> startActivity(new Intent(this, ListFocusActivity.class)));

        // 跳到「GridView 拉伸模式」实验页
        findViewById(R.id.btn_open_grid_view).setOnClickListener(
                v -> startActivity(new Intent(this, GridViewActivity.class)));

        // 跳到「ViewPager 轮播图」实验页（exported=false，同样只能应用内跳）
        findViewById(R.id.btn_open_view_pager).setOnClickListener(
                v -> startActivity(new Intent(this, ViewPagerActivity.class)));
    }

    /**
     * 切换分隔线样式。
     *
     * @param custom true = 换成自定义的渐变粗线；false = 还原成主题默认分隔线
     */
    private void applyDivider(boolean custom) {
        if (custom) {
            // setDivider 只决定「画什么」，粗细由 setDividerHeight 决定，两者要配套改
            lvList.setDivider(getDrawable(R.drawable.divider_custom));
            lvList.setDividerHeight(dp2px(CUSTOM_DIVIDER_HEIGHT_DP));
        } else {
            // 还原快照：drawable 与高度都要还原，只还原其中一个会出现「默认图 + 12dp 高」的怪样子
            lvList.setDivider(defaultDivider);
            lvList.setDividerHeight(defaultDividerHeight);
        }
    }

    /**
     * 切换按压背景（listSelector）。
     *
     * @param custom true = 换成自定义半透明色块；false = 还原成主题默认 selector
     */
    private void applySelector(boolean custom) {
        lvList.setSelector(custom
                ? getDrawable(R.drawable.list_selector_custom)
                : defaultSelector);
        // setSelector 只换引用，当前屏上已绘制的行不会自动重画，手动刷一次才立刻看到变化
        lvList.invalidate();
    }

    /**
     * dp 转 px。
     *
     * <p>setDividerHeight 的单位是<b>像素</b>，而设计稿/XML 里写的是 dp，
     * 2.0 倍屏上直接把 12 传进去会得到 12px（约 6dp），看起来只有设计的一半粗。
     *
     * @param dp dp 值
     * @return 换算后的像素值（四舍五入取整）
     */
    private int dp2px(int dp) {
        // density 就是「1dp 等于多少 px」，如 mdpi=1、xhdpi=2、xxhdpi=3
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
