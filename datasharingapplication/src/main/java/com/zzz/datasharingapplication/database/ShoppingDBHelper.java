package com.zzz.datasharingapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.zzz.datasharingapplication.pojo.Cart;
import com.zzz.datasharingapplication.pojo.Goods;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品 & 购物车数据库帮助器（SQLiteOpenHelper 单例）
 *
 * <p>演示要点：
 * 1. onCreate 只在数据库文件【第一次创建】时执行；onUpgrade 只在 versionCode(DB_VERSION) 变大时执行。
 * 2. 表名只写一处（TABLE_GOODS_INFO / TABLE_CART_INFO 常量），DDL 和增删改查全部引用常量，
 *    避免"建表叫 goods、插数据插 goods_info"这类漂移。
 * 3. 连接生命周期：mRDB / mWDB 用懒加载 + 自愈（dbRead() / dbWrite()）。
 *    任何人把连接关掉了（例如别的页面调了 closeLink()），下一次操作会自动重开，
 *    不会再出现"SQLiteDatabase 是 null → 点一下就闪退"。
 * 4. 批量插入用事务 + insertOrThrow：insert() 失败只返回 -1 不抛异常，容易被静默吞掉。
 *
 * <p>涉及 API：SQLiteOpenHelper / SQLiteDatabase / ContentValues / Cursor / SQLiteTransaction
 */
public class ShoppingDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "ShoppingDBHelper";

    /** 数据库文件名（存在 /data/data/<包名>/databases/ 下） */
    private static final String DB_NAME = "shopping.db";

    /** 商品信息表名 */
    private static final String TABLE_GOODS_INFO = "goods_info";

    /** 购物车信息表名 */
    private static final String TABLE_CART_INFO = "cart_info";

    /**
     * 数据库版本号。
     * v1：表名 goods / cart
     * v2：表名统一为 goods_info / cart_info（升版本让老库在 onUpgrade 里被重建）
     * 注意：以后每改一次表结构，这里必须 +1，否则老用户设备上的表结构不会更新。
     */
    private static final int DB_VERSION = 2;

    /** 单例对象；volatile 保证多线程下的可见性 */
    private static volatile ShoppingDBHelper shoppingDBHelper = null;

    /** 读连接（懒加载，null 表示还没打开或已被关闭） */
    private SQLiteDatabase mRDB = null;

    /** 写连接（懒加载，null 表示还没打开或已被关闭） */
    private SQLiteDatabase mWDB = null;

    /**
     * 私有构造：禁止外部 new，只能走 getInstance()
     *
     * @param context 任意 Context；内部统一换成 ApplicationContext，避免被 Activity 引用导致内存泄漏
     */
    private ShoppingDBHelper(Context context) {
        // super 的第二个参数是库名，第三个 CursorFactory 传 null 表示用系统默认实现
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    /**
     * 获取帮助器唯一实例（双重检查锁）
     *
     * @param context 任意 Context
     * @return 全进程唯一的 ShoppingDBHelper
     */
    public static ShoppingDBHelper getInstance(Context context) {
        if (shoppingDBHelper == null) {                       // 第一次判空：避免每次都加锁
            synchronized (ShoppingDBHelper.class) {           // 只让一个线程进临界区
                if (shoppingDBHelper == null) {               // 第二次判空：防止重复创建
                    shoppingDBHelper = new ShoppingDBHelper(context);
                }
            }
        }
        return shoppingDBHelper;
    }

    // ==================== 连接管理（自愈式） ====================

    /**
     * 取一个可用的读连接。
     * 连接被关掉（或被别人 close 过）时会自动重新打开——这是防 NPE 的关键。
     *
     * @return 一定非 null 的可读 SQLiteDatabase
     */
    private SQLiteDatabase dbRead() {
        if (mRDB == null || !mRDB.isOpen()) {   // 没打开 / 已经被关闭
            mRDB = getReadableDatabase();       // 交给 SQLiteOpenHelper 重新打开（它内部会处理已关闭的旧对象）
        }
        return mRDB;
    }

    /**
     * 取一个可用的写连接（同样自愈）。
     *
     * @return 一定非 null 的可写 SQLiteDatabase
     */
    private SQLiteDatabase dbWrite() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = getWritableDatabase();
        }
        return mWDB;
    }

    /**
     * 关闭连接。
     *
     * <p>注意：业务代码【不需要】主动调用它。SQLite 连接是进程级资源，
     * 由系统在进程结束时回收；Activity 里手动关反而会把别的页面正在用的连接一起关掉。
     * 保留这个方法只是为了测试或特殊清理场景，关掉后下次操作会自动重开。
     */
    public void closeLink() {
        if (mRDB != null && mRDB.isOpen()) {
            mRDB.close();
            mRDB = null;
        }
        if (mWDB != null && mWDB.isOpen()) {
            mWDB.close();
            mWDB = null;
        }
    }

    // ==================== 建表 / 升级 ====================

    /**
     * 数据库文件第一次创建时回调（只执行一次）
     *
     * @param db 系统传进来的、处于打开状态的数据库对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建商品信息表
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_GOODS_INFO + " ( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +   // 商品ID，自增主键（Android 约定主键叫 _id）
                "name TEXT NOT NULL, " +                     // 商品名称
                "price REAL NOT NULL, " +                    // 商品价格
                "stock INTEGER DEFAULT 0, " +                // 库存数量
                "description TEXT, " +                       // 商品描述
                "image_path TEXT" +                          // 图片路径（应用私有目录）
                ")";
        db.execSQL(sql);

        // 创建购物车信息表
        sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CART_INFO + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "goods_id INTEGER NOT NULL, " +              // 关联商品ID
                "goods_name TEXT, " +                        // 冗余存一份商品名，方便直接展示
                "quantity INTEGER DEFAULT 1, " +             // 数量
                "price REAL, " +                             // 下单时的单价
                "FOREIGN KEY(goods_id) REFERENCES " + TABLE_GOODS_INFO + "(_id)" +   // 外键必须指向 _id
                ")";
        db.execSQL(sql);
    }

    /**
     * 数据库版本号变大时回调（老用户升级 App 时）
     *
     * <p>学习项目直接"删表重建"最简单；真实产品必须写 ALTER TABLE 迁移，否则用户数据会丢。
     *
     * @param db         可写数据库
     * @param oldVersion 设备上原来的版本号
     * @param newVersion 本次要升到的版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade: " + oldVersion + " -> " + newVersion + "，重建表");
        // 先删新表名，再删 v1 遗留的旧表名，保证两条升级路径（v1→v2、v2→v3…）都能落到干净的表结构
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOODS_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART_INFO);
        db.execSQL("DROP TABLE IF EXISTS goods");
        db.execSQL("DROP TABLE IF EXISTS cart");
        onCreate(db);   // 建表逻辑复用 onCreate，避免两处维护
    }

    // ==================== 商品表：增 ====================

    /**
     * 批量插入商品信息（要么全部成功，要么全部回滚）
     *
     * @param list 待插入的商品集合，元素不能为 null
     * @return true=全部插入成功；false=插入失败并已回滚
     */
    public boolean insertGoodsInfo(List<Goods> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        SQLiteDatabase db = dbWrite();      // 自愈：即使之前被 closeLink() 关过，这里也会重新打开
        db.beginTransaction();              // 开启事务
        try {
            for (Goods goods : list) {
                ContentValues values = new ContentValues();          // 一行数据用 ContentValues 装
                values.put("name", goods.getName());                 // 商品名称
                values.put("price", goods.getPrice());               // 商品价格
                values.put("stock", goods.getStock());               // 库存数量
                values.put("description", goods.getDescription());   // 商品描述
                values.put("image_path", goods.getImagePath());      // 图片路径
                // 用 insertOrThrow：失败直接抛异常，能被下面的 catch 抓到并回滚；
                // 换成 insert 只会返回 -1 且不抛，事务照样提交 → 数据静默丢失
                db.insertOrThrow(TABLE_GOODS_INFO, null, values);
            }
            db.setTransactionSuccessful();   // 标记成功，endTransaction 时才真正提交
            return true;
        } catch (Exception e) {
            Log.e(TAG, "insertGoodsInfo 失败，事务已回滚", e);
            return false;
        } finally {
            db.endTransaction();             // 必须结束事务，否则会一直持锁、别人写不进来
        }
    }

    // ==================== 商品表：查 ====================

    /**
     * 查询全部商品
     *
     * @return 商品列表；表为空时返回空 List（不是 null）
     */
    public List<Goods> queryAllGoods() {
        List<Goods> list = new ArrayList<>();
        Cursor cursor = dbRead().rawQuery("SELECT * FROM " + TABLE_GOODS_INFO + " ORDER BY _id ASC", null);
        try {
            while (cursor.moveToNext()) {                        // 游标向下移动一行
                list.add(readGoods(cursor));                     // 把当前行读成一个 Goods
            }
        } finally {
            cursor.close();                                      // 必须关闭游标，避免内存/句柄泄漏
        }
        return list;
    }

    /**
     * 按主键查单个商品
     *
     * @param goodsId 商品ID（对应 _id 列）
     * @return 查不到返回 null
     */
    public Goods queryGoodById(int goodsId) {
        Goods good = null;
        Cursor cursor = dbRead().query(TABLE_GOODS_INFO, null,
                "_id=?",                                             // 主键列是 _id，不是 goods_id
                new String[]{String.valueOf(goodsId)},
                null, null, null);
        try {
            if (cursor.moveToFirst()) {
                good = readGoods(cursor);
            }
        } finally {
            cursor.close();
        }
        return good;
    }

    /**
     * 统计商品表行数。用于判断"商品数据到底初始化了没有"（比共享参数里的 firstOpen 更权威）
     *
     * @return 商品条数
     */
    public int queryGoodsCount() {
        Cursor cursor = dbRead().rawQuery("SELECT COUNT(*) FROM " + TABLE_GOODS_INFO, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    /**
     * 把一个游标当前行读成 Goods 对象（三处查询共用的解析逻辑）
     *
     * @param cursor 已 moveToXXX 到某一行的游标
     * @return 该行对应的商品
     */
    private Goods readGoods(Cursor cursor) {
        return new Goods(
                cursor.getInt(cursor.getColumnIndexOrThrow("_id")),            // 商品ID
                cursor.getString(cursor.getColumnIndexOrThrow("name")),        // 商品名称
                cursor.getDouble(cursor.getColumnIndexOrThrow("price")),       // 商品价格
                cursor.getInt(cursor.getColumnIndexOrThrow("stock")),          // 库存数量
                cursor.getString(cursor.getColumnIndexOrThrow("description")), // 商品描述
                cursor.getString(cursor.getColumnIndexOrThrow("image_path"))   // 图片路径
        );
    }

    // ==================== 购物车表：增 / 改 ====================

    /**
     * 加入购物车：已存在则数量 +1，不存在则新增一条
     *
     * @param goodsId 商品ID
     * @return true=成功；false=商品不存在或写库失败
     */
    public boolean insertCartInfo(int goodsId) {
        // 1. 先确认商品存在，顺手把名称、价格取出来（避免购物车里存脏数据）
        Goods good = queryGoodById(goodsId);
        if (good == null) {
            Log.w(TAG, "insertCartInfo: 商品不存在, goodsId=" + goodsId);
            return false;
        }

        // 2. 查购物车中是否已有该商品
        SQLiteDatabase db = dbWrite();
        boolean exists;
        int oldQuantity = 0;
        Cursor cursor = db.query(TABLE_CART_INFO, null,
                "goods_id=?", new String[]{String.valueOf(goodsId)},
                null, null, null);
        try {
            exists = cursor.moveToFirst();
            if (exists) {
                oldQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            }
        } finally {
            cursor.close();     // 先关游标再做写操作，避免读写互相等待
        }

        // 3. 组装要写的数据（存在则数量+1，不存在则数量=1）
        ContentValues values = new ContentValues();
        values.put("goods_id", good.getId());
        values.put("goods_name", good.getName());
        values.put("price", good.getPrice());
        values.put("quantity", oldQuantity + 1);

        if (exists) {
            // 4a. 已有记录 → 更新数量
            int rows = db.update(TABLE_CART_INFO, values, "goods_id=?", new String[]{String.valueOf(goodsId)});
            return rows > 0;
        } else {
            // 4b. 没有记录 → 插入新行
            long rowId = db.insert(TABLE_CART_INFO, null, values);
            return rowId != -1;
        }
    }

    /**
     * 统计购物车里的商品总件数（角标用）
     *
     * @return 总件数；购物车为空时返回 0
     */
    public int queryCartTotalCount() {
        // IFNULL 兜底：SUM() 对空表返回的是 NULL，直接 getInt 虽然也能得到 0，但语义上显式兜底更清楚
        Cursor cursor = dbRead().rawQuery("SELECT IFNULL(SUM(quantity), 0) FROM " + TABLE_CART_INFO, null);
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    // ==================== 购物车表：查列表 / 改 / 删 ====================

    /**
     * 查询购物车全部条目（联表带出商品图片路径，用于列表展示）
     *
     * <p>为什么要联表：购物车表里只存了 goods_id、名称、数量和单价，
     * 图片路径在商品表里，用 LEFT JOIN 一次性取回，避免在适配器里对每一行再查一次库（N+1 查询）。
     *
     * @return 购物车条目列表；空车时返回空 List
     */
    public List<Cart> queryAllCart() {
        // c 是购物车表别名，g 是商品表别名；字段名冲突时用 AS 起别名区分
        String sql = "SELECT c._id AS cart_id, c.goods_id, c.goods_name, c.quantity, c.price, g.image_path " +
                "FROM " + TABLE_CART_INFO + " c " +
                "LEFT JOIN " + TABLE_GOODS_INFO + " g ON g._id = c.goods_id " +
                "ORDER BY c._id ASC";
        List<Cart> list = new ArrayList<>();
        Cursor cursor = dbRead().rawQuery(sql, null);
        try {
            while (cursor.moveToNext()) {
                Cart cart = new Cart(
                        cursor.getInt(cursor.getColumnIndexOrThrow("cart_id")),        // 购物车主键
                        cursor.getInt(cursor.getColumnIndexOrThrow("goods_id")),       // 商品ID
                        cursor.getString(cursor.getColumnIndexOrThrow("goods_name")),  // 商品名
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),       // 数量
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price"))        // 单价
                );
                cart.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));  // 图片路径
                list.add(cart);
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    /**
     * 修改某条购物车记录的数量（数量 ≤ 0 时直接移除该记录）
     *
     * @param cartId   购物车记录主键（cart_info._id，不是 goods_id）
     * @param quantity 修改后的数量
     * @return true=改成功（或被移除）；false=失败
     */
    public boolean updateCartQuantity(int cartId, int quantity) {
        if (quantity <= 0) {
            return deleteCartInfo(cartId);      // 减到 0 = 移除，避免表里留下数量 0 的僵尸行
        }
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        int rows = dbWrite().update(TABLE_CART_INFO, values,
                "_id=?", new String[]{String.valueOf(cartId)});
        return rows > 0;
    }

    /**
     * 删除一条购物车记录
     *
     * @param cartId 购物车记录主键
     * @return true=确实删掉了一行
     */
    public boolean deleteCartInfo(int cartId) {
        int rows = dbWrite().delete(TABLE_CART_INFO,
                "_id=?", new String[]{String.valueOf(cartId)});
        return rows > 0;
    }

    /**
     * 清空购物车
     *
     * @return 被删除的行数
     */
    public int clearCartInfo() {
        // delete 的 whereClause 传 null 表示"删全表"，等价于 DELETE FROM cart_info
        return dbWrite().delete(TABLE_CART_INFO, null, null);
    }

    /**
     * 统计购物车总金额（数量 × 单价 求和）
     *
     * @return 总金额；空车时返回 0.0
     */
    public double queryCartTotalPrice() {
        Cursor cursor = dbRead().rawQuery(
                "SELECT IFNULL(SUM(quantity * price), 0) FROM " + TABLE_CART_INFO, null);
        try {
            return cursor.moveToFirst() ? cursor.getDouble(0) : 0.0;
        } finally {
            cursor.close();
        }
    }


}
