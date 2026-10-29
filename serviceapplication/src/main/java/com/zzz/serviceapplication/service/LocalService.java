package com.zzz.serviceapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * 绑定状态下的 Service 演示（对应文章的「Service 绑定服务」一节，扩展 Binder 类的方式）。
 * 对应布局：activity_bind.xml
 * 涉及 API：Service.onBind()、Binder、ServiceConnection、bindService()、unbindService()
 * <p>
 * <b>一、绑定态与启动态的本质差别</b><br>
 * 绑定态下服务扮演「客户端-服务器」架构里的服务器：客户端通过 IBinder 向它发请求、调它的方法、
 * 甚至跨进程通信。它的寿命由客户端绑定数决定 —— 所有客户端都解绑后系统会销毁服务，
 * <b>不需要</b>调用 stopService()。
 * <p>
 * <b>二、三种定义接口的方式，怎么选</b>
 * <ul>
 *   <li><b>扩展 Binder 类（本类）</b>：最简单。通过 getService() 把 Service 自身暴露出去，
 *       客户端拿到的就是真实对象，能直接调公共方法。⚠️ <b>仅限同一应用、同一进程</b>，
 *       因为 Binder 对象不做跨进程编组，换进程强转会直接崩。</li>
 *   <li><b>使用 Messenger</b>：基于 Handler 的跨进程方案，请求串行进入一个消息队列，
 *       天然线程安全。缺点是并发能力差。见 {@link MessengerService}。</li>
 *   <li><b>使用 AIDL</b>：Messenger 的底层其实就是 AIDL。需要服务自行处理多线程与线程安全，
 *       要写 .aidl 文件由 SDK 生成 Stub。绝大多数应用用不到。</li>
 * </ul>
 * <p>
 * <b>三、实测得到的调用顺序</b><br>
 * 首次 bindService() → 服务 onCreate() → onBind() → 客户端 onServiceConnected()<br>
 * 再次 bindService() → <b>什么都不回调</b>（onBind 全程只走一次）<br>
 * unbindService() → onUnbind() → onDestroy()
 * <p>
 * <b>四、两个极易踩空的细节</b>
 * <ul>
 *   <li><b>onServiceDisconnected() 只在连接「意外」中断时回调</b>（服务崩溃、进程被系统杀死）。
 *       客户端主动 unbindService() 时系统<b>绝对不会</b>调用它 ——
 *       把清理逻辑写在那里等于永远不执行。</li>
 *   <li><b>bindService() 的第三个参数</b>决定服务不存在时是否自动创建：
 *       传 0 不创建，传 {@code BIND_AUTO_CREATE} 才会创建。示例里一律用后者。</li>
 * </ul>
 */
public class LocalService extends Service {
    /** 日志 TAG，与其他 demo 保持一致 */
    private final static String TAG = "ZZZ";

    /** 服务内部状态，客户端通过 getCount() 读走，用来证明「拿到的是同一个实例」 */
    private int count;

    /** 控制计数线程退出的开关，onDestroy 时置 true */
    private boolean quit;

    /** 计数线程，每 1 秒把 count 加 1 */
    private Thread thread;

    /** 唯一的 Binder 实例，onBind() 每次返回的都是它 */
    private LocalBinder binder = new LocalBinder();

    /**
     * 创建 Binder 对象，返回给客户端即 Activity 使用，提供数据交换的接口。
     * <p>
     * Binder 本身只是个通道，客户端拿到它以后还得通过 getService() 才能摸到 Service 本体。
     */
    public class LocalBinder extends Binder {
        /**
         * 声明一个方法 getService，供客户端调用。
         *
         * @return 当前 Service 实例
         */
        public LocalService getService() {
            // 返回当前对象 LocalService.this（外部类实例），
            // 这样客户端就能直接调用 Service 的公共方法了
            return LocalService.this;
        }
    }

    /**
     * 客户端 bindService() 时回调，多次绑定只有第一次触发。
     *
     * @param intent 客户端绑定时传入的 Intent
     * @return 供客户端使用的 Binder，绑定态下不能返回 null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service is invoke Created");
        // 服务本身跑在主线程，耗时活必须另开线程 —— 这也是「Service 不是线程」的直接体现
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 每间隔一秒 count 加 1，直到 quit 为 true。
                // ⚠️ quit 没有加 volatile，理论上存在线程可见性问题（主线程改了、工作线程可能读不到旧值）。
                // 这里能正常工作是因为 Thread.sleep 会触发内存屏障，属于巧合而非设计。
                while (!quit) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
            }
        });
        thread.start();
    }

    /**
     * 公共方法，客户端拿到 Service 实例后调用它读数据。
     *
     * @return 服务已运行的秒数
     */
    public int getCount() {
        return count;
    }

    /**
     * 解除绑定时调用。
     * 注意：所有客户端都解绑后，紧接着就是 onDestroy()，服务不需要自己停止。
     *
     * @param intent 最后一次 bindService 传入的 Intent
     * @return 返回值决定重新绑定时是否回调 onRebind()，此处不关心
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service is invoke onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service is invoke Destroyed");
        // 置 true 让计数线程退出循环，否则线程会一直持有 Service 引用造成泄漏
        this.quit = true;
        super.onDestroy();
    }
}
