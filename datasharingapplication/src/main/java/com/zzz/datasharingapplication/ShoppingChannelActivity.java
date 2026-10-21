package com.zzz.datasharingapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zzz.datasharingapplication.application.ShopApplication;
import com.zzz.datasharingapplication.database.ShoppingDBHelper;
import com.zzz.datasharingapplication.pojo.Goods;

import java.util.List;
import java.util.Locale;

/**
 * 商品列表页：展示商品、点击"加入"写入购物车表、刷新购物车角标
 *
 * <p>演示要点：
 * 1. 数据库连接由 ShoppingDBHelper 自己管理（懒加载 + 自愈），Activity 不要手动开关连接，
 *    否则会把别的页面正在用的同一个连接关掉。
 * 2. 角标数据只有一个权威来源：数据库。读取走 ShopApplication.refreshCartCount()，
 *    Activity 只负责把值画到 TextView 上，不再自己另存一份。
 * 3. onCreate 之后一定会走 onResume，所以只在 onResume 里刷新一次即可，不要在 onCreate 里重复查库。
 *
 * <p>对应布局：activity_shopping_channel.xml（列表项 item_goods.xml）
 */
public class ShoppingChannelActivity extends AppCompatActivity {

    // ==================== 控件 ====================
    private ImageView ivBack;         // 左上返回
    private FrameLayout flCart;       // 右上购物车容器（含角标）
    private ImageView ivCart;         // 购物车图标
    private TextView tvCartCount;     // 购物车角标数字
    private GridLayout glGoods;       // 商品网格容器

    // ==================== 数据 ====================
    private ShoppingDBHelper shoppingDBHelper;   // 数据库帮助器（进程级单例）

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_channel);

        // 1. 绑定控件
        ivBack      = findViewById(R.id.iv_back);
        flCart      = findViewById(R.id.fl_cart);
        ivCart      = findViewById(R.id.iv_cart);
        tvCartCount = findViewById(R.id.tv_cart_count);
        glGoods     = findViewById(R.id.gl_goods);

        // 2. 拿到数据库帮助器。
        //    这里不调 openReadLink()/openWriteLink() 了：helper 内部每个方法都会自己确保连接可用，
        //    Activity 手动 open/close 只会让连接的归属变混乱（曾经就是因为别处 close 了，这里点一下就 NPE）
        shoppingDBHelper = ShoppingDBHelper.getInstance(this);

        // 3. 返回键
        ivBack.setOnClickListener(v -> finish());

        // 4. 购物车点击：跳转购物车页
        flCart.setOnClickListener(v -> {
            // 显式 Intent：目标类写死在代码里，不会被别的应用的同名 action 抢走
            Intent intent = new Intent(ShoppingChannelActivity.this, ShoppingCartActivity.class);
            // CLEAR_TOP 在这里没有意义（购物车页在返回栈里还不存在），去掉，避免以后误解
            startActivity(intent);
        });

        // 5. 渲染商品列表
        showGoods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到前台都刷一次角标：从数据库取真值 → 更新 Application 缓存 → 画到 TextView
        ShopApplication.getInstance().refreshCartCount();
        updateCartCount();
    }

    /** 把 Application 里缓存的购物车件数画到角标上（数据来源唯一，不做二次存储） */
    private void updateCartCount() {
        tvCartCount.setText(String.valueOf(ShopApplication.getInstance().getCartCount()));
    }

    /** 从数据库查出全部商品并渲染到 GridLayout */
    private void showGoods() {
        List<Goods> goodsList = shoppingDBHelper.queryAllGoods();
        glGoods.removeAllViews();       // 先清空，避免重复渲染

        if (goodsList.isEmpty()) {
            Toast.makeText(this, "商品数据为空", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Goods goods : goodsList) {
            // inflate(布局, 父容器, false)：先按父容器的 LayoutParams 生成 View，但不立刻 addView
            View view = LayoutInflater.from(this).inflate(R.layout.item_goods, glGoods, false);

            ImageView ivGoodsImage = view.findViewById(R.id.iv_goods_image);
            TextView tvGoodsName   = view.findViewById(R.id.tv_goods_name);
            TextView tvGoodsDesc   = view.findViewById(R.id.tv_goods_desc);
            TextView tvGoodsPrice  = view.findViewById(R.id.tv_goods_price);
            TextView tvAddCart     = view.findViewById(R.id.tv_add_cart);

            tvGoodsName.setText(goods.getName());
            tvGoodsDesc.setText(goods.getDescription());
            // 显式指定 Locale：默认 Locale 在部分语言环境下会把小数点显示成逗号
            tvGoodsPrice.setText(String.format(Locale.CHINA, "¥%.2f", goods.getPrice()));
            loadGoodsImage(ivGoodsImage, goods);

            // 用商品 id 作为唯一标识去加购，不依赖列表下标
            tvAddCart.setOnClickListener(v -> addToCart(goods.getId(), goods.getName()));

            // 图片 / 名称 / 描述 点击都进同一个详情页。
            // 三处行为完全一样，就共用一个 OnClickListener + 一个方法，
            // 不要写三遍同样的 Intent（以前是三段复制粘贴，改个参数名要改三处）
            View.OnClickListener toDetail = v -> openGoodsDetail(goods.getId());
            ivGoodsImage.setOnClickListener(toDetail);
            tvGoodsName.setOnClickListener(toDetail);
            tvGoodsDesc.setOnClickListener(toDetail);

            glGoods.addView(view);
        }
    }

    /**
     * 打开商品详情页，并把商品ID带过去
     *
     * <p>用显式 Intent（写死目标类），key 用对方类里的常量 {@link ShoppingGoodDetailActivity#EXTRA_GOODS_ID}，
     * 这样"发"和"收"用的是同一个字符串，不会因为拼错而查不到数据。
     *
     * @param goodsId 商品ID（对应 goods_info._id）
     */
    private void openGoodsDetail(int goodsId) {
        Intent intent = new Intent(ShoppingChannelActivity.this, ShoppingGoodDetailActivity.class);
        intent.putExtra(ShoppingGoodDetailActivity.EXTRA_GOODS_ID, goodsId);
        startActivity(intent);
    }

    /**
     * 加载商品图片：优先读私有目录里的图片文件，读不到再回退 drawable 资源图
     *
     * <p>注意：这里在主线程解码图片，只适合这种小图 + 5 条数据的演示场景；
     * 真实项目应放到子线程并做采样压缩（BitmapFactory.Options.inSampleSize）。
     *
     * @param imageView 目标 ImageView
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

            // TODO: 库存校验（stock 不足时阻止加入，并扣减库存）
            // TODO: 通知购物车页面刷新（若已打开）
            // TODO: 埋点统计"加入购物车"事件
            // TODO: 购物车图标/角标加入动画
        } else {
            Toast.makeText(this, "加入失败", Toast.LENGTH_SHORT).show();
        }
    }
}