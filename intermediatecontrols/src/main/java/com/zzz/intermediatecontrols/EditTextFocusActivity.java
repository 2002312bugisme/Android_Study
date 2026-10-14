package com.zzz.intermediatecontrols;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 输入框焦点变化 + 手机号格式校验演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li>用 {@link View.OnFocusChangeListener} 监听输入框「获得 / 失去焦点」，
 *       只在<b>失去焦点</b>时才校验（避免用户还没输完就报错）；</li>
 *   <li>用正则 {@code ^1[3-9]\d{9}$} 校验中国大陆手机号；</li>
 *   <li>校验失败时两种提示方式同时用上：{@code setError}（输入框右侧红叹号 + 气泡）
 *       与 {@link Toast}（屏幕下方浮动提示）。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_edit_text_focus.xml}
 * <p>涉及 API：{@code View#setOnFocusChangeListener}、{@code EditText#getText/setError}、
 * {@code String#matches}、{@code Toast.makeText}
 */
public class EditTextFocusActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    // 被校验的输入框，焦点回调里要读它的内容并回写错误提示，所以提升为成员变量
    private EditText edit_phone;

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给手机号输入框挂焦点监听器。
     *
     * @param savedInstanceState 系统重建本页时回传的上次状态；首次启动为 {@code null}。本页不读写它。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 先调用父类完成窗口/主题等基础初始化
        super.onCreate(savedInstanceState);
        // 开启沉浸式，允许内容延伸到状态栏 / 导航栏区域
        EdgeToEdge.enable(this);
        // 加载布局 XML 作为页面内容
        setContentView(R.layout.activity_edit_text_focus);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 找到待校验的手机号输入框
        edit_phone = findViewById(R.id.edit_phone);
        // 让本 Activity（实现了 View.OnFocusChangeListener）处理焦点变化
        edit_phone.setOnFocusChangeListener(this);
    }

    /**
     * 焦点变化回调，由系统在控件获得或失去焦点时触发。
     *
     * <p>逻辑：只在失去焦点（{@code hasFocus == false}）时做手机号格式校验；
     * 通过则清掉错误提示，不通过则同时用 setError 和 Toast 提示用户。
     *
     * @param v        焦点发生变化的 View，这里就是 {@code edit_phone}。
     *                 本页只给一个控件挂了监听器，所以不靠它区分来源。
     * @param hasFocus true = 刚获得焦点；false = 刚失去焦点（本例只在 false 分支做事）。
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // 只在「失去焦点」时校验：用户填完离开输入框才是合适的校验时机
        if (!hasFocus) {
            // getText() 拿到的是 Editable，toString() 转成普通字符串，trim() 去掉首尾空格
            // （用户很容易误输空格，不 trim 会导致明明看着对却被判错）
            String phone = edit_phone.getText().toString().trim();

            // 正则 ^1[3-9]\d{9}$ 的含义：以 1 开头 → 第二位是 3~9 → 再跟 9 个数字 → 到此结束，共 11 位
            // 注意 Java 字符串里 \d 要写成 \\d（反斜杠需转义）
            if (phone.matches("^1[3-9]\\d{9}$")) {
                // 手机号格式正确
                // 传 null = 清除之前的错误提示（成功后必须清掉，否则红叹号会一直挂着）
                edit_phone.setError(null);
            } else {
                // 手机号格式错误
                // setError 会在输入框右侧显示错误图标，点击后弹气泡显示这段文字
                edit_phone.setError("请输入正确的中国大陆手机号");

                // 再用 Toast 做一次更醒目的提示（Toast 会自动消失，不打断操作）
                // 形参：① this = 上下文 Context（决定依附哪个窗口显示）；
                //       ② 提示文字；③ 显示时长常量（LENGTH_SHORT ≈ 2 秒 / LENGTH_LONG ≈ 3.5 秒）
                Toast.makeText(
                        this,
                        "请输入正确的中国大陆手机号",
                        Toast.LENGTH_SHORT
                ).show();
                // 请求手机号输入框重新获得焦点
                // 已被作者注释掉：一旦强制抢回焦点会打断用户去改其它字段，实际体验不佳
                // edit_phone.requestFocus();
            }
        }
    }
}