package com.zzz.advancedcontrolsapplication.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;

import com.zzz.advancedcontrolsapplication.R;
import com.zzz.advancedcontrolsapplication.util.ToastUtil;

public class LaunchFragment extends Fragment {

    public static LaunchFragment newInstance(int count, int position, int image_id) {
        LaunchFragment fragment = new LaunchFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("image_id", image_id);
        args.putInt("image_count", count);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 先 inflate，保证任何情况下都返回一个 View（返回 null 会让 ViewPager 白屏甚至崩）
        View view = inflater.inflate(R.layout.fragment_launch, container, false);

        Bundle arguments = getArguments();
        if (arguments == null) {
            return view;
        }

        int position = arguments.getInt("position", 0);
        int imageId = arguments.getInt("image_id", 0);
        int imageCount = arguments.getInt("image_count", 0);

        ImageView iv_launch = view.findViewById(R.id.iv_launch);
        RadioGroup rg_indicate = view.findViewById(R.id.rg_indicate);
        Button btn_start = view.findViewById(R.id.btn_start);

        iv_launch.setImageResource(imageId);

        // 每个页面都分配一组对应的单选按钮。
        // setPadding 单位是 px，420dpi 下 10px 只有 3.8dp，这里按 dp 换算。
        int pad = (int) (4 * getResources().getDisplayMetrics().density);
        for (int j = 0; j < imageCount; j++) {
            RadioButton radioButton = new RadioButton(view.getContext());
            radioButton.setLayoutParams(new RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            radioButton.setPadding(pad, pad, pad, pad);
            // 不加进 RadioGroup 的话，下面 getChildAt(position) 会拿到 null
            rg_indicate.addView(radioButton);
        }
        // 当前位置的单选按钮要高亮显示
        ((RadioButton) rg_indicate.getChildAt(position)).setChecked(true);

        // 如果是最后一个引导页，则显示入口按钮，以便用户点击按钮进入主界面
        if (position == imageCount - 1) {
            btn_start.setVisibility(View.VISIBLE);
            btn_start.setOnClickListener(v ->
                    ToastUtil.show(view.getContext(), "欢迎开启美好生活"));
        }
        return view;
    }
}