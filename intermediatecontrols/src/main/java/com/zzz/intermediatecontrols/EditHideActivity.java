package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.zzz.intermediatecontrols.utils.ViewUtil;

public class EditHideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_hide);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText edit_phone_hide = findViewById(R.id.edit_phone_hide);
        edit_phone_hide.addTextChangedListener(new HideTextWatcher(edit_phone_hide, 11));

        EditText edit_password_hide = findViewById(R.id.edit_password_hide);
        edit_password_hide.addTextChangedListener(new HideTextWatcher(edit_password_hide, 6));


    }

    private class HideTextWatcher implements TextWatcher {


        private EditText mView;
        private int mMaxLength;

        public HideTextWatcher(EditText mView, int mMaxLength) {
            this.mMaxLength = mMaxLength;
            this.mView = mView;
        }


        @Override
        public void afterTextChanged(Editable s) {
            // 不用获取输入内容，Editable已经实现了length()
            // 如果达到要求 关闭输入键盘
            if (s.length() == mMaxLength) {
                // 隐藏输入键盘
                ViewUtil.hideOneInputMethod(EditHideActivity.this,mView);
            }

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }
}