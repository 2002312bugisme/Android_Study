package com.zzz.advancedcontrolsapplication;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;

import com.zzz.advancedcontrolsapplication.adapter.LaunchSimpleAdapter;

public class LaunchSimpleActivity extends AppCompatActivity {

    // 声明引导页面的图片数组
    private int[] launchImageArray = {
            R.drawable.guide_1,
            R.drawable.guide_2,
            R.drawable.guide_3,
            R.drawable.guide_4

    };
    private ViewPager vp_launch;
    private LaunchSimpleAdapter launchSimpleAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lauch_simple);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        vp_launch = findViewById(R.id.vp_launch);

        launchSimpleAdapter = new LaunchSimpleAdapter(this,launchImageArray);
        vp_launch.setAdapter(launchSimpleAdapter);
    }
}