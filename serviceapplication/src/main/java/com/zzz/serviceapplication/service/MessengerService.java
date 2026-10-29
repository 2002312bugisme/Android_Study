package com.zzz.serviceapplication.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;


/**
 * 用 Messenger 实现跨进程通信的 Service（对应文章的 4.2 节）。
 * 对应布局：activity_message.xml
 * 涉及 API：Messenger、Handler、Message、IBinder
 * <p>
 * 清单里声明了 android:process=":remote"，所以本服务跑在独立进程，
 * 客户端与它之间只能靠 Binder 驱动传 Parcelable 数据，不能直接传对象引用。
 * 这就是「不能像 LocalService 那样强转 Binder」的原因。
 * <p>
 * <b>Message 能带什么数据</b><br>
 * Messenger 传数据必须把数据塞进 Message，因为 Message 和 Messenger 都实现了 Parcelable，
 * 可以跨进程传递。Message 的可用载体有：
 * <ul>
 *   <li><b>what</b>：消息类型（int），服务端靠它决定做什么；</li>
 *   <li><b>arg1 / arg2</b>：两个 int 型轻量参数，不够用就换 Bundle；</li>
 *   <li><b>obj</b>：⚠️ 只对同进程有用。Android 2.2 前完全不支持跨进程，
 *       之后也只允许传递系统提供的 Parcelable 对象，自定义 Parcelable 塞进去照样失败；</li>
 *   <li><b>data（Bundle）</b>：支持的类型最全，跨进程传数据首选它；</li>
 *   <li><b>replyTo（Messenger）</b>：把「回信的地址」带过去，服务端处理完可以回传结果。</li>
 * </ul>
 * 发送本身是 {@code oneway} 的：客户端 send() 立即返回，不等待服务端处理，
 * 所以两边日志的先后顺序并不固定。
 */
public class MessengerService extends Service {

    /**
     * Command to the service to display a message
     * 客户端发的消息类型，服务端靠 Message.what 区分要执行什么
     */
    public static final int MSG_SAY_HELLO = 1;

    /**
     * 日志 TAG，客户端用同一个，方便在 logcat 里对照两个进程的输出
     */
    private static final String TAG = "ZZZ";

    /**
     * 用于接收从客户端传递过来的数据。
     * <p>
     * 它跑在「服务端进程」的主线程上：客户端 send() 的消息经 Binder 驱动送到本进程，
     * 最终在这个 Handler 里被取出。
     * 注意这是内部类（非 static），编译期会持有 MessengerService 的引用；
     * 由于 Service 本身由系统管理生命周期，这里不会造成内存泄漏。
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // what 是消息类型，只有客户端发了 MSG_SAY_HELLO 才走这里
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Log.i(TAG, "thanks,Service had receiver message from client!");
                    break;
                default:
                    // 不认识的消息交回父类，避免把系统消息吞掉
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * 用于接收从客户端传递过来的数据
     */
    class BidirectionalIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Log.i(TAG, "thanks,Service had receiver message from client!");
                    //回复客户端信息,该对象由客户端传递过来
                    Messenger client = msg.replyTo;
                    //获取回复信息的消息实体
                    Message replyMsg = Message.obtain(null, MessengerService.MSG_SAY_HELLO);
                    Bundle bundle = new Bundle();
                    bundle.putString("reply", "ok~,I had receiver message from you! ");
                    replyMsg.setData(bundle);
                    //向客户端发送消息
                    try {
                        client.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * 创建 Messenger 并传入 Handler 实例对象。
     * <p>
     * Messenger 是 Handler 的跨进程包装：它内部持有 IMessenger.aidl 的 Stub，
     * send() 会被转换成 binder 调用，跨进程投递到上面的 IncomingHandler。
     * 构造时把 Handler 传进去，等于宣布「所有消息都交给 IncomingHandler 处理」。
     */
    // 换成新的BidirectionalIncomingHandler 进行新的消息处理逻辑
    final Messenger mMessenger = new Messenger(new BidirectionalIncomingHandler());

    /**
     * 当绑定 Service 时该方法被调用，通过 mMessenger 返回一个实现 IBinder 接口的实例对象。
     * <p>
     * 关键差异：这里返回的不是 Service 自己，而是 Messenger 内部的 Binder。
     * 客户端拿它 new Messenger(service)，就能跨进程发消息了。
     *
     * @param intent 客户端 bindService 时传入的 Intent
     * @return Messenger 内部的 IBinder，可跨进程传递
     */
    @Override
    public IBinder onBind(Intent intent) {
        // 打印 pid：客户端与服务端会打印出两个不同的值，证明真的跨进程了
        Log.i(TAG, "Service is invoke onBind, pid=" + Process.myPid());
        return mMessenger.getBinder();
    }

    // ---------------------------------------------------------------
    // 下面三个回调原文示例里没有，补上是为了验证文章的那句结论：
    // 「Messenger 与扩展 Binder 两种方式的生命周期调用顺序基本一致」
    // ---------------------------------------------------------------

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service onCreate, pid=" + Process.myPid());
    }

    /**
     * 所有客户端解绑时回调，随后系统销毁服务。
     *
     * @param intent 最后一次 bindService 传入的 Intent
     * @return 返回值决定重新绑定时是否回调 onRebind()，此处不关心
     */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service onDestroy");
    }
}
