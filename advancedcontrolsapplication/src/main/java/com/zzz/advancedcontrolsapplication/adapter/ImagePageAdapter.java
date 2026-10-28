package com.zzz.advancedcontrolsapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.zzz.advancedcontrolsapplication.R;
import com.zzz.advancedcontrolsapplication.pojo.BannerImage;

import java.util.List;

/**
 * ViewPager 的页面适配器。
 *
 * <p>三个必须实现的方法：getCount（一共几页）、instantiateItem（造出第 N 页）、
 * isViewFromObject（认领刚刚造的 View）。
 */
public class ImagePageAdapter extends PagerAdapter {

    private final List<BannerImage> banners;

    private final LayoutInflater inflater;

    public ImagePageAdapter(Context context, List<BannerImage> banners) {
        this.banners = banners;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * 报一个很大的页数，配合下面的取模实现「滑不到头」的无限翻页。
     * 留一半余量，向两侧滑都不会真的顶到边界。
     */
    @Override
    public int getCount() {
        return Integer.MAX_VALUE / 2;
    }

    /** 真实张数，算页码和画标题用；getCount 那个大数是不能直接显示给人看的 */
    public int getRealCount() {
        return banners.size();
    }

    /** 告诉 ViewPager「这个 View 就是刚才返回的那个 object」 */
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    /**
     * 造出第 position 页。
     *
     * @param container ViewPager 本身，新页面必须 addView 进去才会显示
     * @param position  页下标，可能是天文数字，用 % 张数 折回真实下标
     */
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        BannerImage banner = banners.get(position % banners.size());

        View page = inflater.inflate(R.layout.item_banner, container, false);
        ImageView ivBanner = page.findViewById(R.id.iv_banner);
        ivBanner.setImageResource(banner.getImageResId());

        // 不加进容器这一页就不会显示，且不报错
        container.addView(page);
        return page;
    }

    /** 回收滑出屏幕的页 */
    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    /** PagerTabStrip 上每个标签的文字就从这里取 */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return banners.get(position % banners.size()).getName();
    }
}
