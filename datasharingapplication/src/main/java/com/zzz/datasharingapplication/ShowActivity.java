package com.zzz.datasharingapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

/**
 * Application 全局数据演示（展示页）：读取 Application 里存的列表并逐条打印
 *
 * <p>演示要点：
 * 1. 这里<b>没有</b>通过 Intent 接收数据——数据在进程级单例里，跨页面直接可读，
 *    这就是"Application 当全局容器"的用法；
 * 2. 风险提示：正因为"谁都能读能改"，全局状态一旦多了就难以追踪，
 *    正式项目更推荐 ViewModel / Repository 这类有明确作用域的方案；
 * 3. 注意：如果进程被系统回收过，这里读到的就是空列表——数据没有持久化。
 *
 * <p>对应布局：{@code activity_show.xml}
 * <p>涉及 API：{@link StringBuilder}（字符串拼接）、{@link TextView#setText}
 */
public class ShowActivity extends AppCompatActivity {

    /**
     * 页面创建：读取全局列表 → 拼成多行文本 → 一次性设置给 TextView
     *
     * @param savedInstanceState 系统保存的临时状态（本页无需恢复数据）
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show);

        // 与其他页面一致的 insets 处理：避免内容被系统栏遮挡
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvResult = findViewById(R.id.tvResult);

        // 1. 通过单例读取 Application 中存储的全局数据
        MyApplication app = MyApplication.getInstance();
        List<MyApplication.UserInfo> list = app.getUserInfoList();

        // 2. 空列表与有数据分两种展示
        if (list.isEmpty()) {
            tvResult.setText("暂无数据");
        } else {
            // 先拼标题，再逐条追加；用 StringBuilder 而不是 String += ，避免循环里反复创建字符串
            StringBuilder sb = new StringBuilder("共 " + list.size() + " 条数据：\n\n");
            for (int i = 0; i < list.size(); i++) {
                // i+1 是给人看的序号（从 1 开始），list.get(i) 拿对象，再调它的 toString()
                sb.append(i + 1).append(". ").append(list.get(i).toString()).append("\n\n");
            }
            // setText 传 CharSequence 即可，StringBuilder 也是 CharSequence
            tvResult.setText(sb.toString());
        }
    }
}
