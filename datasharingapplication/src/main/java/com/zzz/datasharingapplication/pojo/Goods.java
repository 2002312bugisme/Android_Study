package com.zzz.datasharingapplication.pojo;

import com.zzz.datasharingapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品实体：对应 SQLite 表 {@code goods_info} 的一行
 *
 * <p>演示要点：
 * 1. 字段分两类：<b>落库字段</b>（id / name / price / stock / description / imagePath）
 *    与<b>仅内存字段</b>（imageResId）。后者不写进数据库，因为资源 id 是编译期生成的
 *    int，App 一旦重新打包就可能变，存进数据库毫无意义；
 * 2. 因此"从数据库查回来的 Goods" 其 {@code imageResId} 恒为 0，
 *    想按资源显示图片时必须用 {@link #imageResIdOf(String)} 按商品名反查；
 * 3. {@link #buildGoodsList()} 用几个<b>平行数组</b>装配出 5 条演示数据，
 *    是"模拟服务端返回商品列表"的替身。
 *
 * <p>相关文件：{@code database/ShoppingDBHelper}（建表与增删改查）、
 * {@code ShoppingChannelActivity}（列表页）、{@code ShoppingGoodDetailActivity}（详情页）
 */
public class Goods {

    private int id;              // 商品ID（数据库自增主键，对应 _id 列）
    private String name;         // 商品名称
    private double price;        // 商品价格（元）
    private int stock;           // 库存数量
    private String description;  // 商品描述
    private String imagePath;    // 图片路径（存库）：私有目录下 goods_x.png 的绝对路径
    private int imageResId;      // 图片资源编号（不存库，仅内存用）

    /**
     * 无参构造：留给"先 new、再逐字段 set"的场景（例如由 Cursor 逐列填充）
     */
    public Goods() {
    }

    /**
     * 全参构造（不含 id 和 imageResId）：用于新增落库
     *
     * @param name        商品名称
     * @param price       商品价格
     * @param stock       库存数量
     * @param description 商品描述
     * @param imagePath   图片在私有目录中的绝对路径
     */
    public Goods(String name, double price, int stock,
                 String description, String imagePath) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imagePath = imagePath;
    }

    /**
     * 全参构造（含 id）：用于查询回显
     *
     * @param id          商品ID（数据库 _id）
     * @param name        商品名称
     * @param price       商品价格
     * @param stock       库存数量
     * @param description 商品描述
     * @param imagePath   图片绝对路径
     */
    public Goods(int id, String name, double price, int stock,
                 String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imagePath = imagePath;
    }

    /** @return 商品ID（数据库 _id） */
    public int getId() {
        return id;
    }

    /** @param id 商品ID */
    public void setId(int id) {
        this.id = id;
    }

    /** @return 商品名称 */
    public String getName() {
        return name;
    }

    /** @param name 商品名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 商品价格 */
    public double getPrice() {
        return price;
    }

    /** @param price 商品价格 */
    public void setPrice(double price) {
        this.price = price;
    }

    /** @return 库存数量 */
    public int getStock() {
        return stock;
    }

    /** @param stock 库存数量 */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /** @return 商品描述 */
    public String getDescription() {
        return description;
    }

    /** @param description 商品描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 图片绝对路径；可能为 null 或空串（尚未"下载"） */
    public String getImagePath() {
        return imagePath;
    }

    /** @param imagePath 图片绝对路径 */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /** @return 图片资源 id（仅内存构建的列表里有值，从数据库查回来的是 0） */
    public int getImageResId() {
        return imageResId;
    }

    /** @param imageResId 图片资源 id，例如 R.drawable.goods_1 */
    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    /**
     * 输出全部字段，方便 Log 打印与调试
     *
     * @return 形如 {@code Goods{id=1, name='无线蓝牙耳机', ...}} 的文本
     */
    @Override
    public String toString() {
        return "Goods{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", description='" + description + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", imageResId=" + imageResId +
                '}';
    }

    // ================== 静态初始化数据 ==================

    /** 商品名称数组 */
    public static final String[] NAMES = {
            "无线蓝牙耳机", "智能运动手表", "双肩背包", "跑步运动鞋", "微单相机"
    };

    /** 商品描述数组（下标与 {@link #NAMES} 一一对应） */
    public static final String[] DESCRIPTIONS = {
            "主动降噪，续航30小时，佩戴舒适",
            "心率监测，50米防水，多运动模式",
            "大容量防泼水，人体工学背负系统",
            "轻量缓震，透气网面，适合长跑",
            "2400万像素，4K视频，轻便易携带"
    };

    /** 商品价格数组（元），下标与 {@link #NAMES} 对应 */
    public static final double[] PRICES = {
            299.00, 599.00, 159.00, 349.00, 3999.00
    };

    /** 商品库存数量数组，下标与 {@link #NAMES} 对应 */
    public static final int[] STOCKS = {
            100, 50, 200, 80, 20
    };

    /** 图片资源编号数组（对应 res/drawable/goods_x.png），下标与 {@link #NAMES} 对应 */
    public static final int[] IMAGE_RES_IDS = {
            R.drawable.goods_1,
            R.drawable.goods_2,
            R.drawable.goods_3,
            R.drawable.goods_4,
            R.drawable.goods_5
    };

    /** 图片路径数组（"下载"到私有目录后的目标路径，初始化时先留空、落盘后回填） */
    public static final String[] IMAGE_PATHS = {
            "", "", "", "", ""
    };

    /**
     * 按商品名反查对应的 drawable 资源 id。
     *
     * <p>为什么需要它：imageResId 是"仅内存用"的字段，不落库；
     * 从数据库查出来的 Goods 对象 imageResId 永远是 0，
     * 所以"图片文件加载失败时回退资源图"的逻辑必须靠商品名反查，否则那段回退代码是死代码。
     *
     * @param name 商品名称
     * @return 对应的资源 id；找不到返回 0（0 不是合法资源 id，可直接当"没有"用）
     */
    public static int imageResIdOf(String name) {
        if (name == null) {
            return 0;
        }
        // 在 NAMES 里找同名项，取同下标的资源 id —— 这就是"平行数组"的用法
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(name)) {
                return IMAGE_RES_IDS[i];
            }
        }
        return 0;
    }

    /**
     * 构建商品信息列表（初始化用）
     *
     * <p>每个 Goods 同时带上 imageResId（"下载"源）和 imagePath（下载目标，暂空）。
     *
     * @return 5 条商品数据，顺序与 {@link #NAMES} 一致
     */
    public static List<Goods> buildGoodsList() {
        List<Goods> list = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            // 用平行数组的同一下标取名字 / 价格 / 库存 / 描述
            Goods g = new Goods(
                    NAMES[i],
                    PRICES[i],
                    STOCKS[i],
                    DESCRIPTIONS[i],
                    IMAGE_PATHS[i]
            );
            g.setImageResId(IMAGE_RES_IDS[i]); // 内存字段，不落库
            list.add(g);
        }
        return list;
    }
}
