package com.zzz.intermediatecontrols;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.Month;

public class DatePickerActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_date;
    private DatePicker dialog_date_picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_date_picker);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dialog_date_picker = findViewById(R.id.dialog_date_picker);
        Button btn_choose_date = findViewById(R.id.btn_choose_date);
        btn_choose_date.setOnClickListener(this);
        Button btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);

        tv_date = findViewById(R.id.tv_date);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit) {
            String desc = String.format("The date you selected is %d year %d month %d day",
                    dialog_date_picker.getYear(),
                    dialog_date_picker.getMonth() + 1,
                    dialog_date_picker.getDayOfMonth());
            tv_date.setText(desc);
        } else if (v.getId() == R.id.btn_choose_date) {
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            Month monthNum = now.getMonth();
            int month = monthNum.getValue() - 1;
            int day = now.getDayOfMonth();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String desc = String.format("The date you selected is %d year %d month %d day",
                        year1,
                        month1,
                        dayOfMonth);
                tv_date.setText(desc);
            }, year, month, day);

            dialog.show();

        }

    }
}