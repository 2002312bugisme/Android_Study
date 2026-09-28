package com.zzz.myactivityapplication;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IntentActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn_displayed_intent = findViewById(R.id.btn_displayed_intent);
        btn_displayed_intent.setOnClickListener(this);

        Button btn_implicit_intent_tel = findViewById(R.id.btn_implicit_intent_tel);
        btn_implicit_intent_tel.setOnClickListener(this);

        Button btn_implicit_intent_msg = findViewById(R.id.btn_implicit_intent_msg);
        btn_implicit_intent_msg.setOnClickListener(this);

        Button btn_implicit_intent_my = findViewById(R.id.btn_implicit_intent_my);
        btn_implicit_intent_my.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String phoneNo = "123456789";
        Intent intent;
        if(v.getId() == R.id.btn_displayed_intent){
            intent = new Intent();
            ComponentName component = new ComponentName(this, ActFinishActivity.class);
            intent.setComponent(component);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_implicit_intent_tel) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_DIAL);
            Uri uri = Uri.parse("tel:" + phoneNo);
            intent.setData(uri);
            startActivity(intent);
        }else if (v.getId() == R.id.btn_implicit_intent_msg) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_SENDTO);
            Uri uri = Uri.parse("smsto:" + phoneNo);
            intent.setData(uri);
            startActivity(intent);
        }else if (v.getId() == R.id.btn_implicit_intent_my) {
            intent = new Intent();
            intent.setAction("android.intent.action.ZZZ");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }
    }
}