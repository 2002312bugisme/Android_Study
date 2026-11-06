package com.zzz.daggerapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.daggerapplication.common.DaggerApplicationComponent;
import com.zzz.daggerapplication.common.MyApplication;
import com.zzz.daggerapplication.service.ApiService;

import jakarta.inject.Inject;

public class SecondActivity extends AppCompatActivity {

    @Inject
    ApiService apiService3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dagger);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //DaggerApplicationComponent.create().inject(this);
        MyApplication.getApplicationComponent().inject(this);
        Log.d("TAG", "onCreate: apiService3" + apiService3);
    }
}
