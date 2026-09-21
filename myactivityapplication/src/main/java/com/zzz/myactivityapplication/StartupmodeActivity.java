package com.zzz.myactivityapplication;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StartupmodeActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_startupmode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_jump_toB = findViewById(R.id.btn_jump_toB);

        btn_jump_toB.setOnClickListener(this);
    }

    // standard      ：创建新的 Activity
    // CLEAR_TASK    ：清空整个 Activity 栈
    // CLEAR_TOP     ：找到目标，删除目标上面的 Activity
    // REORDER_FRONT ：找到目标，把目标移动到栈顶
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_jump_toB) {
            Intent intent = new Intent(this, StartupmodefinishActivity.class);
            //intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            intent.setFlags(
//                    Intent.FLAG_ACTIVITY_NEW_TASK |
//                            Intent.FLAG_ACTIVITY_CLEAR_TASK
//            );
            startActivity(intent);


            /*
             * Activity 启动模式 / Intent Flag 对返回栈（Back Stack）的影响：
             *
             * 1. standard
             *    每次启动都创建新的 Activity 实例，并压入栈顶。
             *    例如：A → B → A → B
             *    返回：B → A → B → A → 退出
             *
             * 2. FLAG_ACTIVITY_CLEAR_TASK
             *    清空整个 Task 的 Activity 栈，再启动目标 Activity。
             *    通常与 FLAG_ACTIVITY_NEW_TASK 搭配使用。
             *    适合登录成功后清除登录页面等场景。
             *
             * 3. FLAG_ACTIVITY_CLEAR_TOP
             *    如果目标 Activity 已经存在于当前栈中，
             *    则清除目标 Activity 上面的所有 Activity，
             *    然后回到目标 Activity。
             *
             * 4. FLAG_ACTIVITY_REORDER_TO_FRONT
             *    如果目标 Activity 已经存在于当前栈中，
             *    不创建新的实例，也不删除其他 Activity，
             *    只是将目标 Activity 移动到栈顶。
             *
             * 核心理解：
             *    standard       → 创建新的
             *    CLEAR_TASK     → 清空整个栈
             *    CLEAR_TOP      → 删除目标上面的
             *    REORDER_FRONT  → 把目标移动到最上面
             *
             * Back 操作本质：
             *    从栈顶移除当前 Activity（pop），
             *    显示下面的 Activity；栈为空时退出 Task。
             */


        }
    }
}