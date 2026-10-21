package com.zzz.datasharingapplication;

import android.app.Application;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Application 全局数据演示：在进程级单例里维护一份用户列表
 *
 * <p>演示要点：
 * 1. {@code Application} 是"进程单例"，它的生命周期从进程创建到进程结束，
 *    比任何 Activity 都长，因此放在里面的集合不会因为页面销毁而丢；
 * 2. <b>但它没有持久化能力</b>：进程被杀（内存紧张、用户划掉任务）后数据就没了，
 *    要长期保存还是得用 SharedPreferences / 数据库 / 文件；
 * 3. <b>重要前提</b>：清单里 {@code android:name} 只能注册一个 Application。
 *    本模块当前注册的是 {@code ShopApplication}，所以本类的 {@code onCreate} <b>不会执行</b>，
 *    {@link #instance} 始终为 null —— 这一页只是保留下来讲原理，
 *    想看效果需要把本类注册进清单（或把它并入已注册的那个 Application）；
 * 4. 四个生命周期回调里，{@code onTerminate} 在真机上基本不会触发，不要在里面写清理逻辑。
 *
 * <p>相关文件：{@code ShopApplication}（实际注册的那个）、
 * {@code ApplicationActivity}（写入）、{@code ShowActivity}（读取）
 */
public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    // 单例（懒汉式，双重检查锁）
    private static volatile MyApplication instance;

    // 全局存储表单数据：进程存活期间一直有效
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

    /**
     * 往全局列表里加一条用户信息
     *
     * <p>注意：这里没有做去重、没有线程同步，多个页面同时加可能并发修改 List，
     * 正式项目应换成线程安全容器或加锁。
     *
     * @param info 要保存的用户信息，不能为 null
     */
    public void addUserInfo(UserInfo info) {
        userInfoList.add(info);
        Log.d(TAG, "addUserInfo: 当前数量 = " + userInfoList.size());
    }

    /**
     * 取出全局列表（返回的是内部集合的引用，调用方可以修改到它）
     *
     * @return 当前已保存的全部用户信息
     */
    public List<UserInfo> getUserInfoList() {
        return userInfoList;
    }

    /**
     * 清空全局数据
     */
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