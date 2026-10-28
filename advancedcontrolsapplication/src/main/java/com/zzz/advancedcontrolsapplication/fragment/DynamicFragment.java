package com.zzz.advancedcontrolsapplication.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zzz.advancedcontrolsapplication.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DynamicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DynamicFragment extends Fragment {
    public static final String TAG = "dynamic fragment";

    public static DynamicFragment newInstance(String user_name, int user_age, String user_introduction,
                                              int position) {
        DynamicFragment fragment = new DynamicFragment();
        Bundle args = new Bundle();
        args.putString("user_name", user_name);
        args.putInt("user_age", user_age);
        args.putString("user_introduction", user_introduction);
        // 位置也要塞进 Bundle：Fragment 被系统重建时只有 Bundle 里的数据还在
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }


    // 创建碎片视图
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dynamic, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            // 1. 先在当前 Fragment 的布局里找到控件
            TextView tv_name = view.findViewById(R.id.tv_name);
            TextView tv_age = view.findViewById(R.id.tv_age);
            TextView tv_introduction = view.findViewById(R.id.tv_introduction);

            // 2. 再把 Bundle 里的数据填进去
            tv_name.setText("姓名：" + arguments.getString("user_name"));
            tv_age.setText("年龄：" + arguments.getInt("user_age") + " 岁");
            tv_introduction.setText("简介：" + arguments.getString("user_introduction"));
        }
        Log.d(TAG, "onCreateView: " + getPosition());
        return view;
    }

    // 从包裹中取出位置序号
    private int getPosition() {
        // 直接 new DynamicFragment() 时没有 Bundle，这里要防一下空指针
        Bundle args = getArguments();
        return args == null ? 0 : args.getInt("position", 0);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: " + getPosition());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + getPosition());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: " + getPosition());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + getPosition());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: " + getPosition());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: " + getPosition());
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: " + getPosition());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: " + getPosition());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + getPosition());
    }
}