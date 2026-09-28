package com.zzz.myactivityapplication;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReadResourceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_read_resource);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView read_string = findViewById(R.id.read_string);

        String stringData = getString(R.string.string_data);

        read_string.setText(stringData);

        // 读取 strings.xml
        TextView readString = findViewById(R.id.read_string);
        readString.setText(getString(R.string.string_data));

        // 读取 AndroidManifest.xml 中的 metadata
        TextView readMetadata = findViewById(R.id.read_metadata);

        PackageManager packageManager = getPackageManager();

        try {
            ActivityInfo activityInfo = packageManager.getActivityInfo(
                    getComponentName(),
                    PackageManager.GET_META_DATA
            );

            Bundle metaData = activityInfo.metaData;

            if (metaData != null) {
                String metadata = metaData.getString("metadata");
                readMetadata.setText(metadata);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}