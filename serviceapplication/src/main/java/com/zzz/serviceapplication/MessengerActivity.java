package com.zzz.serviceapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.MessengerService;

/**
 * Messenger 跨进程通信的客户端（对应文章的 4.2 节）。
 * 对应布局：activity_message.xml（状态 TextView + bind / unbind / sendMsg 三个按钮）
 * 涉及 API：bindService()、ServiceConnection、Messenger、Message
 * <p>
 * 与 LocalService 的核心区别：这里拿到的 IBinder 属于另一个进程，
 * 不能强转成自定义的 Binder 子类，只能包成 Messenger，靠 Message 传数据。
 */
public class MessengerActivity extends AppCompatActivity {

    /** 日志 TAG，与服务端保持一致，便于在 logcat 里对照两个进程 */
    private static final String TAG = "ZZZ";

    /**
     * 与服务端交互的 Messenger。
     * 它内部持有服务端 Binder 的代理，调用 send() 相当于发起一次跨进程调用。
     */
    private Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. 是否已成功绑定 */
    private boolean mBound;

    /** 已发出的消息条数，只用于界面回显 */
    private int msgCount = 0;

    /** 一行状态输出，不参与业务逻辑 */
    private TextView tvStatus;

    /**
     * 实现与服务端链接的对象。
     * 两个方法都是主线程回调；这段代码跑在客户端进程里。
     */
    private final ServiceConnection mConnection = new ServiceConnection() {
        /**
         * 连接成功时回调。
         *
         * @param className 被绑定服务的组件信息，一般用不到
         * @param service   服务端 onBind() 返回的 IBinder（这里是 Messenger 内部的 Binder）
         */
        public void onServiceConnected(ComponentName className, IBinder service) {
            /**
             * 通过服务端传递的 IBinder 对象创建相应的 Messenger，
             * 之后就用这个 Messenger 与服务端交互。
             * 注意：不 new 出来就没法发消息，因为 IBinder 本身没有 send() 方法。
             */
            mService = new Messenger(service);
            mBound = true;
            tvStatus.setText("已绑定，可以发消息");
            Log.d(TAG, "onServiceConnected, pid=" + Process.myPid());
        }

        /**
         * 连接意外断开时回调。
         * 服务崩溃或被系统杀死才会走到这里，客户端主动 unbindService 不会触发。
         *
         * @param className 被绑定服务的组件信息
         */
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

    /**
     * 向服务端发送一条消息。
     *
     * @param v 触发本次调用的控件，本方法内未使用
     */
    public void sayHello(View v) {
        // 没绑定就发消息会 NPE，这里直接拦截
        if (!mBound) {
            tvStatus.setText("未绑定，忽略本次发送");
            Log.d(TAG, "还没绑定服务，忽略本次发送");
            return;
        }
        // 创建与服务交互的消息实体 Message。what 是消息类型，arg1/arg2 只能传 int
        Message msg = Message.obtain(null, MessengerService.MSG_SAY_HELLO, 0, 0);
        try {
            // 发送消息，跨进程失败（如服务端进程已死）会抛 RemoteException
            mService.send(msg);
            msgCount++;
            tvStatus.setText("已发出第 " + msgCount + " 条消息");
            Log.d(TAG, "客户端已发出第 " + msgCount + " 条消息, pid=" + Process.myPid());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void sayHelloMessage(View v) {
        if (!mBound) return;
        // 创建与服务交互的消息实体Message
        Message msg = Message.obtain(null, MessengerService.MSG_SAY_HELLO, 0, 0);
        //把接收服务器端的回复的Messenger通过Message的replyTo参数传递给服务端
        msg.replyTo=mRecevierReplyMsg;
        try {
            //发送消息
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvStatus = findViewById(R.id.tv_status);
        Button bindService = (Button) findViewById(R.id.btn_bindService);
        Button unbindService = (Button) findViewById(R.id.btn_unbindService);
        Button sendMsg = (Button) findViewById(R.id.btn_sendMsgToService);

        bindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick-->bindService");
                // 当前 Activity 绑定服务端；BIND_AUTO_CREATE 让服务不存在时自动创建
                bindService(new Intent(MessengerActivity.this, MessengerService.class), mConnection,
                        Context.BIND_AUTO_CREATE);
            }
        });

        // 发送消息给服务端
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sayHello(v);
                sayHelloMessage(v);
            }
        });


        unbindService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Unbind from the service
                if (mBound) {
                    Log.d(TAG, "onClick-->unbindService");
                    unbindService(mConnection);
                    mBound = false;
                    tvStatus.setText("已解绑");
                }
            }
        });

        // 本页是 launcher，顺便做其余演示页的入口
        findViewById(R.id.btn_gotoForeground).setOnClickListener(v ->
                startActivity(new Intent(this, ForegroundActivity.class)));
        findViewById(R.id.btn_gotoSimple).setOnClickListener(v ->
                startActivity(new Intent(this, SimpleActivity.class)));
        findViewById(R.id.btn_gotoHybrid).setOnClickListener(v ->
                startActivity(new Intent(this, HybridActivity.class)));
        findViewById(R.id.btn_gotoIntentService).setOnClickListener(v ->
                startActivity(new Intent(this, IntentServiceActivity.class)));
        findViewById(R.id.btn_gotoImplicit).setOnClickListener(v ->
                startActivity(new Intent(this, ImplicitStartActivity.class)));
        findViewById(R.id.btn_gotoKeepAlive).setOnClickListener(v ->
                startActivity(new Intent(this, KeepAliveActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 页面销毁时若还绑着，会泄漏 ServiceConnection，系统会打印 leaked 警告
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * 用于接收服务器返回的信息
     */
    private Messenger mRecevierReplyMsg= new Messenger(new ReceiverReplyMsgHandler());


    private static class ReceiverReplyMsgHandler extends Handler {
        private static final String TAG = "ZZZ";

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //接收服务端回复
                case MessengerService.MSG_SAY_HELLO:
                    Log.i(TAG, "receiver message from service:"+msg.getData().getString("reply"));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
