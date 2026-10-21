package com.zzz.datasharingapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zzz.datasharingapplication.application.ShopApplication;
import com.zzz.datasharingapplication.database.ShoppingDBHelper;
import com.zzz.datasharingapplication.pojo.Goods;

import java.util.Locale;

/**
 * 商品详情页：根据 Intent 传来的商品 ID 查库，把数据绑定到控件上
 *
 * <p>演示要点（也是这次踩坑的三个点）：
 * 1. <b>setContentView 必须指向本页面的布局</b>。
 *    原来这里写的是 {@code R.layout.activity_shopping_cart}（购物车页布局），
 *    于是详情页的 iv_ProductImage / tv_Price / tv_Description / btn_AddToCart 全部 findViewById 到 null，
 *    下一行 setImageBitmap() 就 NPE 闪退。
 *    id 找不到时 findViewById 不会报错、只返回 null，所以这类错误一定是在"用"的时候才炸。
 * 2. <b>取 Intent 参数要用 getIntExtra(key, 默认值)</b>，不要 getExtras().getInt(key)：
 *    没有携带 extra 时 getExtras() 返回 null，直接 .getInt() 就是 NPE。
 * 3. <b>queryGoodById() 可能返回 null</b>（id 不存在 / 商品被删），
 *    拿到结果先判空再绑定，否则 goods.getPrice() 又是 NPE。
 *
 * <p>对应布局：activity_shopping_good_detail.xml（顶部栏来自 layout_top_bar.xml）
 */
public class ShoppingGoodDetailActivity extends AppCompatActivity {

    /**
     * Intent 里传递商品 ID 用的 key。
     * 抽成 public 常量，让"发 Intent 的地方"和"收 Intent 的地方"共用同一个字符串，
     * 避免两边各写一遍、拼错一个字母就查不到数据（而且不报错，只是 goods 为 null）。
     */
    public static final String EXTRA_GOODS_ID = "goods_id";

    /** 参数非法/商品不存在时的兜底 id（_id 自增从 1 开始，-1 一定查不到） */
    private static final int INVALID_GOODS_ID = -1;

    // ==================== 顶部栏（来自 include 的公共组件） ====================
    private ImageView ivBack;         // 返回键
    private TextView tvTitle;         // 顶部栏标题
    private FrameLayout flCart;       // 右上角购物车入口
    private TextView tvCartCount;     // 顶部角标

    // ==================== 内容区控件 ====================
    private ImageView ivProductImage;   // 商品大图
    private TextView tvPrice;           // 价格
    private TextView tvGoodsName;       // 商品名称（布局里 id 是 tv_Title）
    private TextView tvDescription;     // 商品描述
    private Button btnAddToCart;        // 加入购物车

    // ==================== 数据 ====================
    private ShoppingDBHelper shoppingDBHelper;
    private int goodsId = INVALID_GOODS_ID;   // 从 Intent 取到的商品ID
    private Goods goods;                       // 查库得到的商品对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ★ 修复点①：这里原来写成了 R.layout.activity_shopping_cart（购物车页布局）
        //   用错布局 → 详情页所有 id 都取不到 → ivProductImage 为 null → 后面 setImageBitmap 直接 NPE
        setContentView(R.layout.activity_shopping_good_detail);

        bindViews();
        shoppingDBHelper = ShoppingDBHelper.getInstance(this);

        // ★ 修复点②：getExtras() 在"没有携带任何 extra"时返回 null，必须用带默认值的 getIntExtra
        goodsId = getIntent().getIntExtra(EXTRA_GOODS_ID, INVALID_GOODS_ID);
        if (goodsId == INVALID_GOODS_ID) {
            // 参数没传进来（比如调试时用 am start 直接拉起），友好退出，不要带着非法 id 去查库
            Toast.makeText(this, "缺少商品参数，无法打开详情", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 标题由本页面决定（公共组件里的默认文字是"商品列表"）
        tvTitle.setText("商品详情");

        // ★ 修复点③：查不到商品就直接结束，不要继续走绑定流程
        if (!bindGoodsInfo()) {
            return;
        }

        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 角标要以数据库为准：每次回到前台重新查一次，否则从详情页回到列表页时数字可能是旧的
        ShopApplication.getInstance().refreshCartCount();
        updateCartCount();
    }

    /**
     * 绑定控件（include 进来的顶部栏组件同样可以按 id 取到）
     *
     * <p>注意：这里的 findViewById 全部对应 activity_shopping_good_detail.xml 里的 id，
     * 一旦 setContentView 用错布局，这里拿到的就都是 null（而且不会报错）。
     */
    private void bindViews() {
        // 内容区
        ivProductImage = findViewById(R.id.iv_ProductImage);
        tvPrice        = findViewById(R.id.tv_Price);
        tvGoodsName    = findViewById(R.id.tv_Title);        // 布局里商品名称用的是 tv_Title
        tvDescription  = findViewById(R.id.tv_Description);
        btnAddToCart   = findViewById(R.id.btn_AddToCart);

        // 顶部栏
        ivBack      = findViewById(R.id.iv_back);
        tvTitle     = findViewById(R.id.tv_title);
        flCart      = findViewById(R.id.fl_cart);
        tvCartCount = findViewById(R.id.tv_cart_count);
    }

    /**
     * 根据 goodsId 查库并把数据绑定到控件上
     *
     * @return true=查到并绑定完成；false=商品不存在，调用方应停止后续流程
     */
    private boolean bindGoodsInfo() {
        goods = shoppingDBHelper.queryGoodById(goodsId);   // 查不到时返回 null
        if (goods == null) {
            Toast.makeText(this, "商品不存在或已下架", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        loadGoodsImage(ivProductImage, goods);
        tvGoodsName.setText(goods.getName());
        // 显式指定 Locale：默认 Locale 在部分语言环境下会把小数点显示成逗号
        tvPrice.setText(String.format(Locale.CHINA, "¥%.2f", goods.getPrice()));
        tvDescription.setText(goods.getDescription());
        return true;
    }

    /**
     * 所有点击事件集中在这里，方便对照布局检查
     *
     * <p>调用时机很关键：必须在 bindGoodsInfo() 成功之后调用，
     * 因为下面的 lambda 用到了 goods，goods 为 null 时点一下按钮就会 NPE。
     */
    private void setListeners() {
        // 返回键：回到上一个页面
        ivBack.setOnClickListener(v -> finish());

        // 顶部购物车图标：跳到购物车页
        flCart.setOnClickListener(v -> startActivity(
                new Intent(ShoppingGoodDetailActivity.this, ShoppingCartActivity.class)));

        // 加入购物车：这里 goods 一定非 null（setListeners 只在 bindGoodsInfo 成功后调用）
        btnAddToCart.setOnClickListener(v -> addToCart(goods.getId(), goods.getName()));
    }

    /**
     * 加入购物车：写库 → 重查角标 → 提示
     *
     * @param goodsId 商品ID
     * @param name    商品名称（仅用于 Toast 文案）
     */
    private void addToCart(int goodsId, String name) {
        boolean success = shoppingDBHelper.insertCartInfo(goodsId);
        if (success) {
            ShopApplication.getInstance().refreshCartCount();   // 写完后重新查库，拿最新总件数
            updateCartCount();
            Toast.makeText(this, "已将「" + name + "」加入购物车", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "加入失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 加载商品图片：优先读私有目录里的图片文件，读不到再回退 drawable 资源图
     *
     * <p>注意：这里在主线程解码图片，只适合这种小图演示；真实项目应放子线程 + 采样压缩。
     *
     * @param imageView 目标 ImageView（不能为 null，否则 setImageBitmap 直接 NPE）
     * @param goods     商品数据（imagePath 来自数据库，imageResId 仅内存有值）
     */
    private void loadGoodsImage(ImageView imageView, Goods goods) {
        String path = goods.getImagePath();
        if (path != null && !path.trim().isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);   // 文件不存在时返回 null
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        }
        // 回退：从数据库查回来的对象 imageResId 恒为 0，必须按商品名反查资源 id
        int resId = Goods.imageResIdOf(goods.getName());
        if (resId != 0) {
            imageView.setImageResource(resId);
        }
    }

    /** 把 Application 里缓存的购物车件数画到角标上（数据来源唯一，不做二次存储） */
    private void updateCartCount() {
        tvCartCount.setText(String.valueOf(ShopApplication.getInstance().getCartCount()));
    }
}
