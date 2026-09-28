package com.zzz.myactivityapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.myactivityapplication.utils.DateUtils;

public class BundleSendActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView waiting_for_sending;

    private String message = "hello world!";
    private ActivityResultLauncher<Intent> register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bundle_send);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        waiting_for_sending = findViewById(R.id.waiting_for_sending);
        waiting_for_sending.setText("待发送的消息为：" + message);

        Button btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);

        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            if (o != null) {
                System.out.println(o.toString());
                Intent intent = o.getData();

                if (intent != null && o.getResultCode() == Activity.RESULT_OK) {

                    System.out.println(intent.toString());

                    String responseTime = intent.getStringExtra("response_time");
                    String responseMessage = intent.getStringExtra("response_message");
                    String format = String.format("接收到的信息为：\n请求时间：%s,\n请求信息为：%s", responseTime, responseMessage);
                    TextView tv_receive = findViewById(R.id.tv_receive);
                    tv_receive.setText(format);
                }
            }

        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_send) {
            Intent intent = new Intent(this, BundleReceiveActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("response_time", DateUtils.getNowTime());
            bundle.putString("response_message", message);
            intent.putExtras(bundle);
            //不进行这样进行跳转了
            //startActivity(intent);

            register.launch(intent);

        }
    }

}