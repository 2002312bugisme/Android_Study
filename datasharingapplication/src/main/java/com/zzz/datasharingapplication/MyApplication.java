package com.zzz.datasharingapplication;

import android.app.Application;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    // 单例（懒汉式，双重检查锁）
    private static volatile MyApplication instance;

    // 全局存储表单数据
    private final List<UserInfo> userInfoList = new ArrayList<>();

    /**
     * 懒汉式单例
     */
    public static MyApplication getInstance() {
        if (instance == null) {
            synchronized (MyApplication.class) {
                if (instance == null) {
                    instance = new MyApplication();
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 注意：这里系统会调用，instance 也会被系统创建并赋值
        instance = this;
        Log.d(TAG, "onCreate: Application 被创建，进程 PID = " + android.os.Process.myPid());
    }

    // onTerminate() 不可靠：官方文档明确说明只在模拟器调试时可能被调用，真机上基本不会触发，不要在里面做重要清理逻辑。
    @Override
    public void onTerminate() {
        super.onTerminate();
        // 注意：onTerminate 只在模拟器上会被调用，真机上几乎不会被调用
        Log.d(TAG, "onTerminate: Application 被终止");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory: 系统内存不足");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d(TAG, "onTrimMemory: level = " + level);
    }

    // ==================== 数据操作方法 ====================

    public void addUserInfo(UserInfo info) {
        userInfoList.add(info);
        Log.d(TAG, "addUserInfo: 当前数量 = " + userInfoList.size());
    }

    public List<UserInfo> getUserInfoList() {
        return userInfoList;
    }

    public void clear() {
        userInfoList.clear();
    }

    // ==================== 数据模型 ====================

    public static class UserInfo {
        public String name;
        public String age;
        public String email;

        public UserInfo(String name, String age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        @Override
        public String toString() {
            return "UserInfo{name='" + name + "', age='" + age + "', email='" + email + "'}";
        }
    }

    /*MyApplication extends Application 本身只提供 Context 能力和生命周期回调，不提供数据容器。
    我们在子类里声明自己的数据结构字段（List/Map/自定义对象），并自己封装 add/get/clear 等方法，
    就形成了"全局数据存储"。它的存续期由进程生命周期保证（而非 Activity 生命周期），只要进程不被杀死，
    数据就在；进程一死，数据全丢。
    这与 Spring 里"容器托管的单例池"在思路上有相似之处——都是"靠一个长生命周期的宿主对象持有引用，从而让数据比普通对象活得更久"，
    但 Spring 的池有框架协议的升降级语义，而 Application 存数据没有任何协议，纯粹是开发者自定义的字段。*/
}