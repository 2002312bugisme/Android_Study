package com.zzz.daggerapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.daggerapplication.common.ApplicationComponent;
import com.zzz.daggerapplication.common.DaggerApplicationComponent;
import com.zzz.daggerapplication.common.MyApplication;
import com.zzz.daggerapplication.pojo.User;
import com.zzz.daggerapplication.service.ApiService;

import jakarta.inject.Inject;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class DaggerActivity extends AppCompatActivity {

    @Inject
    User user1;
    @Inject
    User user2;
    @Inject
    User user3;

    @Inject
    OkHttpClient okHttpClient;

    @Inject
    Retrofit retrofit;

    @Inject
    ApiService apiService1;
    @Inject
    ApiService apiService2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dagger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 执行注册动作，需对项目进行重新编译 compile
        //DaggerApplicationComponent.create().inject(this);

        MyApplication.getApplicationComponent().inject(this);

        Log.d("TAG", "onCreate: user1" + user1);
        Log.d("TAG", "onCreate: user2" + user2);
        Log.d("TAG", "onCreate: user3" + user3);
        Log.d("TAG", "onCreate: okHttpClient" + okHttpClient);
        Log.d("TAG", "onCreate: retrofit" + retrofit);
        Log.d("TAG", "onCreate: apiService1" + apiService1);
        Log.d("TAG", "onCreate: apiService2" + apiService2);

        startActivity(new Intent(this, SecondActivity.class));
    }
}