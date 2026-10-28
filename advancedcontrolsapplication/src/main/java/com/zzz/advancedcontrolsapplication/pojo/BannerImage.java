package com.zzz.advancedcontrolsapplication.pojo;

import com.zzz.advancedcontrolsapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 轮播图数据：一张图 + 一个名称。
 *
 * <p>本地 drawable 没有「路径」，只有编译期分配好的 int 资源 id（R.drawable.xxx）；
 * 只有相册图 / 网络图才存在 String 形式的路径。
 */
public class BannerImage {

    /** 图片资源 id */
    private final int imageResId;

    /** 图片名称，显示在 PagerTabStrip 的标签上 */
    private final String name;

    public BannerImage(int imageResId, String name) {
        this.imageResId = imageResId;
        this.name = name;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getName() {
        return name;
    }

    /** 内置的 5 张演示图 */
    public static List<BannerImage> getDefaultBanners() {
        List<BannerImage> banners = new ArrayList<>();
        banners.add(new BannerImage(R.drawable.goods_1, "轮播图 1"));
        banners.add(new BannerImage(R.drawable.goods_2, "轮播图 2"));
        banners.add(new BannerImage(R.drawable.goods_3, "轮播图 3"));
        banners.add(new BannerImage(R.drawable.goods_4, "轮播图 4"));
        banners.add(new BannerImage(R.drawable.goods_5, "轮播图 5"));
        return banners;
    }
}
