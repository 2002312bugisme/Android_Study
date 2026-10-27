package com.zzz.advancedcontrolsapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzz.advancedcontrolsapplication.R;
import com.zzz.advancedcontrolsapplication.pojo.Planet;

import java.util.List;

/**
 * 行星列表适配器 —— 自己继承 {@link BaseAdapter} 写的那种。
 *
 * <p>演示要点：
 * <ol>
 *   <li><b>BaseAdapter 是「什么都不做」的基类</b>：它只定义接口，没有默认实现。
 *       相比 {@link android.widget.ArrayAdapter}（专管单一文本）和
 *       {@link android.widget.SimpleAdapter}（专管「几个格子 + Map 数据」），
 *       BaseAdapter 不预设行布局、不预设数据形状，所以什么都能做，代价是四个方法都得自己写。</li>
 *   <li><b>四个方法的分工</b>：
 *       {@link #getCount()} 决定行数（返回 0 列表就是空白）；
 *       {@link #getItem(int)} / {@link #getItemId(int)} 是给 AdapterView 回调用；
 *       真正的重头戏是 {@link #getView(int, View, ViewGroup)}——「这一行长什么样」全在它里面。</li>
 *   <li><b>getView 的三件事</b>：① 拿到（或复用）行视图 → ② 按 position 取数据 →
 *       ③ 把数据塞进行视图里的控件。</li>
 *   <li><b>convertView 复用</b>：ListView 只为「屏幕上看得见的几行 + 缓冲」创建视图，
 *       滑出去的行会被回收成 convertView 传回来。所以 {@code convertView == null} 才真的 inflate，
 *       否则直接复用——不判这个空，滑动时每行都新建，GC 压力大、列表会卡。</li>
 *   <li><b>ViewHolder</b>：复用行视图时，如果每次都 {@code findViewById}，等于每行每帧都在做
 *       遍历查找。把控件引用存进一个静态小对象、用 {@code setTag()} 挂在行视图上，
 *       下一次复用直接取出来。这是 ListView 时代的标准写法
 *       （现在更推荐 RecyclerView，它对 ViewHolder 是强制的）。</li>
 * </ol>
 *
 * <p>对应布局：行布局 {@code res/layout/item_base_adapter.xml}；
 * 使用方 {@code BaseAdapterActivity}
 * <p>涉及 API：{@code BaseAdapter#getCount/getItem/getItemId/getView}、
 * {@code LayoutInflater#inflate}、{@code View#setTag/getTag}
 */
public class PlanetBaseAdapter extends BaseAdapter {

    /**
     * 用于取 LayoutInflater 和主题。
     * 存的是外部传进来的 Context —— 在 Activity 里 new 本适配器时通常传 Activity 本身，
     * 只要适配器生命周期不比 Activity 长就没问题；若适配器要长期持有，应改用 getApplicationContext()。
     */
    private final Context context;

    /**
     * 数据集。每个元素 = 列表的一行；顺序即显示顺序。final：本 demo 不整份替换，只读
     */
    private final List<Planet> planetList;

    /**
     * 唯一构造函数。
     *
     * @param context    用于 inflate 行布局，一般传 Activity
     * @param planetList 数据集，通常来自 {@link Planet#getDefaultPlanets()}；
     *                   传 null 会导致 getCount 返回 0（列表空白），不会崩
     */
    public PlanetBaseAdapter(Context context, List<Planet> planetList) {
        this.context = context;
        this.planetList = planetList;
    }

    /**
     * 列表一共有几行。ListView 靠它决定滚动范围。
     *
     * @return 数据条数；数据为 null 时返回 0（列表显示为空白，而不是抛 NPE）
     */
    @Override
    public int getCount() {
        return planetList == null ? 0 : planetList.size();
    }

    /**
     * 取第 position 行的数据。
     *
     * <p>注意返回类型写的是 {@link Planet} 而不是 {@code Object} —— 这是 Java 的<b>协变返回类型</b>，
     * 覆写时可以把返回值收窄。好处是调用方（Activity 的点击回调）直接拿到 Planet，
     * 不用再 {@code (Planet) parent.getItemAtPosition(pos)} 强转。
     *
     * @param position 行下标，从 0 开始
     * @return 该行对应的行星对象
     */
    @Override
    public Planet getItem(int position) {
        return planetList.get(position);
    }

    /**
     * 取第 position 行的 id。
     *
     * <p>返回值会作为 {@code AdapterView.OnItemClickListener#onItemClick} 的第四个参数
     * {@code id} 传出来。这里直接返回 position 就够了；只有配合
     * {@code setHasStableIds(true)}（数据增删时希望 id 保持不变）时才需要返回真正的唯一 id。
     *
     * @param position 行下标
     * @return 行 id，这里等于 position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 造出（或复用）第 position 行的视图。整个适配器唯一真正干活的方法。
     *
     * <p>标准套路：{@code convertView} 为空就 inflate 一个新行并把控件引用塞进 ViewHolder；
     * 不为空就直接取出旧的 ViewHolder。然后无论新旧，都重新按 position 赋一次值
     * ——因为复用的行视图里装的还是上一行的数据。
     *
     * @param position    要渲染的行下标
     * @param convertView 被回收的旧行视图；第一次显示某行时为 {@code null}
     * @param parent      行视图的父容器（就是 ListView）。
     *                    必须传给 inflate —— 否则行布局根节点的 layout_width/layout_height
     *                    会失效（没有父容器就无从解析 LayoutParams）
     * @return 填好数据的行视图，交给 ListView 显示
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 复用容器：把「行视图里那三个控件的引用」打包，避免每次 findViewById
        ViewHolder holder;

        if (convertView == null) {
            // 第一次显示这一屏的行：inflate 一份新布局。
            // inflate 三个形参：① 布局 id ② 父容器（只用来算 LayoutParams）
            //                  ③ attachToRoot —— 必须 false，否则新视图会被立刻塞进 ListView，
            //                     而 ListView 需要自己接管返回值，会抛 IllegalStateException
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_base_adapter, parent, false);

            // 行视图刚建好，此时才需要 findViewById 找一遍控件
            holder = new ViewHolder();
            holder.ivIcon = convertView.findViewById(R.id.iv_icon);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvDesc = convertView.findViewById(R.id.tv_desc);

            // 把 holder 挂到行视图上，下次这行被复用时直接从 tag 里取
            // 将 视图持有者 保存到 转换视图上
            convertView.setTag(holder);
        } else {
            // 复用的旧行：控件引用早在 setTag 时存好了，不需要再 findViewById
            holder = (ViewHolder) convertView.getTag();
        }

        // 按 position 取当前行的数据，重新填一遍
        // （复用的行里装的还是上一行的文字/图片，必须整体覆盖）
        Planet planet = planetList.get(position);
        holder.ivIcon.setImageResource(planet.getIconResId());
        holder.tvTitle.setText(planet.getTitle());
        holder.tvDesc.setText(planet.getDescription());

        return convertView;
    }

    /**
     * 行视图的控件缓存。
     *
     * <p>为什么是 {@code static}：ViewHolder 不持有外部适配器的引用，
     * 做成静态类才不会让每个实例都隐含一个 this$0 指针（既省内存，也避免无意间延长外部对象生命周期）。
     */
    private static class ViewHolder {
        /**
         * 图标控件，对应 Planet#getIconResId()
         */
        ImageView ivIcon;
        /**
         * 主标题控件，对应 Planet#getTitle()
         */
        TextView tvTitle;
        /**
         * 副标题控件，对应 Planet#getDescription()
         */
        TextView tvDesc;
    }
}
