package com.zzz.advancedcontrolsapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import com.zzz.advancedcontrolsapplication.adapter.UserPageAdapter;
import com.zzz.advancedcontrolsapplication.pojo.User;

import java.util.List;

public class FragmentDynamicActivity extends AppCompatActivity {

    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fragment_dynamic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initPagerStrip();

        initViewPager();
    }

    // 初始化翻页视图
    private void initViewPager() {
        ViewPager vp_content = findViewById(R.id.vp_content);
        userList = User.getUserList();
        UserPageAdapter userPageAdapter = new UserPageAdapter(getSupportFragmentManager(), userList);
        vp_content.setAdapter(userPageAdapter);
        vp_content.setCurrentItem(1);
    }

    // 初始化翻页标签栏
    private void initPagerStrip() {
        PagerTabStrip pts_tab = findViewById(R.id.pts_tab);
        // 设置翻页标签栏的文本大小
        pts_tab.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        pts_tab.setTextColor(Color.BLACK);
    }
}