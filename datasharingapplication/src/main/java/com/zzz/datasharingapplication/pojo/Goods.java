package com.zzz.datasharingapplication.pojo;

import com.zzz.datasharingapplication.R;

import java.util.ArrayList;
import java.util.List;

public class Goods {
    private int id;              // 商品ID（数据库自增）
    private String name;         // 商品名称
    private double price;        // 商品价格
    private int stock;           // 库存数量
    private String description;  // 商品描述
    private String imagePath;    // 图片路径（存库）
    private int imageResId;      // 图片资源编号（不存库，仅内存用）

    // 无参构造
    public Goods() {
    }

    // 全参构造（不含 id 和 imageResId，用于新增落库）
    public Goods(String name, double price, int stock,
                 String description, String imagePath) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imagePath = imagePath;
    }

    // 全参构造（含 id，用于查询回显）
    public Goods(int id, String name, double price, int stock,
                 String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imagePath = imagePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

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

    /** 商品描述数组 */
    public static final String[] DESCRIPTIONS = {
            "主动降噪，续航30小时，佩戴舒适",
            "心率监测，50米防水，多运动模式",
            "大容量防泼水，人体工学背负系统",
            "轻量缓震，透气网面，适合长跑",
            "2400万像素，4K视频，轻便易携带"
    };

    /** 商品价格数组（元） */
    public static final double[] PRICES = {
            299.00, 599.00, 159.00, 349.00, 3999.00
    };

    /** 商品库存数量数组 */
    public static final int[] STOCKS = {
            100, 50, 200, 80, 20
    };

    /** 图片资源编号数组（对应 res/drawable/goods_x.png） */
    public static final int[] IMAGE_RES_IDS = {
            R.drawable.goods_1,
            R.drawable.goods_2,
            R.drawable.goods_3,
            R.drawable.goods_4,
            R.drawable.goods_5
    };

    /** 图片路径数组（下载到 SD 卡后的目标路径，先留空待下载后回填） */
    public static final String[] IMAGE_PATHS = {
            "", "", "", "", ""
    };

    /**
     * 构建商品信息列表（初始化用）
     * 每个 Goods 同时带上 imageResId（下载源）和 imagePath（下载目标，暂空）
     */
    /**
     * 按商品名反查对应的 drawable 资源 id。
     *
     * <p>为什么需要它：imageResId 是"仅内存用"的字段，不落库；
     * 从数据库查出来的 Goods 对象 imageResId 永远是 0，
     * 所以"SD 卡图片加载失败时回退资源图"的逻辑必须靠商品名反查，否则回退分支是死代码。
     *
     * @param name 商品名称
     * @return 对应的资源 id；找不到返回 0（0 不是合法资源 id，可直接当"没有"用）
     */
    public static int imageResIdOf(String name) {
        if (name == null) {
            return 0;
        }
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(name)) {
                return IMAGE_RES_IDS[i];
            }
        }
        return 0;
    }

    public static List<Goods> buildGoodsList() {
        List<Goods> list = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
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
