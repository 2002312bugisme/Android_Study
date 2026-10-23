package com.zzz.client_application;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.client_application.util.PermissionUtil;

import java.time.LocalDate;

public class MonitorSmsActivity extends AppCompatActivity {

    private SmsGetObserver observer;

    // 申请权限
    private static final String[] PERMISSION_SMS = new String[]{
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS
    };

    public static final int REQUEST_CODE_SMS = 2;
    private static TextView tv_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitor_sms);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tv_message = findViewById(R.id.tv_message);

        // 给指定的Uri注册内容观察器，一旦发生数据变化，就触发观察器的onChange方法
        Uri uri = Uri.parse("content://sms");

        observer = new SmsGetObserver(this);

        if (PermissionUtil.checkPermission(this, PERMISSION_SMS, REQUEST_CODE_SMS)) {
            getContentResolver().registerContentObserver(uri, true, observer);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(observer);
    }

    private static class SmsGetObserver extends ContentObserver {
        private final Context context;

        public SmsGetObserver(Context context) {
            super(new Handler(Looper.getMainLooper()));
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            // onChange会多次调用，收到一条短信会调用两次onChange
            // Android 7.0 以上的系统，点击标记为已读，也会调用一次
            // 收到一条短信都是uri后面会有确定的一个数据，对应数据库中的_id，
            if (uri == null) {
                return;
            }
            if (uri.toString().contains("content://sms/raw") ||
                    uri.toString().equals("content://sms")) {
                return;
            }
            // 通过内容解析器获取符合条件的结果集游标
            Cursor cursor = context.getContentResolver().query(uri, new String[]{"address", "body", "date"}, null, null, "date DESC");
            while (cursor.moveToNext()) {
                // 短信发送号码
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                Log.d("zzz", String.format("address:%s,content:%s", address, body));
                tv_message.setText(String.format("address:%s,content:%s", address, body));
            }

            cursor.close();
        }
    }


}