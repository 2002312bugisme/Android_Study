package com.zzz.advancedcontrolsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.List;

/**
 * 下拉列表（Spinner + ArrayAdapter）演示页，展开形式为 dropdown。
 *
 * <p>演示要点：
 * <ol>
 *   <li>Spinner 自己不知道有哪些选项，它只负责「展示 + 弹列表 + 上报选中项」；
 *       真正管数据的是 Adapter。这里用 {@link ArrayAdapter}——最简单的一种适配器，
 *       把数组/集合里的每一项原样塞进一个布局变成一行。</li>
 *   <li>ArrayAdapter 的两种数据来源：{@code createFromResource()} 读 {@code <string-array>} 资源；
 *       {@code new ArrayAdapter<>(...)} 直接吃内存里的 {@link List}。</li>
 *   <li>两套布局各管一头：{@code setDropDownViewResource()} 管「展开后的列表项」，
 *       构造时传入的 resource 管「折叠状态下 Spinner 自己显示的那一行」。</li>
 *   <li>{@code OnItemSelectedListener} 的 {@code onItemSelected} 在 setAdapter 之后会被
 *       <b>立刻回调一次</b>（position = 0），这不是用户点击，而是「当前选中项已确定」的通知。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_spinner_dropdown.xml}
 * <p>对应数据源：{@code res/values/arrays.xml}（{@code spinner_cities}）
 * <p>涉及 API：{@code ArrayAdapter#createFromResource}、{@code ArrayAdapter#setDropDownViewResource}、
 * {@code Spinner#setAdapter}、{@code Spinner#setOnItemSelectedListener}、
 * {@code AdapterView.OnItemSelectedListener}
 */
public class SpinnerDropdownActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（本页唯一覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距，然后分别给两个 Spinner
     * 装配数据源（Adapter）与选中监听器。
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
        setContentView(R.layout.activity_spinner_dropdown);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding（注意：这里是覆盖式 setPadding，所以留白写在子容器上）
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupSpinnerFromArrayResource();
        setupSpinnerFromList();

        // 跳到第二页：spinnerMode="dialog"。两页的装配代码完全相同，差别只在布局里那一个属性，
        // 放在一起对照才能看出"同一个控件的两种展开形态"。
        // 显式 Intent：目标是本应用内的 Activity 类，所以不需要 action / category 匹配
        findViewById(R.id.btn_open_dialog).setOnClickListener(
                v -> startActivity(new Intent(this, SpinnerDialogActivity.class)));

        // 跳到第三页：图文列表（ListView + SimpleAdapter）。同样显式 Intent + exported=false。
        findViewById(R.id.btn_open_simple_adapter).setOnClickListener(
                v -> startActivity(new Intent(this, SimpleAdapterActivity.class)));
    }

    /**
     * 第一组：数据源是 {@code res/values/arrays.xml} 里的字符串数组资源。
     *
     * <p>适合选项固定、需要多语言翻译的场景——把文案放进资源文件，
     * 换语言时改 XML 即可，Java 代码一行不动。
     */
    private void setupSpinnerFromArrayResource() {
        // 下拉列表控件
        Spinner spinnerFromArray = findViewById(R.id.spinner_from_array);
        // 结果回显控件，监听回调里要写它
        TextView tvResult = findViewById(R.id.tv_from_array_result);

        // createFromResource 三要素：
        //   形参1 context  —— 用来拿 Resources 和主题
        //   形参2 数组资源 id —— R.array.spinner_cities，即 arrays.xml 里那个 string-array
        //   形参3 单项布局 —— 必须是「只含一个 TextView」的布局，系统内置的 simple_spinner_item 正合适
        // 返回类型是 ArrayAdapter<CharSequence>：string-array 里每项都是 CharSequence，不是 String
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.spinner_cities, android.R.layout.simple_spinner_item);

        // 关键一步：指定「展开后的下拉列表」每一项用哪个布局。
        // 不调这句的话，展开列表会复用 spinner_item 布局 —— 行高偏大、左侧没有列表该有的内边距，
        // 看起来就像几个大字贴在弹层里。simple_spinner_dropdown_item 是系统给的下拉专用版式。
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 把 Adapter 交给 Spinner。setAdapter 之后控件立刻根据数据渲染出折叠态的当前项，
        // 同时会触发一次 onItemSelected(position = 0)，见下方监听器说明。
        spinnerFromArray.setAdapter(arrayAdapter);

        // 选中监听：既可以用匿名内部类，也可以让 Activity implements AdapterView.OnItemSelectedListener；
        // 本页有两个 Spinner，用匿名内部类能让每个回调只关心自己的控件，不必在回调里按 id 分支。
        spinnerFromArray.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * 某一项被选中时回调（用户点击，或代码调用 setSelection / setAdapter 引起的选中变化）。
             *
             * @param parent   触发回调的 AdapterView 本体，这里是 spinner_from_array；
             *                 类型写成父类 AdapterView<?> 是因为该接口被 ListView、GridView 等共用。
             * @param view     被选中那一行的行 View；dropdown 模式下用户点完弹层就消失了，
             *                 这个引用随后失效，所以不要把它缓存起来用。
             * @param position 选中项在数据源中的下标（从 0 开始）。
             * @param id       该行的 row id，ArrayAdapter 里恒等于 position。
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // getItemAtPosition 返回数据源里那一项（这里是 CharSequence），转成 String 再拼接。
                // 也可以写 arrayAdapter.getItem(position)，两者结果相同。
                String city = parent.getItemAtPosition(position).toString();
                // 一行结果回显：核心 API 调用的结论就落在这句上
                tvResult.setText("选中的城市：" + city);
            }

            /**
             * 选中项被清空时回调。
             *
             * <p>dropdown 模式下没有"取消选择"入口，只要数据源非空，这个方法几乎不会被调用；
             * 它更多是为 {@code spinnerMode="dialog"} 或数据源被清空这类边界情况留的。
             * 接口要求必须实现，空实现即可。
             *
             * @param parent 触发回调的 AdapterView 本体
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 无选中项时不做处理
            }
        });
    }

    /**
     * 第二组：数据源是 Java 内存集合。
     *
     * <p>适合选项在运行时才能确定（从数据库 / 网络 / 用户输入拼出来）的场景。
     */
    private void setupSpinnerFromList() {
        Spinner spinnerFromList = findViewById(R.id.spinner_from_list);
        TextView tvResult = findViewById(R.id.tv_from_list_result);

        // 运行时构造的选项列表。注意 ArrayAdapter 是「持有引用」而不是拷贝：
        // 之后再往这个 List 里增删元素，必须调 adapter.notifyDataSetChanged() 让 Spinner 重新渲染。
        List<String> plans = Arrays.asList("标准套餐", "经济套餐", "豪华套餐");

        // 这个构造函数同样三要素：context、折叠态单项布局、数据集合。
        // 与 createFromResource 的区别只在于第三参是 List 而不是资源 id。
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, plans);

        // 同样要给展开列表指定版式，否则下拉那几行会和折叠态共用简单布局，观感不一致
        listAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 装配到 Spinner，并挂选中监听
        spinnerFromList.setAdapter(listAdapter);
        spinnerFromList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * 选中回调，参数含义同第一组。
             *
             * @param parent   触发回调的 AdapterView（这里是 spinner_from_list）
             * @param view     被选中那一行的行 View（弹层关闭后失效）
             * @param position 选中项下标
             * @param id       行 id，ArrayAdapter 下等于 position
             */
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, @Nullable View view,
                                       int position, long id) {
                String plan = parent.getItemAtPosition(position).toString();
                tvResult.setText("选中的套餐：" + plan);
            }

            /**
             * 选中项被清空时回调，理由同第一组，空实现。
             *
             * @param parent 触发回调的 AdapterView 本体
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 无选中项时不做处理
            }
        });
    }
}
