package com.zzz.advancedcontrolsapplication;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.advancedcontrolsapplication.adapter.PlanetGridAdapter;
import com.zzz.advancedcontrolsapplication.pojo.Planet;

/**
 * GridView 五种属性实验页：用下拉列表切换 stretchMode，看剩余空间被怎么处理。
 *
 * <p>演示要点：
 * <ol>
 *   <li><b>五个关键属性</b>（XML / Java 一一对应）：
 *       <pre>
 *       android:numColumns         setNumColumns(int)          —— 列数；写 AUTO_FIT(-1) 则按列宽自动算能放几列
 *       android:columnWidth        setColumnWidth(int)         —— 每列宽度，单位 px（Java 里要自己 dp→px）
 *       android:horizontalSpacing  setHorizontalSpacing(int)   —— 列与列的横向缝隙
 *       android:verticalSpacing    setVerticalSpacing(int)     —— 行与行的纵向缝隙
 *       android:stretchMode        setStretchMode(int)         —— 剩余空间怎么处理
 *       </pre>
 *       注意后四个 setter 的单位都是<b>像素</b>，XML 里写 dp 会被资源系统换算，Java 里得自己换算。</li>
 *   <li><b>四种 stretchMode</b>（本页切换的就是这一项）：
 *       <ul>
 *         <li>{@code NO_STRETCH}：不拉伸，列宽就是 columnWidth，剩余空间全留在右边；</li>
 *         <li>{@code STRETCH_COLUMN_WIDTH}：拉宽<b>列</b>把空隙吃掉，列间距保持 horizontalSpacing 不变；</li>
 *         <li>{@code STRETCH_SPACING}：列宽不变，剩余空间均分到<b>列与列之间</b>（两侧边缘不加）；</li>
 *         <li>{@code STRETCH_SPACING_UNIFORM}：列宽不变，剩余空间连<b>两侧边缘</b>一起均分，
 *             所以左右两边也会出现留白。</li>
 *       </ul>
 *       后两种模式不改列宽，所以 columnWidth 必须给定（否则列宽只能由内容决定，效果随数据变）。</li>
 *   <li><b>切换时到底改了谁</b>：界面上那行实测数值会把「格子宽度 / 列间距 / 左侧留白」打出来，
 *       一眼就能看出 columnWidth 模式改的是格子宽，另外两种改的是间距。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_grid_view.xml}、格子布局 {@code res/layout/item_grid.xml}
 * <p>涉及 API：{@code GridView#setStretchMode}、{@code GridView#setNumColumns}、
 * {@code GridView#setColumnWidth}、{@code GridView#setHorizontalSpacing}、{@code GridView#setVerticalSpacing}、
 * {@code AdapterView.OnItemSelectedListener}
 */
public class GridViewActivity extends AppCompatActivity {

    /**
     * 四种拉伸模式，顺序与 {@code R.array.grid_stretch_modes} 逐位对应。
     * GridView 里它们实际是 int 常量：0/1/2/3。
     */
    private static final int[] STRETCH_MODES = {
            GridView.NO_STRETCH,              // 0 → none
            GridView.STRETCH_COLUMN_WIDTH,    // 1 → columnWidth
            GridView.STRETCH_SPACING,         // 2 → spacingWidth
            GridView.STRETCH_SPACING_UNIFORM  // 3 → spacingWidthUniform
    };

    /** 网格控件，切换模式要改它 */
    private GridView gvPlanet;

    /** 实测数值回显 */
    private TextView tvMeasure;

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
        setContentView(R.layout.activity_grid_view);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding（覆盖式 setPadding，所以视觉留白写在子容器上）
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gvPlanet = findViewById(R.id.gv_planet);
        tvMeasure = findViewById(R.id.tv_measure);

        // 列数 / 列宽 / 横纵间距都已在 XML 里配好，这里只装数据。
        // 适配器不关心排几列 —— 那是 GridView 的事。
        gvPlanet.setAdapter(new PlanetGridAdapter(this, Planet.getDefaultPlanets()));

        setupSpinner();
    }

    /**
     * 装配「拉伸模式」下拉列表，并挂上选中监听。
     *
     * <p>数据源用 {@code createFromResource} 读 {@code res/values/arrays.xml} 里的字符串数组，
     * 与之前 Spinner 页同一条链路；下拉行用系统的 simple_spinner_dropdown_item。
     */
    private void setupSpinner() {
        Spinner spStretchMode = findViewById(R.id.sp_stretch_mode);

        // 形参：context + 数组资源 id + 折叠态单项布局
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.grid_stretch_modes, android.R.layout.simple_spinner_item);
        // 展开后列表项的版式（不设的话下拉列表会复用折叠态布局，观感不对）
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStretchMode.setAdapter(adapter);

        spStretchMode.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            /**
             * 选中某一项时回调。注意 setAdapter 之后会<b>立刻回调一次</b>（position = 0），
             * 所以初始状态也是由这里设置的，onCreate 里不必再单独设一遍。
             *
             * @param parent   触发回调的 AdapterView（这里是 sp_stretch_mode）
             * @param view     选中那一行的行 View（弹层关闭后失效）
             * @param position 选项下标，与 STRETCH_MODES 逐位对应
             * @param id       行 id，ArrayAdapter 下等于 position
             */
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                       int position, long id) {
                applyStretchMode(STRETCH_MODES[position]);
            }

            /**
             * 选中项被清空时回调。dropdown 模式下基本不会触发，空实现即可。
             */
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // 不做处理
            }
        });
    }

    /**
     * 应用某个拉伸模式，并重新测量一次布局后回显数值。
     *
     * @param stretchMode GridView 的拉伸模式常量（NO_STRETCH 等）
     */
    private void applyStretchMode(int stretchMode) {
        // setStretchMode 内部在"值发生变化"时会 requestLayout，
        // 下一帧 GridView 会按新模式重新计算列宽与间距。
        gvPlanet.setStretchMode(stretchMode);

        // 强制再走一次布局：只调 setStretchMode 时，若值没变（重复选同一项）不会触发重排；
        // 显式 requestLayout 能保证每次都刷新，代价可以忽略。
        gvPlanet.requestLayout();

        // 布局要等下一帧才完成，此刻读 getChildAt(0).getWidth() 拿到的还是旧值。
        // 用一次性的 OnGlobalLayoutListener 等到"这一帧布局结束"再读，才是最准的。
        gvPlanet.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // 只测一次就摘掉，否则每次布局都会重复回调
                        gvPlanet.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        showMeasure(stretchMode);
                    }
                });
    }

    /**
     * 把当前布局的实测数值打到界面上。
     *
     * <p>三个数各自代表什么：
     * <ul>
     *   <li>格子宽度 = {@code getChildAt(0).getWidth()}，即一列的实际宽度；</li>
     *   <li>列间距 = 第 2 格左边界 - 第 1 格右边界，即 horizontalSpacing 被拉伸后的实际值；</li>
     *   <li>左侧留白 = 第 1 格左边界，STRETCH_SPACING_UNIFORM 下会明显大于 0。</li>
     * </ul>
     *
     * @param stretchMode 当前模式（用来在文案里标出模式名）
     */
    private void showMeasure(int stretchMode) {
        if (gvPlanet.getChildCount() < 2) {
            // 数据太少或还没布局完，读不到相邻两格
            return;
        }
        View first = gvPlanet.getChildAt(0);
        View second = gvPlanet.getChildAt(1);

        int cellWidth = first.getWidth();
        int spacing = second.getLeft() - first.getRight();
        int leftMargin = first.getLeft();

        // 三目链把 int 常量翻译成可读名字，方便对照下拉里的选项
        String modeName = stretchMode == GridView.NO_STRETCH ? "none"
                : stretchMode == GridView.STRETCH_COLUMN_WIDTH ? "columnWidth"
                : stretchMode == GridView.STRETCH_SPACING ? "spacingWidth"
                : "spacingWidthUniform";

        tvMeasure.setText("当前 stretchMode = " + modeName
                + "\n格子宽度 " + cellWidth + "px"
                + "　列间距 " + spacing + "px"
                + "　左侧留白 " + leftMargin + "px");
    }
}
