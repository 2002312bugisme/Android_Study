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

/**
 * 输入框「输入满位数自动收起键盘」演示页。
 *
 * <p>演示要点：
 * <ol>
 *   <li>自定义一个 {@link TextWatcher} 实现类，而不是用匿名内部类——因为要带两个构造参数（目标输入框、长度上限）；</li>
 *   <li>在 {@code afterTextChanged} 里判断「已输入字符数是否达到上限」，达到就调用工具类收起软键盘；</li>
 *   <li>同一个监听器类复用于手机号（11 位）和密码（6 位）两个输入框，差异只由构造参数决定。</li>
 * </ol>
 *
 * <p>对应布局：{@code res/layout/activity_edit_hide.xml}
 * <p>涉及 API：{@code EditText#addTextChangedListener}、{@link TextWatcher}、
 * {@code Editable#length}、{@link ViewUtil#hideOneInputMethod}
 */
public class EditHideActivity extends AppCompatActivity {

    /**
     * Activity 创建入口（生命周期中唯一被本页覆写的方法）。
     *
     * <p>职责：开启沉浸式、加载布局、处理系统栏内边距、给两个输入框各挂一个「限长自动收键盘」监听器。
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
        setContentView(R.layout.activity_edit_hide);
        // 内容已顶到系统栏下，这里把系统栏占掉的尺寸补成根布局内边距，避免被遮挡。
        // 形参：v = 根布局（id=main）；insets = 本次窗口插入信息；返回值原样回传表示继续向后分发
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // 取状态栏 + 导航栏所占区域
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 四方向设为根布局 padding
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 手机号输入框：限制到 11 位就自动收键盘
        EditText edit_phone_hide = findViewById(R.id.edit_phone_hide);
        // 构造参数：① 要监听的输入框本身；② 达到多少位开始收键盘（手机号 11 位）
        edit_phone_hide.addTextChangedListener(new HideTextWatcher(edit_phone_hide, 11));

        // 密码输入框：同一套逻辑，只是把上限换成 6 位
        EditText edit_password_hide = findViewById(R.id.edit_password_hide);
        edit_password_hide.addTextChangedListener(new HideTextWatcher(edit_password_hide, 6));


    }

    /**
     * 自定义文本变化监听器：当输入内容长度刚好达到 {@code mMaxLength} 时隐藏软键盘。
     *
     * <p>写成私有内部类（非匿名）的原因：需要保存「监听哪个输入框」和「长度上限」两份状态，
     * 由构造函数传入，这样一个类就能复用到多个输入框上。
     */
    private class HideTextWatcher implements TextWatcher {


        // 被监听的输入框；隐藏键盘时需要它的 windowToken 来定位窗口
        private EditText mView;
        // 触发收键盘的字符数上限
        private int mMaxLength;

        /**
         * 构造监听器。
         *
         * @param mView      要监听的输入框（隐藏键盘时需要用它的 windowToken 定位窗口）
         * @param mMaxLength 输入达到多少个字符时收起软键盘
         */
        public HideTextWatcher(EditText mView, int mMaxLength) {
            this.mMaxLength = mMaxLength;
            this.mView = mView;
        }


        /**
         * 文本变化<b>之后</b>的回调（三个回调中唯一适合读最终内容的地方）。
         *
         * @param s 变化完成后的可编辑文本对象，可直接用它取长度
         */
        @Override
        public void afterTextChanged(Editable s) {
            // 不用获取输入内容，Editable已经实现了length()
            // 如果达到要求 关闭输入键盘
            // 用 == 而不是 >= ：只在「刚好达到」这一瞬间收键盘，避免用户删改后再输入时被反复打扰
            if (s.length() == mMaxLength) {
                // 隐藏输入键盘
                // 第一个实参传 Activity（Context），第二个实参传目标输入框：工具类内部用它取 windowToken
                ViewUtil.hideOneInputMethod(EditHideActivity.this,mView);
            }

        }

        /**
         * 文本变化<b>之前</b>的回调（可以拿到旧内容与即将发生的替换区间），本页不需要，空实现。
         *
         * @param s     变化前的文本
         * @param start 即将被替换的起始下标
         * @param count 即将被替换掉的字符个数
         * @param after 替换后新增的字符个数
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        /**
         * 文本正在变化的回调，本页不需要，空实现。
         *
         * @param s      变化前的文本
         * @param start  本次变化的起始下标
         * @param before 本次被替换掉的字符个数
         * @param count  本次新增的字符个数
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }
}