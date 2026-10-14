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

/**
 * 简易计算器演示页（本模块中逻辑最完整的一个类，可当作"第一个真正的案例"来读）。
 *
 * <p>整体设计思路：把"计算器的记忆"压缩成三个成员变量，所有按钮只改这三个值、再刷新显示。
 * <pre>
 *   firstNumber —— 已经输入完成的第一个操作数（例如 3 + 4 中的 3）
 *   operator    —— 待执行的运算符（"+" "-" "*" "/"；空串表示"当前没有待算的运算"）
 *   newNumber   —— 关键开关：true 表示"下一次输入数字要重新开始，而不是接着往上拼"
 * </pre>
 *
 * <p>为什么 <b>newNumber</b> 是整个计算器的灵魂（用输入 3 + 4 = 说明）：
 * <ol>
 *   <li>输入 3 → newNumber 为 false，数字拼到显示区得到 "3"；</li>
 *   <li>点 "+" → 把 "3" 存进 firstNumber、记下运算符 "+"，并把 newNumber 置 true；</li>
 *   <li>输入 4 → 因为 newNumber 是 true，显示区被<b>清空重写</b>为 "4"（而不是变成 "34"）；</li>
 *   <li>点 "=" → 用 firstNumber 和显示区当前值做运算，写回结果，并把 newNumber 再置 true，
 *       这样下一次输入数字又是从新数字开始。</li>
 * </ol>
 *
 * <p>已知的写法上的简化（照实说明，方便你自己判断哪些要改进）：
 * <ul>
 *   <li>连续运算不支持"边输边算"（如 3+4+5 需要在中间按一次 =）；</li>
 *   <li>{@code Double.parseDouble} 用的是<b>默认区域设置</b>，在把逗号当小数点的地区会有隐患，
 *       而且显示区内容是 "Error" 时再解析会抛异常；</li>
 *   <li>20 个按钮逐个 findViewById + setOnClickListener，代码重复度高，
 *       练习时可改为遍历按钮数组，或统一写 {@code android:onClick}。</li>
 * </ul>
 *
 * <p>对应布局：{@code res/layout/activity_calculator.xml}
 * <p>涉及 API：{@code TextView#setText/append/getText}、{@code Double.parseDouble}、
 * {@code Math.sqrt}、{@code String#valueOf}、{@code View.OnClickListener}
 */
public class CalculatorActivity extends AppCompatActivity implements View.OnClickListener {

    // 显示区域
    // 计算器上方那块显示结果的 TextView，所有运算的输入与输出都经过它
    private TextView tvDisplay;

    // 计算器状态
    // 已输入完成的第一个操作数（如 3 + 4 中的 3）；初值 0 保证"直接点 = "不会崩
    private double firstNumber = 0;
    // 待执行的运算符，空串表示当前没有待算的运算（calculate 靠 isEmpty 判断是否该返回）
    private String operator = "";
    // 下一次输入数字时是否"另起一个新数"：true = 覆盖显示区，false = 追加到显示区后面
    private boolean newNumber = true;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、缓存显示控件与 20 个按钮并统一挂监听器。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。
     *                           注意本页<b>没有</b>保存/恢复计算器状态，所以屏幕旋转后
     *                           firstNumber / operator 会随 Activity 重建而丢失（属于未完善之处）。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);

        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_calculator);

        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding（这里为了可读性分多行书写，效果与单行一致）
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
        // 第 1 行：功能键 + 除乘
        Button btnClearEntry = findViewById(R.id.btn_clear_entry);
        Button btnDivide = findViewById(R.id.btn_divide);
        Button btnMultiply = findViewById(R.id.btn_multiply);
        Button btnClear = findViewById(R.id.btn_clear);

        // 第 2 行：7 8 9 +
        Button btn7 = findViewById(R.id.btn_7);
        Button btn8 = findViewById(R.id.btn_8);
        Button btn9 = findViewById(R.id.btn_9);
        Button btnAdd = findViewById(R.id.btn_add);

        // 第 3 行：4 5 6 -
        Button btn4 = findViewById(R.id.btn_4);
        Button btn5 = findViewById(R.id.btn_5);
        Button btn6 = findViewById(R.id.btn_6);
        Button btnSubtract = findViewById(R.id.btn_subtract);

        // 第 4 行：1 2 3 √
        Button btn1 = findViewById(R.id.btn_1);
        Button btn2 = findViewById(R.id.btn_2);
        Button btn3 = findViewById(R.id.btn_3);
        Button btnSqrt = findViewById(R.id.btn_sqrt);

        // 第 5 行：1/X 0 . =
        Button btnReciprocal = findViewById(R.id.btn_reciprocal);
        Button btn0 = findViewById(R.id.btn_0);
        Button btnDecimal = findViewById(R.id.btn_decimal);
        Button btnEqual = findViewById(R.id.btn_equal);

        // 给所有按钮绑定点击事件
        // 全部指向本 Activity（已 implements View.OnClickListener），
        // 于是 20 个按钮共用同一个 onClick，靠 v.getId() 分流
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

    /**
     * 20 个按钮共用的点击回调——本计算器的"总调度台"。
     *
     * <p>逻辑：把 id 拿到本地变量（避免反复调用 {@code v.getId()}），然后一路
     * {@code if / else if} 分发到对应的私有方法。注意整体是<b>一条</b> if-else 链，
     * 中间穿插的注释行看起来像"分节"，但 else if 仍属于同一条件链，命中一个就不会再往下走。
     *
     * @param v 被点击的那个按钮（20 个中任意一个）。必须用 {@code v.getId()} 判断是哪个键。
     */
    @Override
    public void onClick(View v) {

        // 把 id 缓存到局部变量：一是少一次方法调用，二是后面几十次比较都能直接读局部变量
        int id = v.getId();

        // =========================
        // 数字
        // =========================

        // 0~9 共 10 个分支，逻辑完全一样，只是传入的字符不同 ——
        // 这正是可以进一步优化的地方（例如把 id 与字符做成映射或数组遍历）
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

        // 点运算符时把"="右边要用的那个运算符传给 setOperator 记下来
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
    /**
     * 追加/覆盖一个数字字符。
     *
     * <p>逻辑：由 {@code newNumber} 决定是"另起一个新数"还是"接在原来的数后面"。
     *
     * @param number 要输入的单个数字字符（"0"~"9"）。
     *               用 String 而不是 int 是为了直接交给 {@code setText/append} 使用，
     *               也避免前导零被 int 抹掉（例如用户想输入 007）。
     */
    private void inputNumber(String number) {

        if (newNumber) {
            // 上一轮运算已结束（或刚点过运算符）：覆盖显示区，从头输入这个新数
            tvDisplay.setText(number);
            // 之后继续按数字键就应该是"接着拼"而不是再覆盖
            newNumber = false;
        } else {
            // 正在输入同一个数的过程中：把字符追加到已有内容末尾
            // append 内部相当于 setText(旧内容 + 新内容)
            tvDisplay.append(number);
        }
    }


    // 输入小数点
    /**
     * 输入小数点，并保证同一个数字里不会出现两个 "."。
     *
     * <p>逻辑分三种情况处理，避免出现 "1.2.3" 这种非法数字。
     */
    private void inputDecimal() {

        // 取显示区当前文本（getText 返回 Editable，需要 toString 才是普通字符串）
        String current = tvDisplay.getText().toString();

        if (newNumber) {
            // 情况一：正要开始一个新数 —— 直接给个 "0."，
            // 这样用户只按 "." 也能得到合法的 0.（否则单一个 "." 无法被 parseDouble 解析）
            tvDisplay.setText("0.");
            newNumber = false;
        } else if (!current.contains(".")) {
            // 情况二：当前数里还没有小数点 —— 才允许追加
            // contains 判断的是子串，这里够用；若严格校验可用正则
            tvDisplay.append(".");
        }
        // 情况三（隐含）：当前数里已有小数点 —— 什么都不做，即忽略这次点击
    }


    // 选择运算符
    /**
     * 记录待执行的运算符，并把当前显示的数当作"第一个操作数"存起来。
     *
     * <p>这是"两步计算"模型的关键一步：点运算符时先把左操作数与运算符暂存，
     * 真正的运算推迟到点 "=" 时进行。
     *
     * @param operator 运算符字符串："+" "-" "*" "/"。
     *                 形参名与同名字段一致，所以方法体内必须用 {@code this.operator} 才能赋值给字段，
     *                 否则会变成"把形参赋给它自己"而完全无效（这是极易踩的坑）。
     */
    private void setOperator(String operator) {

        // 把显示区当前的文字解析成 double 作为第一个操作数。
        // 之所以能放心解析：显示区在正常输入下一定是合法数字文本
        firstNumber = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        // 注意 this. 不能省：左边是成员字段，右边是形参
        this.operator = operator;

        // 置 true：下一次按数字键要"另起一个新数"，而不是接在第一个操作数后面
        newNumber = true;
    }


    // 计算
    /**
     * 执行运算并把结果写回显示区。
     *
     * <p>逻辑：取第二个操作数 → 按运算符做四则运算 → 格式化输出 → 复位状态。
     * 除零是唯一需要显式保护的异常情况（浮点除零不会抛异常，但会得到 Infinity，所以要自己判断）。
     */
    private void calculate() {

        // 没有任何待算的运算符（例如刚进页面就点 "="）→ 直接什么都不做，避免把显示值当第二个操作数瞎算
        if (operator.isEmpty()) {
            return;
        }

        // 第二个操作数 = 显示区当前文字
        double secondNumber = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        // 先声明，下面各分支负责赋值；这种"先声明后赋值"的写法在 switch 里很常见
        double result;

        // 用字符串做 switch 是 Java 7 起支持的特性（老版本只能用 int 或 if-else）
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

                // 除数为 0 的保护：Android 上 double 除以 0 不会抛异常而是得到 Infinity/NaN，
                // 那样的结果对用户没有意义，所以这里主动报错
                if (secondNumber == 0) {
                    tvDisplay.setText("Error");
                    // 复位状态，让用户可以直接输入新算式；
                    // 注意这里<b>没有</b>清 firstNumber —— 但它已不会被用到，因为 operator 被清空了
                    operator = "";
                    newNumber = true;
                    return;
                }

                result = firstNumber / secondNumber;
                break;

            // 兜底：运算符异常时直接返回（正常情况下不会走到）
            default:
                return;
        }

        // 把 double 结果按"整数就不带小数点"的规则格式化后显示
        tvDisplay.setText(formatResult(result));

        // 复位：清掉运算符，并让下一次数字输入另起一个新数
        operator = "";
        newNumber = true;
    }


    // C
    /**
     * 全部清除（C 键）：把计算器恢复到刚打开的状态。
     *
     * <p>与 {@link #clearEntry()} 的区别：C 会连"待算的运算符"和"第一个操作数"一起清掉，
     * 相当于放弃整个算式。
     */
    private void clearAll() {

        // 显示区回到 0
        tvDisplay.setText("0");

        firstNumber = 0;
        operator = "";
        newNumber = true;
    }


    // CE
    /**
     * 清除当前数字（CE 键）：只把显示区清零，<b>保留</b>已输入的运算符和第一个操作数。
     *
     * <p>使用场景：想重输第二个操作数，但不想重来整个算式（如 100 + 输错 → 按 CE 重输）。
     */
    private void clearEntry() {

        tvDisplay.setText("0");

        // 置 true：下一次输入数字要覆盖这个 0，而不是追加成 "05" 之类
        newNumber = true;
    }


    // 开平方
    /**
     * 对显示区当前数开平方（√ 键）。
     *
     * <p>逻辑：解析当前值 → 负数报错 → 求平方根 → 格式化显示。
     */
    private void squareRoot() {

        double number = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        // 负数在实数范围内没有平方根 —— 主动报错，否则 Math.sqrt 会返回 NaN
        if (number < 0) {
            tvDisplay.setText("Error");
            newNumber = true;
            return;
        }

        // Math.sqrt 是 java.lang.Math 的静态方法，直接传参返回 double
        double result = Math.sqrt(number);

        tvDisplay.setText(formatResult(result));

        // 结果是"已经算好"的数，下一次输入数字应另起新数
        newNumber = true;
    }


    // 1/X
    /**
     * 求显示区当前数的倒数（1/X 键）。
     *
     * <p>逻辑：解析当前值 → 判断是否为零 → 求倒数 → 格式化显示。
     */
    private void reciprocal() {

        double number = Double.parseDouble(
                tvDisplay.getText().toString()
        );

        // 0 没有倒数（1/0 在浮点运算里会得到 Infinity），主动报错
        if (number == 0) {
            tvDisplay.setText("Error");
            newNumber = true;
            return;
        }

        // 注意这里 1 是 int、number 是 double，运算时会自动提升为 double 除法，
        // 不会出现整数除法把 1/2 算成 0 的问题
        double result = 1 / number;

        tvDisplay.setText(formatResult(result));

        newNumber = true;
    }


    // 格式化结果
    /**
     * 把 double 结果格式化成"好看"的字符串。
     *
     * <p>解决的问题：{@code double} 没有整数概念，3 + 4 会得到 5.0，
     * 直接显示 "5.0" 不符合计算器的使用习惯，所以整数结果要去掉小数点。
     *
     * @param result 待格式化的运算结果。
     * @return 若 result 是整数值（如 5.0）返回不带小数点的 "5"；否则返回 {@code String.valueOf} 的原样输出。
     */
    private String formatResult(double result) {

        // 如果结果实际上是整数，例如 5.0
        // 显示成 5，而不是 5.0

        // 判断技巧：把 double 强制转 long 会<b>截断</b>小数部分。
        // 若截断前后相等，说明小数部分本来就是 0，即"数学上是整数"。
        // 注意 double 用 == 比较在浮点误差下通常有风险，但这里比较的是同一个值的两种转换，
        // 不存在精度损失问题，是安全的
        if (result == (long) result) {
            return String.valueOf((long) result);
        }

        // 带小数的结果直接输出（受 double 精度限制，0.1+0.2 会显示 0.30000000000000004，
        // 这是浮点数的固有特性，若要更友好可改用 BigDecimal 或 String.format("%.10f"))
        return String.valueOf(result);
    }
}