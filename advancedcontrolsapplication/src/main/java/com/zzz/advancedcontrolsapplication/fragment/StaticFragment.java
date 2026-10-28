package com.zzz.advancedcontrolsapplication.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zzz.advancedcontrolsapplication.R;

/**
 * 静态加载的 Fragment：由 activity_fragment_static.xml 里的 &lt;fragment&gt; 标签直接声明。
 *
 * <p>每个生命周期方法里打一条 Log.d（TAG = "fragment"），
 * 用 adb logcat -s fragment 就能看到完整调用顺序。
 */
public class StaticFragment extends Fragment {

    public static final String TAG = "fragment";

    /** 1. 附着到 Activity，此时才能拿到 Context / Activity */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    /** 2. Fragment 自身初始化（只调一次，不涉及界面） */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    /** 3. 创建视图，返回值就是这一页的根布局 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_static, container, false);
    }



    /** 4. 视图创建完成，比 onCreateView 多了 view 参数，findViewById / 初始化控件写在这里 */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume —— 此刻开始可见、可交互");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    /** 视图销毁，但 Fragment 对象还在；切走再切回时 onCreateView 会重新走一遍 */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /** 与 Activity 分离，最后一个回调 */
    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }
}
