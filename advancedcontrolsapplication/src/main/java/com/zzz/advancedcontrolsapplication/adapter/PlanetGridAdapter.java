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
 * 行星网格适配器 —— 给 {@link android.widget.GridView} 用。
 *
 * <p>演示要点：
 * <ol>
 *   <li>GridView 和 ListView 是<b>兄弟</b>：都继承自 {@code AbsListView}，都用 {@code BaseAdapter}，
 *       {@code getView} 的写法一模一样。差别只在于父容器的排版方式——
 *       ListView 一行一个、铺满宽度；GridView 按「列数 / 列宽 / 间距 / 拉伸模式」把它们排成网格。</li>
 *   <li>所以适配器<b>不需要知道</b>自己会被排成几列 —— 列数、列宽、间距全是 GridView 的事。
 *       适配器只负责「造一个格子、填上内容」，这也是换了网格参数后代码一行不用改的原因。</li>
 *   <li>复用机制与 ListView 完全一致：{@code convertView} 判空 + ViewHolder。</li>
 * </ol>
 *
 * <p>对应布局：格子布局 {@code res/layout/item_grid.xml}；使用方 {@code GridViewActivity}
 * <p>涉及 API：{@code BaseAdapter}、{@code GridView#setAdapter}
 */
public class PlanetGridAdapter extends BaseAdapter {

    /** 用于 inflate 格子布局 */
    private final Context context;

    /** 数据集；每个元素 = 一个格子 */
    private final List<Planet> planetList;

    /**
     * @param context    一般传 Activity
     * @param planetList 数据集，通常来自 {@link Planet#getDefaultPlanets()}
     */
    public PlanetGridAdapter(Context context, List<Planet> planetList) {
        this.context = context;
        this.planetList = planetList;
    }

    /**
     * @return 格子总数 = 数据条数
     */
    @Override
    public int getCount() {
        return planetList == null ? 0 : planetList.size();
    }

    /**
     * @param position 格子下标
     * @return 该格子的行星对象（协变返回类型，调用方不必强转）
     */
    @Override
    public Planet getItem(int position) {
        return planetList.get(position);
    }

    /**
     * @param position 格子下标
     * @return 格子 id，等于 position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 造出（或复用）第 position 个格子的视图。
     *
     * <p>与 ListView 版本的 getView 结构完全相同：判空 inflate、ViewHolder 复用、按 position 赋值。
     * GridView 只会在「调用 getView 之前」把 parent 的列宽算好，从这里传进来。
     *
     * @param position    格子下标
     * @param convertView 被回收的旧格子视图；首次显示时为 {@code null}
     * @param parent      父容器（GridView）—— 传给 inflate 才能正确解析根节点的 LayoutParams
     * @return 填好内容的格子视图
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_grid, parent, false);

            holder = new ViewHolder();
            holder.ivIcon = convertView.findViewById(R.id.iv_icon);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Planet planet = planetList.get(position);
        holder.ivIcon.setImageResource(planet.getIconResId());

        // 格子里塞不下完整的 "水星 Mercury"，这里只取空格前的部分（中文名）显示。
        // 取的是标题的副本，数据源本身没被改动。
        holder.tvTitle.setText(planet.getTitle().split("\\s+")[0]);

        return convertView;
    }

    /**
     * 格子视图的控件缓存（static 是为了不隐含外部适配器的引用）。
     */
    private static class ViewHolder {
        /** 图标 */
        ImageView ivIcon;
        /** 名称 */
        TextView tvTitle;
    }
}
