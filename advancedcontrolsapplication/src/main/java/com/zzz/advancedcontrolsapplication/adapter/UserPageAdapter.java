package com.zzz.advancedcontrolsapplication.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.zzz.advancedcontrolsapplication.fragment.DynamicFragment;
import com.zzz.advancedcontrolsapplication.pojo.User;

import java.util.List;

public class UserPageAdapter extends FragmentPagerAdapter {

    private final List<User> userList;

    public UserPageAdapter(@NonNull FragmentManager fm, List<User> userList) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.userList = userList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        User user = userList.get(position);
        // position 一起传进去，Fragment 里 getPosition() 打日志才有意义
        return DynamicFragment.newInstance(
                user.getUserName(),
                user.getUserAge(),
                user.getUserIntroduction(),
                position);
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        // PagerTabStrip 的标签文字就取自这里
        return userList.get(position).getUserName();
    }


}
