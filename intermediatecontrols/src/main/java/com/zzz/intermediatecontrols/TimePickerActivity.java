package com.zzz.intermediatecontrols;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;

public class TimePickerActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_time;
    private TimePicker dialog_time_picker;
    private int second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_picker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dialog_time_picker = findViewById(R.id.dialog_time_picker);
        dialog_time_picker.setIs24HourView(true);
        Button btn_choose_time = findViewById(R.id.btn_choose_time);
        btn_choose_time.setOnClickListener(this);
        Button btn_submit = findViewById(R.id.btn_submit_time);
        btn_submit.setOnClickListener(this);

        tv_time = findViewById(R.id.tv_time);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit_time) {
            String desc = String.format(
                    "The time you selected is %d hours and %d minutes",
                    dialog_time_picker.getHour(),
                    dialog_time_picker.getMinute()
            );
            tv_time.setText(desc);

        }else if (v.getId() == R.id.btn_choose_time) {
            LocalTime now = LocalTime.now();
            int hour = now.getHour();
            int minute1 = now.getMinute();
            second = now.getSecond();
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.R.style.Widget_Holo_ActionBar_Solid,(TimePickerDialog.OnTimeSetListener) (view, hourOfDay, minute) -> {
                String desc = String.format("The time you selected is %d hours, %d minutes and %d seconds",
                        hourOfDay,
                        minute,
                        second);
                tv_time.setText(desc);
            },hour,minute1,true);

            timePickerDialog.show();


        }
    }
}