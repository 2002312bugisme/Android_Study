package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RadioButtonActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private TextView radioBtn_choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_radio_button);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RadioGroup radioBtn_gender = findViewById(R.id.radioBtn_gender);
        RadioButton radioBtn_man = findViewById(R.id.radioBtn_man);
        RadioButton radioBtn_woman = findViewById(R.id.radioBtn_woman);
        radioBtn_gender.setOnCheckedChangeListener(this);

        radioBtn_choice = findViewById(R.id.radioBtn_choice);
    }

    @Override
    public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {
        String choice = null;
        if (checkedId == R.id.radioBtn_man){
            choice = "Man";
        } else if (checkedId == R.id.radioBtn_woman) {
            choice = "Woman";
        }
        radioBtn_choice.setText(String.format("Your Choice: %s",choice));
    }
}