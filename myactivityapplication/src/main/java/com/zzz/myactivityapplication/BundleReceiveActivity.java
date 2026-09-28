package com.zzz.myactivityapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.myactivityapplication.utils.DateUtils;

public class BundleReceiveActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_receive_receive;
    private Button btn_send_receive;
    private String message = "hello world!";
    private TextView waiting_for_returning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bundle_receive);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_receive_receive = findViewById(R.id.tv_receive_receive);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String responseTime1 = extras.getString("response_time");
        String responseMessage1 = extras.getString("response_message");
        Log.d("response1", responseTime1 + responseMessage1);
        String format1 = String.format("接收到的信息为：\n请求时间：%s,\n请求信息为：%s", responseTime1, responseMessage1);
        tv_receive_receive.setText(format1);
        String responseTime = intent.getStringExtra("response_time");
        String responseMessage = intent.getStringExtra("response_message");
        String format = String.format("接收到的信息为：\n请求时间：%s,\n请求信息为：%s", responseTime, responseMessage);
        Log.d("response", responseTime + responseMessage);
        tv_receive_receive.setText(format);

        btn_send_receive = findViewById(R.id.btn_send_receive);
        btn_send_receive.setOnClickListener(this);

        waiting_for_returning = findViewById(R.id.waiting_for_returning);
        waiting_for_returning.setText("待返回的信息：" + message);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, BundleSendActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("response_time", DateUtils.getNowTime());
        bundle.putString("response_message", message);
        intent.putExtras(bundle);
        //携带意图返回上一个界面。RESULT_OK 表示处理成功
        setResult(Activity.RESULT_OK, intent);
        //结束当前活动页
        finish();
    }

}