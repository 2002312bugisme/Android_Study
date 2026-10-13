package com.zzz.intermediatecontrols;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AlertDialogActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alert_dialog);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_dialog = findViewById(R.id.btn_dialog);
        btn_dialog.setOnClickListener(this);

        tv_message = findViewById(R.id.tv_message);

    }

    @Override
    public void onClick(View v) {
        // 创建提示对话框的建造器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置对话框的标题文本
        builder.setTitle("Dear User : ");
        // 设置对话框的内容文本
        builder.setMessage("Are you really gonna uninstall me?");
        // 设计对话框的肯定按钮文本及其点击监听器
        builder.setPositiveButton("uninstall",(DialogInterface.OnClickListener) (dialog, which) -> {
            tv_message.setText("Although reluctantly, but can only leave");
        });
        // 设置对话框的否定按钮文本及其点击监听器
        builder.setNegativeButton("Let me think about it", (dialog, which) -> {
            tv_message.setText("Let me stay with you three hundred and sixty-five days and nights");
        });
        // 构建提醒对话框对象
        AlertDialog alertDialog = builder.create();
        // 显示提醒对话框
        alertDialog.show();

    }
}