package com.zzz.datasharingapplication.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.zzz.datasharingapplication.database.ShoppingDBHelper;
import com.zzz.datasharingapplication.pojo.Goods;
import com.zzz.datasharingapplication.util.FileUtil;
import com.zzz.datasharingapplication.util.SharedUtil;

import java.io.File;
import java.util.List;

/**
 * 全局 Application：进程启动时做一次性数据初始化 + 持有购物车件数的内存缓存
 *
 * <p>演示要点：
 * 1. Application.onCreate 早于任何 Activity，适合做"App 启动就要准备好"的初始化。
 * 2. 是否首次初始化，用【两个判据】共同决定：
 *    - 共享参数 firstOpen（语义：初始化成功过吗）
 *    - 数据库里商品表行数（语义：数据现在到底在不在）
 *    只信 firstOpen 会出事：一旦标志位被错误置为 false，数据就永远补不回来。
 * 3. 单例的 instance 由系统回调赋值，绝不能自己 new（new 出来的没有 base context）。
 * 4. <b>一个 APK 只能注册一个 Application</b>：清单里 android:name 只能写一个类。
 *    本模块既要 Room（BookApplication）又要商城初始化（ShopApplication），
 *    所以让 ShopApplication 【继承】BookApplication：
 *    super.onCreate() 里完成 Room 建库并设置 BookApplication.instance，
 *    这样 BookEntryActivity 里的 BookApplication.getBookDao() 依然可用，
 *    不必把两套逻辑复制一遍（继承 = 复用父类初始化 + 追加自己的）。
 *
 * <p>涉及 API：Application / getExternalFilesDir / BitmapFactory / SharedPreferences
 */
public class ShopApplication extends BookApplication {

    private static final String TAG = "ShopApplication";

    /** 共享参数里的"首次初始化完成"标记 */
    private static final String KEY_FIRST_OPEN = "firstOpen";

    /** 单例（volatile 保证多线程可见性） */
    private static volatile ShopApplication instance;

    /** 购物车商品总件数（内存缓存，供角标直接读取；真值始终以数据库为准） */
    private int cartCount;

    /**
     * 获取单例。instance 一定会在 onCreate 里被系统赋值，
     * 所以这里不再做"为 null 就 new 一个"的懒汉式创建（那样创建出来的对象没有 Context）。
     *
     * @return 全进程唯一的 ShopApplication
     */
    public static ShopApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;        // 系统回调里把自己赋值给单例
        initGoodsInfo();        // 初始化商品数据（只在需要时执行）
    }

    /**
     * 初始化商品数据：把资源图"模拟下载"到应用私有目录，再把商品信息写入数据库。
     *
     * <p>触发条件 = firstOpen 为 true（第一次装/清过数据） <b>或</b> 商品表为空（数据丢了/升版本被重建）。
     */
    private void initGoodsInfo() {
        ShoppingDBHelper helper = ShoppingDBHelper.getInstance(this);

        // 判据一：共享参数（用户清应用数据后，这个标记会一起被清掉，恢复成默认 true）
        boolean isFirstOpen = SharedUtil.getInstance(this).readBoolean(KEY_FIRST_OPEN, true);
        // 判据二：数据库真实数据。查库会顺带触发建表/升级，所以放在这里调用没问题
        int goodsCount = helper.queryGoodsCount();
        Log.d(TAG, "initGoodsInfo: isFirstOpen=" + isFirstOpen + ", goodsCount=" + goodsCount);

        if (!isFirstOpen && goodsCount > 0) {
            return;             // 已经初始化过且数据还在，直接结束
        }

        // 取应用私有下载目录：/sdcard/Android/data/<包名>/files/Download/
        // 该目录属于应用专属外部存储，Android 10+ 无需任何存储权限就能读写
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            // 外部存储不可用（极少见）时 dir 为 null，这里必须判空，否则下一行就 NPE，整个进程起不来
            Log.e(TAG, "initGoodsInfo: 外部存储不可用，跳过商品初始化");
            return;
        }
        String directory = dir.getAbsolutePath() + File.separator;

        // 模拟"从网络下载商品图片"：把 drawable 里的图解码后写到私有目录
        List<Goods> goods = Goods.buildGoodsList();
        try {
            for (int i = 0; i < goods.size(); i++) {
                Goods good = goods.get(i);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), good.getImageResId());
                if (bitmap == null) {
                    Log.w(TAG, "initGoodsInfo: 资源图解码失败, index=" + i);
                    continue;
                }
                String path = directory + "goods_" + i + ".png";   // 用下标命名，避免商品名里的特殊字符
                FileUtil.saveImage(path, bitmap);                    // 落盘
                bitmap.recycle();                                    // 及时回收，释放 native 内存
                good.setImagePath(path);                             // 回填路径，后面要写进数据库
            }
        } catch (Exception e) {
            // 图片没落盘成功就不要写库，否则库里存着指向不存在文件的路径，下次也补不回来
            Log.e(TAG, "initGoodsInfo: 图片落盘失败，本次跳过写库", e);
            return;
        }

        // 写库：全部成功才更新标记，保证"标记 == 数据真的进去了"
        if (helper.insertGoodsInfo(goods)) {
            SharedUtil.getInstance(this).writeBoolean(KEY_FIRST_OPEN, false);
            Log.d(TAG, "initGoodsInfo: 商品数据初始化完成, 共 " + goods.size() + " 条");
        } else {
            Log.e(TAG, "initGoodsInfo: 插入商品失败，保留 firstOpen=true 以便下次重试");
        }
    }

    /**
     * 购物车件数的内存缓存
     *
     * @return 上次从库里查出来的总件数
     */
    public int getCartCount() {
        return cartCount;
    }

    /**
     * 重新从数据库查询购物车总件数并刷新缓存
     *
     * <p>注意：这里【只读不关】——之前版本在这里调了 closeLink()，
     * 把 Activity 正在用的读写连接一起关掉了，导致点"加入"时 SQLiteDatabase 为 null 直接闪退。
     *
     * @return 最新的总件数
     */
    public int refreshCartCount() {
        cartCount = ShoppingDBHelper.getInstance(this).queryCartTotalCount();
        return cartCount;
    }
}
