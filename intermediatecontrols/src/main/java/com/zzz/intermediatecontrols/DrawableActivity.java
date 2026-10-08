package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DrawableActivity extends AppCompatActivity implements View.OnClickListener {

    private View view_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_drawable);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        view_content = findViewById(R.id.view_content);
        Button btn_view_rectangle = findViewById(R.id.btn_view_rectangle);
        Button btn_view_oval = findViewById(R.id.btn_view_oval);

        btn_view_rectangle.setOnClickListener(this);

        btn_view_oval.setOnClickListener(this);

        view_content.setBackgroundResource(R.drawable.shape_rectangle);


    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_view_rectangle){
            view_content.setBackgroundResource(R.drawable.shape_rectangle);
        } else if (v.getId() == R.id.btn_view_oval) {
            view_content.setBackgroundResource(R.drawable.shape_oval);
        }
    }
}