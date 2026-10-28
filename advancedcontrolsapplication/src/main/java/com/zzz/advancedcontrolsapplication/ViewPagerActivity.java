package com.zzz.advancedcontrolsapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.zzz.advancedcontrolsapplication.adapter.ImagePageAdapter;
import com.zzz.advancedcontrolsapplication.pojo.BannerImage;

/**
 * 翻页视图（ViewPager + PagerAdapter）轮播图演示。
 *
 * <p>ViewPager 只管滑动和预加载左右页，页面内容交给 Adapter 造；
 * 顶部标签栏用 ViewPager 自带的 PagerTabStrip，文字取自 Adapter#getPageTitle。
 *
 * <p>对应布局：{@code activity_view_pager.xml}
 */
public class ViewPagerActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private static final String TAG = "ViewPagerActivity";

    /** 自动翻页间隔 */
    private static final long AUTO_SCROLL_INTERVAL = 3000L;

    private ViewPager vpBanner;

    private ImagePageAdapter adapter;

    /** 定时任务必须跑在主线程，UI 操作也只能在主线程 */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 自动翻页任务。
     *
     * <p>写成成员变量：removeCallbacks 要移除同一个实例才生效，
     * 每次 new 一个新 Runnable 是取消不掉的。
     */
    private final Runnable autoScrollTask = new Runnable() {
        @Override
        public void run() {
            // 往后翻一页，第二个参数 true 表示带动画
            vpBanner.setCurrentItem(vpBanner.getCurrentItem() + 1, true);
            // postDelayed 只执行一次，不重排就只翻一页
            handler.postDelayed(this, AUTO_SCROLL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_pager);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vpBanner = findViewById(R.id.vp_banner);

        adapter = new ImagePageAdapter(this, BannerImage.getDefaultBanners());
        vpBanner.setAdapter(adapter);

        // 本类 implements 了 OnPageChangeListener，三个滑动回调才会被调用
        vpBanner.addOnPageChangeListener(this);

        // 起始位置必须在正中间，再向下对齐到真实张数的整数倍（保证首屏是第 1 张）。
        // 注意别写成 (getCount() / 张数) * 张数 —— 那算出的是贴着末尾的位置，
        // 只能再往前滑两页就到头了。
        int start = adapter.getCount() / 2;
        start = start - (start % adapter.getRealCount());
        vpBanner.setCurrentItem(start, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(autoScrollTask, AUTO_SCROLL_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 不停的话页面已不可见还在翻页，而且 Handler 持有 Activity，无法回收
        handler.removeCallbacks(autoScrollTask);
    }

    /**
     * 翻页停稳后回调，position 是假下标（大数），% 张数 才是第几张图。
     */
    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: 第 " + (position % adapter.getRealCount() + 1) + " 张");
    }

    /**
     * 滑动过程中每帧都回调，适合做视差/渐变，这里不做处理。
     *
     * @param position             正在滑离的页下标
     * @param positionOffset       偏移百分比，0~1
     * @param positionOffsetPixels 偏移像素
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    /**
     * 翻页状态变化：0 静止、1 手指拖动中、2 松手后惯性滑动。
     */
    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
