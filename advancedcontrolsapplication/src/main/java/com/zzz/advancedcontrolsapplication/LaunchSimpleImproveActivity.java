package com.zzz.advancedcontrolsapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.zzz.advancedcontrolsapplication.adapter.LaunchSimpleAdapter;
import com.zzz.advancedcontrolsapplication.adapter.LaunchSimpleImproveAdapter;

public class LaunchSimpleImproveActivity extends AppCompatActivity {
    // 声明引导页面的图片数组
    private static int[] launchImageArray = {
            R.drawable.guide_1,
            R.drawable.guide_2,
            R.drawable.guide_3,
            R.drawable.guide_4

    };
    private ViewPager vp_launch_improve;
    private LaunchSimpleImproveAdapter launchSimpleImproveActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launch_simple_improve);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vp_launch_improve = findViewById(R.id.vp_launch_improve);

        launchSimpleImproveActivity = new LaunchSimpleImproveAdapter(getSupportFragmentManager(), launchImageArray);
        vp_launch_improve.setAdapter(launchSimpleImproveActivity);
    }
}