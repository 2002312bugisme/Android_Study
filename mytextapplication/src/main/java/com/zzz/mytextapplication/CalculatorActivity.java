package com.zzz.mytextapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CalculatorActivity extends AppCompatActivity implements View.OnClickListener {

    // 显示区域
    private TextView tvDisplay;

    // 计算器状态
    private double firstNumber = 0;
    private String operator = "";
    private boolean newNumber = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calculator);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        // 获取显示控件
        tvDisplay = findViewById(R.id.tv_display);

        // 获取按钮
        Button btnClearEntry = findViewById(R.id.btn_clear_entry);
        Button btnDivide = findViewById(R.id.btn_divide);
        Button btnMultiply = findViewById(R.id.btn_multiply);
        Button btnClear = findViewById(R.id.btn_clear);

        Button btn7 = findViewById(R.id.btn_7);
        Button btn8 = findViewById(R.id.btn_8);
        Button btn9 = findViewById(R.id.btn_9);
        Button btnAdd = findViewById(R.id.btn_add);

        Button btn4 = findViewById(R.id.btn_4);
        Button btn5 = findViewById(R.id.btn_5);
        Button btn6 = findViewById(R.id.btn_6);
        Button btnSubtract = findViewById(R.id.btn_subtract);

        Button btn1 = findViewById(R.id.btn_1);
        Button btn2 = findViewById(R.id.btn_2);
        Button btn3 = findViewById(R.id.btn_3);
        Button btnSqrt = findViewById(R.id.btn_sqrt);

        Button btnReciprocal = findViewById(R.id.btn_reciprocal);
        Button btn0 = findViewById(R.id.btn_0);
        Button btnDecimal = findViewById(R.id.btn_decimal);
        Button btnEqual = findViewById(R.id.btn_equal);

        // 给所有按钮绑定点击事件
        btnClearEntry.setOnClickListener(this);
        btnDivide.setOnClickListener(this);
        btnMultiply.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btnSubtract.setOnClickListener(this);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btnSqrt.setOnClickListener(this);

        btnReciprocal.setOnClickListener(this);
        btn0.setOnClickListener(this);
        btnDecimal.setOnClickListener(this);
        btnEqual.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        // =========================
        // 数字
        // =========================

        if (id == R.id.btn_0) {
            inputNumber("0");

        } else if (id == R.id.btn_1) {
            inputNumber("1");

        } else if (id == R.id.btn_2) {
            inputNumber("2");

        } else if (id == R.id.btn_3) {
            inputNumber("3");

        } else if (id == R.id.btn_4) {
            inputNumber("4");

        } else if (id == R.id.btn_5) {
            inputNumber("5");

        } else if (id == R.id.btn_6) {
            inputNumber("6");

        } else if (id == R.id.btn_7) {
            inputNumber("7");

        } else if (id == R.id.btn_8) {
            inputNumber("8");

        } else if (id == R.id.btn_9) {
            inputNumber("9");

        }

        // =========================
        // 小数点
        // =========================

        else if (id == R.id.btn_decimal) {
            inputDecimal();
        }

        // =========================
        // 四则运算
        // =========================

        else if (id == R.id.btn_add) {
            setOperator("+");

        } else if (id == R.id.btn_subtract) {
            setOperator("-");

        } else if (id == R.id.btn_multiply) {
            setOperator("*");

        } else if (id == R.id.btn_divide) {
            setOperator("/");

        }

        // =========================
        // 等于
        // =========================

        else if (id == R.id.btn_equal) {
            calculate();
        }

        // =========================
        // C：全部清除
        // =========================

        else if (id == R.id.btn_clear) {
            clearAll();
        }

        // =========================
        // CE：清除当前数字
        // =========================

        else if (id == R.id.btn_clear_entry) {
            clearEntry();
        }

        // =========================
        // 开平方
        // =========================

        else if (id == R.id.btn_sqrt) {
            squareRoot();
        }

        // =========================
        // 1/X
        // =========================

        else if (id == R.id.btn_reciprocal) {
            reciprocal();
        }
    }


    // 输入数字
    private void inputNumber(String number) {

        if (newNumber) {
            tvDisplay.setText(number);
            newNumber = false;
        } else {
            tvDisplay.append(number);
        }
    }


    // 输入小数点
    private void inputDecimal() {

        String current = tvDisplay.getText().toString();

        if (newNumber) {
            tvDisplay.setText("0.");
            newNumber = false;
        } else if (!current.contains(".")) {
            tvDisplay.append(".");
        }
    }


    // 选择运算符
    private void setOperator(String operator) {

        firstNumber = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        this.operator = operator;

        newNumber = true;
    }


    // 计算
    private void calculate() {

        if (operator.isEmpty()) {
            return;
        }

        double secondNumber = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        double result;

        switch (operator) {

            case "+":
                result = firstNumber + secondNumber;
                break;

            case "-":
                result = firstNumber - secondNumber;
                break;

            case "*":
                result = firstNumber * secondNumber;
                break;

            case "/":

                if (secondNumber == 0) {
                    tvDisplay.setText("Error");
                    operator = "";
                    newNumber = true;
                    return;
                }

                result = firstNumber / secondNumber;
                break;

            default:
                return;
        }

        tvDisplay.setText(formatResult(result));

        operator = "";
        newNumber = true;
    }


    // C
    private void clearAll() {

        tvDisplay.setText("0");

        firstNumber = 0;
        operator = "";
        newNumber = true;
    }


    // CE
    private void clearEntry() {

        tvDisplay.setText("0");

        newNumber = true;
    }


    // 开平方
    private void squareRoot() {

        double number = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        if (number < 0) {
            tvDisplay.setText("Error");
            newNumber = true;
            return;
        }

        double result = Math.sqrt(number);

        tvDisplay.setText(formatResult(result));

        newNumber = true;
    }


    // 1/X
    private void reciprocal() {

        double number = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        if (number == 0) {
            tvDisplay.setText("Error");
            newNumber = true;
            return;
        }

        double result = 1 / number;

        tvDisplay.setText(formatResult(result));

        newNumber = true;
    }


    // 格式化结果
    private String formatResult(double result) {

        // 如果结果实际上是整数，例如 5.0
        // 显示成 5，而不是 5.0
        if (result == (long) result) {
            return String.valueOf((long) result);
        }

        return String.valueOf(result);
    }
}