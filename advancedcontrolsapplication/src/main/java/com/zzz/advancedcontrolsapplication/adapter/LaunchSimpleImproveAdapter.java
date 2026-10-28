package com.zzz.advancedcontrolsapplication.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.zzz.advancedcontrolsapplication.fragment.LaunchFragment;

public class LaunchSimpleImproveAdapter extends FragmentPagerAdapter {

    private int[] imageArray;

    public LaunchSimpleImproveAdapter(@NonNull FragmentManager fm, int[] imageArray) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.imageArray = imageArray;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return LaunchFragment.newInstance(imageArray.length,position,imageArray[position]);
    }

    @Override
    public int getCount() {
        return imageArray.length;
    }
}
