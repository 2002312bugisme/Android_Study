package com.zzz.datasharingapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.zzz.datasharingapplication.application.ShopApplication;
import com.zzz.datasharingapplication.database.ShoppingDBHelper;
import com.zzz.datasharingapplication.pojo.Cart;
import com.zzz.datasharingapplication.pojo.Goods;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 购物车页面：条目列表（图/名称/单价/数量/小计）+ 加减数量 + 清空 + 结算 + 合计
 *
 * <p>演示要点：
 * 1. 顶部栏用 {@code <include layout="@layout/layout_top_bar"/>} 复用，
 *    include 之后组件内的 id（iv_back / tv_title / fl_cart / tv_cart_count）在宿主里照样能 findViewById。
 * 2. 列表用 ListView + BaseAdapter（本项目没有引入 RecyclerView 依赖），
 *    必须用 ViewHolder 模式复用 convertView，否则滚动时反复 findViewById 会卡。
 * 3. 数据的唯一真值永远在数据库：每次增删改之后都重新 queryAllCart() 再刷新，
 *    不在内存里自己维护数量，避免"界面和库里不一致"。
 * 4. 空态用 ListView.setEmptyView()，交给系统自动切换显示，不要自己手写 visibility 判断。
 *
 * <p>对应布局：activity_shopping_cart.xml（行布局 item_cart.xml，顶部栏 layout_top_bar.xml）
 */
public class ShoppingCartActivity extends AppCompatActivity {

    // ==================== 顶部栏（来自 include 的公共组件） ====================
    private ImageView ivBack;         // 返回键
    private TextView tvTitle;         // 标题
    private FrameLayout flCart;       // 右上角购物车入口
    private TextView tvCartCount;     // 顶部角标

    // ==================== 页面控件 ====================
    private ListView lvCart;          // 购物车列表
    private View llEmpty;             // 空态视图（列表为空时自动显示）
    private TextView tvTotalPrice;    // 合计金额
    private TextView btnClear;        // 清空
    private TextView btnBackList;     // 返回列表
    private TextView btnCheckout;     // 结算
    private TextView btnGoShopping;   // 空态里的"去逛逛"

    // ==================== 数据 ====================
    private ShoppingDBHelper shoppingDBHelper;
    private final List<Cart> cartList = new ArrayList<>();   // 当前列表数据（每次从库里重新查）
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        bindViews();
        shoppingDBHelper = ShoppingDBHelper.getInstance(this);

        // 标题由本页面决定（公共组件里的默认文字是"商品列表"）
        tvTitle.setText("购物车");

        // 列表 + 空态
        cartAdapter = new CartAdapter();
        lvCart.setAdapter(cartAdapter);
        lvCart.setEmptyView(llEmpty);   // ★ 一行搞定"空列表显示空态、有数据显示列表"

        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到前台都重新读库：从商品页加购后返回、或从后台切回来，都能拿到最新数据
        loadCart();
    }

    /** 绑定控件（include 进来的顶部栏组件同样可以按 id 取到） */
    private void bindViews() {
        ivBack        = findViewById(R.id.iv_back);
        tvTitle       = findViewById(R.id.tv_title);
        flCart        = findViewById(R.id.fl_cart);
        tvCartCount   = findViewById(R.id.tv_cart_count);

        lvCart        = findViewById(R.id.lv_cart);
        llEmpty       = findViewById(R.id.ll_empty);
        tvTotalPrice  = findViewById(R.id.tv_total_price);
        btnClear      = findViewById(R.id.btn_clear);
        btnBackList   = findViewById(R.id.btn_back_list);
        btnCheckout   = findViewById(R.id.btn_checkout);
        btnGoShopping = findViewById(R.id.btn_go_shopping);
    }

    /** 所有点击事件集中在这里，方便对照布局检查 */
    private void setListeners() {
        // 返回键 / 返回列表：都回到商品列表页
        ivBack.setOnClickListener(v -> finish());
        btnBackList.setOnClickListener(v -> finish());
        btnGoShopping.setOnClickListener(v -> finish());   // 空车时"去逛逛"也是回列表

        // 已经在购物车页面，点顶部购物车图标只做一次刷新，避免把用户绕进死循环
        flCart.setOnClickListener(v -> {
            loadCart();
            Toast.makeText(this, "已经在购物车页面啦", Toast.LENGTH_SHORT).show();
        });

        btnClear.setOnClickListener(v -> confirmClear());

        btnCheckout.setOnClickListener(v -> confirmCheckout());
    }

    // ==================== 数据加载与界面刷新 ====================

    /**
     * 从数据库重新读取购物车并刷新列表、合计、角标
     *
     * <p>所有"改数据"的操作最后都调用它，保证界面 == 数据库。
     */
    private void loadCart() {
        cartList.clear();
        cartList.addAll(shoppingDBHelper.queryAllCart());
        cartAdapter.notifyDataSetChanged();   // ListView 会顺带触发空态切换

        // 合计用 SQL 的 SUM 聚合算，不在内存里遍历累加，避免和库里不一致
        double total = shoppingDBHelper.queryCartTotalPrice();
        tvTotalPrice.setText(String.format(Locale.CHINA, "¥%.2f", total));

        // 顶部角标跟着变（Application 里缓存的值也一起更新，别的页面回来就能直接用）
        ShopApplication.getInstance().refreshCartCount();
        tvCartCount.setText(String.valueOf(ShopApplication.getInstance().getCartCount()));
    }

    // ==================== 数量加减 ====================

    /**
     * 点"+"：数量 +1
     *
     * @param cart 当前行对应的购物车条目
     */
    private void onPlusClick(Cart cart) {
        if (shoppingDBHelper.updateCartQuantity(cart.getId(), cart.getQuantity() + 1)) {
            loadCart();     // 改完重新读库刷新，这是最简单也最不容易出错的做法
        } else {
            Toast.makeText(this, "修改数量失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 点"−"：数量 -1；数量已经是 1 时再次点减号 = 移除该商品（数量减到 0 就删除）
     *
     * @param cart 当前行对应的购物车条目
     */
    private void onMinusClick(Cart cart) {
        if (cart.getQuantity() <= 1) {
            // 减到 0 就移除：直接删行，比在表里留一条 quantity=0 的脏数据干净
            if (shoppingDBHelper.deleteCartInfo(cart.getId())) {
                Toast.makeText(this, "已移除「" + cart.getGoodsName() + "」", Toast.LENGTH_SHORT).show();
                loadCart();
            }
        } else {
            if (shoppingDBHelper.updateCartQuantity(cart.getId(), cart.getQuantity() - 1)) {
                loadCart();
            } else {
                Toast.makeText(this, "修改数量失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==================== 清空 / 结算 ====================

    /** 清空购物车（破坏性操作，先弹确认框） */
    private void confirmClear() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "购物车已经是空的了", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("清空购物车")
                .setMessage("确定要移除全部 " + cartList.size() + " 种商品吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定清空", (dialog, which) -> {
                    int rows = shoppingDBHelper.clearCartInfo();
                    loadCart();
                    Toast.makeText(this, "已清空 " + rows + " 条记录", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    /** 结算（演示：确认后清空购物车；真实业务这里要生成订单、扣库存、走支付） */
    private void confirmCheckout() {
        if (cartList.isEmpty()) {
            Toast.makeText(this, "购物车是空的，先去挑点东西吧", Toast.LENGTH_SHORT).show();
            return;
        }
        double total = shoppingDBHelper.queryCartTotalPrice();
        int count = shoppingDBHelper.queryCartTotalCount();
        new AlertDialog.Builder(this)
                .setTitle("确认结算")
                .setMessage(String.format(Locale.CHINA, "共 %d 件商品，合计 ¥%.2f", count, total))
                .setNegativeButton("再看看", null)
                .setPositiveButton("确认支付", (dialog, which) -> {
                    // TODO: 真实业务要在这里 ① 生成订单 ② 扣减 goods_info.stock ③ 调起支付
                    shoppingDBHelper.clearCartInfo();
                    loadCart();
                    Toast.makeText(this, "结算成功，共支付 ¥" + String.format(Locale.CHINA, "%.2f", total),
                            Toast.LENGTH_LONG).show();
                })
                .show();
    }

    // ==================== 适配器 ====================

    /**
     * 购物车列表适配器（ListView + BaseAdapter）
     *
     * <p>ViewHolder 的作用：convertView 复用时不再重复 findViewById，
     * 这是 ListView 性能的关键，不写它滑动会明显掉帧。
     */
    private class CartAdapter extends BaseAdapter {

        private final LayoutInflater inflater = LayoutInflater.from(ShoppingCartActivity.this);

        @Override
        public int getCount() {
            return cartList.size();
        }

        @Override
        public Cart getItem(int position) {
            return cartList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return cartList.get(position).getId();   // 用主键做 id，语义更准确
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                // 没有可复用的条目 → 新 inflate 一个，并把子控件引用缓存进 tag
                convertView = inflater.inflate(R.layout.item_cart, parent, false);
                holder = new ViewHolder();
                holder.ivImage     = convertView.findViewById(R.id.iv_cart_image);
                holder.tvName      = convertView.findViewById(R.id.tv_cart_name);
                holder.tvUnitPrice = convertView.findViewById(R.id.tv_unit_price);
                holder.tvQty       = convertView.findViewById(R.id.tv_qty);
                holder.tvSubtotal  = convertView.findViewById(R.id.tv_subtotal);
                holder.btnMinus    = convertView.findViewById(R.id.btn_minus);
                holder.btnPlus     = convertView.findViewById(R.id.btn_plus);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();   // 复用旧条目，直接取缓存的引用
            }

            Cart cart = getItem(position);

            holder.tvName.setText(cart.getGoodsName());
            holder.tvUnitPrice.setText(String.format(Locale.CHINA, "¥%.2f", cart.getPrice()));
            holder.tvQty.setText(String.valueOf(cart.getQuantity()));
            holder.tvSubtotal.setText(String.format(Locale.CHINA, "¥%.2f", cart.getSubtotal()));
            loadCartImage(holder.ivImage, cart);

            // 注意：这里用 cart 对象（而不是 position）作为回调参数，
            // 因为点击发生时列表可能已经重新加载过，用 position 去取会拿到错的行
            holder.btnPlus.setOnClickListener(v -> onPlusClick(cart));
            holder.btnMinus.setOnClickListener(v -> onMinusClick(cart));

            return convertView;
        }
    }

    /** 列表行的子控件容器（配合 convertView.setTag 使用） */
    private static class ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvUnitPrice;
        TextView tvQty;
        TextView tvSubtotal;
        TextView btnMinus;
        TextView btnPlus;
    }

    /**
     * 加载行内商品图片：先读文件路径，读不到再用商品名反查资源图
     *
     * <p>主线程解码只适合这种小图 + 少量数据的演示；真实项目要放子线程 + 采样压缩。
     *
     * @param imageView 目标 ImageView
     * @param cart      购物车条目（imagePath 来自联表查询）
     */
    private void loadCartImage(ImageView imageView, Cart cart) {
        String path = cart.getImagePath();
        if (path != null && !path.trim().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        int resId = Goods.imageResIdOf(cart.getGoodsName());
        if (resId != 0) {
            imageView.setImageResource(resId);
        }
    }
}
