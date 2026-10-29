package com.zzz.serviceapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.serviceapplication.service.MyIntentService;

/**
 * IntentService 的调用入口（对应文章「Service 与 Thread 的区别」一节）。
 * 对应布局：activity_intent_service.xml
 * 涉及 API：startService()、IntentService.onHandleIntent()
 * <p>
 * 观察重点：
 * 1. 连发 3 个任务，日志里它们的线程名完全相同 → 只有一个 worker thread；
 * 2. 三个任务首尾严格相接（任务 1 结束才开始任务 2）→ 串行排队；
 * 3. 全部任务跑完后自动出现 onDestroy → IntentService 会自己 stopSelf()，不用手动停。
 * <p>
 * 注意 MyIntentService 已标记 @Deprecated，这里的调用方式仅为教学演示。
 */
public class IntentServiceActivity extends AppCompatActivity {

    /** 日志 TAG */
    private static final String TAG = "ZZZ";

    /** 界面回显用，记录已提交的任务数 */
    private int submitted = 0;

    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intent_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvStatus = findViewById(R.id.tv_status);

        // 提交单个任务：服务会立即创建并开始执行
        findViewById(R.id.btn_one_task).setOnClickListener(v -> {
            submitTask();
            Log.d(TAG, "[Client] 提交 1 个任务");
        });

        // 连发 3 个：三次 startService，服务只会创建一次，任务依次排队
        findViewById(R.id.btn_three_tasks).setOnClickListener(v -> {
            for (int i = 0; i < 3; i++) {
                submitTask();
            }
            Log.d(TAG, "[Client] 连发 3 个任务");
        });
    }

    /**
     * 提交一个任务。
     * 每调用一次 startService()，IntentService 的 onHandleIntent() 就会被调用一次。
     */
    private void submitTask() {
        submitted++;
        Intent intent = new Intent(this, MyIntentService.class);
        // 任务编号放在 extra 里，服务端取出来打到日志上
        intent.putExtra(MyIntentService.EXTRA_TASK_ID, submitted);
        startService(intent);
        tvStatus.setText("已提交 " + submitted + " 个任务");
    }
}
