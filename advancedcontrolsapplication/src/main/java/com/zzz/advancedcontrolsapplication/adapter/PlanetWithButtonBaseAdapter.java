package com.zzz.advancedcontrolsapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzz.advancedcontrolsapplication.R;
import com.zzz.advancedcontrolsapplication.pojo.Planet;

import java.util.List;

/**
 * 带 Button 的行适配器 —— 用来演示「行内子控件抢焦点，导致 ListView 的行点击失效」。
 *
 * <p>演示要点：
 * <ol>
 *   <li><b>现象</b>：行里放一个 Button（Button 默认 {@code clickable=true}、{@code focusable=true}）后，
 *       {@code listView.setOnItemClickListener()} 注册的行点击会失灵。</li>
 *   <li><b>两层原因要分开看</b>：
 *       <ul>
 *         <li>触摸事件分发层：子控件 clickable 时，落在它身上的触摸由它自己消费，
 *             事件不会再回传给 ListView，所以 ListView 根本没机会触发 onItemClick；</li>
 *         <li>焦点层：子控件 focusable 时会参与焦点竞争，抢走整行的焦点，
 *             导致"行的点击状态"立不起来。</li>
 *       </ul>
 *       本 demo 用 {@code descendantFocusability} 处理第二层（也是官方推荐的修法），
 *       第一层不用管——点按钮本来就该走按钮。</li>
 *   <li><b>修法两种写法，二选一</b>：
 *       <ul>
 *         <li>XML：在<b>行布局的根节点</b>写 {@code android:descendantFocusability="blocksDescendants"}；</li>
 *         <li>Java：ViewHolder 里多缓存一个「行根布局」的引用，
 *             {@code holder.itemRoot.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS)}。</li>
 *       </ul>
 *       本页用第二种，因为要在运行期通过 CheckBox 开关对比效果。
 *       ⚠️ 这个属性由<b>行布局根节点</b>设置，不是由 ListView 设置。</li>
 *   <li><b>三个取值的语义</b>：
 *       {@code FOCUS_BEFORE_DESCENDANTS}（默认，行自己先拿焦点）/
 *       {@code FOCUS_AFTER_DESCENDANTS}（先给子控件）/ {@code FOCUS_BLOCK_DESCENDANTS}（不许子控件拿焦点）。</li>
 * </ol>
 *
 * <p>对应布局：行布局 {@code res/layout/item_list_with_button.xml}；使用方 {@code ListFocusActivity}
 * <p>涉及 API：{@code ViewGroup#setDescendantFocusability}、{@code BaseAdapter#getView}
 */
public class PlanetWithButtonBaseAdapter extends BaseAdapter {

    /**
     * 行内按钮被点击的回调。
     *
     * <p>为什么用接口而不是在适配器里直接 Toast：适配器是「可复用的组件」，
     * 不该知道自己被放在哪个页面、更不该持有 Activity。把"点击之后干什么"交回给调用方，
     * 适配器才能被别的页面复用，也不会因为持有 Activity 而泄漏。
     */
    public interface OnItemButtonClickListener {
        /**
         * @param position 被点击的按钮所在行的下标
         */
        void onItemButtonClick(int position);
    }

    /** 用于 inflate 行布局与取主题 */
    private final Context context;

    /** 数据集 */
    private final List<Planet> planetList;

    /** 按钮点击回调，可以为 null（不需要时传 null 即可） */
    private final OnItemButtonClickListener buttonClickListener;

    /**
     * 是否阻止子控件抢焦点。
     * true → {@code FOCUS_BLOCK_DESCENDANTS}（行点击恢复正常）；
     * false → {@code FOCUS_BEFORE_DESCENDANTS}（默认值，复现问题）。
     */
    private boolean blockDescendants;

    /**
     * 构造函数。
     *
     * @param context               用于 inflate 行布局，一般传 Activity
     * @param planetList            数据集
     * @param buttonClickListener   行内按钮的点击回调，可传 {@code null}
     */
    public PlanetWithButtonBaseAdapter(Context context, List<Planet> planetList,
                                       OnItemButtonClickListener buttonClickListener) {
        this.context = context;
        this.planetList = planetList;
        this.buttonClickListener = buttonClickListener;
    }

    /**
     * 切换「是否阻止子控件抢焦点」。
     *
     * <p>只改字段还不够 —— 已经 inflate 出来的行视图不会自己重跑 getView，
     * 所以调用方改完这个开关后要再调一次 {@link #notifyDataSetChanged()}，让可见行重新绑定。
     *
     * @param blockDescendants true = 阻止（FOCUS_BLOCK_DESCENDANTS）
     */
    public void setBlockDescendants(boolean blockDescendants) {
        this.blockDescendants = blockDescendants;
    }

    /**
     * @return 数据条数；数据集为 null 时返回 0
     */
    @Override
    public int getCount() {
        return planetList == null ? 0 : planetList.size();
    }

    /**
     * @param position 行下标
     * @return 该行的行星对象（协变返回类型，调用方不必强转）
     */
    @Override
    public Planet getItem(int position) {
        return planetList.get(position);
    }

    /**
     * @param position 行下标
     * @return 行 id，等于 position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 造出（或复用）第 position 行的视图。
     *
     * <p>相比不含按钮的版本，这里多了两件事：
     * ① 每次都要按开关重设行根布局的 {@code descendantFocusability}；
     * ② 每次都要重设按钮的点击监听（原因见方法内注释）。
     *
     * @param position    行下标
     * @param convertView 被回收的旧行视图；首次显示时为 {@code null}
     * @param parent      父容器（ListView），用于让 inflate 正确解析 LayoutParams
     * @return 填好数据的行视图
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // attachToRoot 必须 false：ListView 要自己接管返回值
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_list_with_button, parent, false);

            holder = new ViewHolder();
            // ★ 多缓存一个「行根布局」的引用 —— 焦点抢占方式要设在它身上
            holder.itemRoot = convertView.findViewById(R.id.ll_item);
            holder.ivIcon = convertView.findViewById(R.id.iv_icon);
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvDesc = convertView.findViewById(R.id.tv_desc);
            holder.btn = convertView.findViewById(R.id.btn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // ★ 修法（Java 版）：把「子控件不许抢焦点」设在行根布局上。
        // 等价于 XML 里的 android:descendantFocusability="blocksDescendants"。
        // 放在 if 外面而不是 if 里面：复用的行也要跟着开关变，
        // 否则勾选后滑动列表会出现"新行正常、旧行还是老状态"的诡异现象。
        holder.itemRoot.setDescendantFocusability(blockDescendants
                ? ViewGroup.FOCUS_BLOCK_DESCENDANTS   // 子控件（Button）拿不到焦点，行点击恢复正常
                : ViewGroup.FOCUS_BEFORE_DESCENDANTS); // 默认值：行自己先处理焦点，问题复现

        // 按 position 取数据并整体覆盖（复用的行里装的还是上一行的内容）
        Planet planet = planetList.get(position);
        holder.ivIcon.setImageResource(planet.getIconResId());
        holder.tvTitle.setText(planet.getTitle());
        holder.tvDesc.setText(planet.getDescription());

        // ★ 按钮监听必须在每次 getView 里重设，不能写在 convertView == null 分支里。
        // 因为闭包捕获的是「当次调用时的 position」，而 convertView 会被复用给别的 position：
        // 若只在创建时设一次，复用到第 4 行后点按钮，回显的仍然是第 0 行的数据。
        holder.btn.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onItemButtonClick(position);
            }
        });

        return convertView;
    }

    /**
     * 行视图的控件缓存。
     *
     * <p>{@code static} 是为了不隐含外部适配器的 this$0 引用。
     */
    private static class ViewHolder {
        /**
         * 行布局根节点（ll_item）——焦点抢占方式设在它身上，不是设在 ListView 上。
         * 类型必须是 ViewGroup：setDescendantFocusability / getDescendantFocusability
         * 是 ViewGroup 才有的方法，声明成 View 会编译不过。
         */
        ViewGroup itemRoot;
        /** 图标，对应 Planet#getIconResId() */
        ImageView ivIcon;
        /** 主标题，对应 Planet#getTitle() */
        TextView tvTitle;
        /** 副标题，对应 Planet#getDescription() */
        TextView tvDesc;
        /** 行内按钮，演示"抢焦点 / 抢事件"的控件 */
        Button btn;
    }
}
