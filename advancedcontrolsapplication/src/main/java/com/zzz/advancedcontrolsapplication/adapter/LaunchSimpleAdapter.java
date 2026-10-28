package com.zzz.advancedcontrolsapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.zzz.advancedcontrolsapplication.R;
import com.zzz.advancedcontrolsapplication.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class LaunchSimpleAdapter extends PagerAdapter {

    private List<View> mViewList = new ArrayList<>();

    public LaunchSimpleAdapter(Context context, int[] imageArray) {
        // setPadding 的单位是 px，420dpi 的屏上 10px 只有 3.8dp，圆点会挤在一起，这里按 dp 换算
        int pad = (int) (4 * context.getResources().getDisplayMetrics().density);

        for (int i = 0; i < imageArray.length; i++) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_launch, null);
            ImageView iv_launch = view.findViewById(R.id.iv_launch);
            RadioGroup rg_indicate = view.findViewById(R.id.rg_indicate);
            Button btn_start = view.findViewById(R.id.btn_start);
            iv_launch.setImageResource(imageArray[i]);

            // 每个页面都分配一组对应的单选按钮
            for (int j = 0; j < imageArray.length; j++) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                radioButton.setPadding(pad, pad, pad, pad);
                // ★ 少了这一行，rg_indicate 里一个子 View 都没有，
                //   下面的 getChildAt(i) 就会拿到 null，调用 setChecked 直接空指针
                rg_indicate.addView(radioButton);
            }
            // 当前位置的单选按钮要高亮显示
            ((RadioButton) rg_indicate.getChildAt(i)).setChecked(true);

            // 如果是最后一个引导页，则显示入口按钮，以便用户点击按钮进入主界面
            if (i == imageArray.length - 1) {
                btn_start.setVisibility(View.VISIBLE);
                btn_start.setOnClickListener(v -> {
                    ToastUtil.show(context,"欢迎开启美好生活");
                });
            }

            mViewList.add(view);
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View item = mViewList.get(position);
        container.addView(item);
        return item;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // 用 object 移除，而不是按 position 去 mViewList 里取 —— object 就是当初加进来的那个 View
        container.removeView((View) object);
    }
}
