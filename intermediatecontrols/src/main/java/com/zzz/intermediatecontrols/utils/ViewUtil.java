package com.zzz.intermediatecontrols.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 界面相关的工具类（本模块的公用方法集中放这里，和业务 Activity 分开）。
 *
 * <p>目前只有「隐藏软键盘」一个方法，被 {@code EditHideActivity} 在输入位数凑满时调用。
 * 之所以要抽成工具类：取系统服务的代码在每个页面都写一遍太啰嗦，也不利于统一修改。
 */
public class ViewUtil {

    /**
     * 隐藏屏幕上已弹出的输入法软键盘。
     *
     * <p>实现要点：输入法不是 App 自己创建的，而是系统服务，必须先向系统「要」到它，
     * 再通过某个窗口的 token 告诉系统「把作用在这个窗口上的键盘收起来」。
     *
     * @param act  调用方 Activity。作用有二：① 作为 Context 向系统索取服务；
     *             ② 表示「在哪个界面的上下文里」操作键盘，传入的 Activity 不能是 null。
     * @param view 依附的 View —— 用来取 {@code windowToken}（窗口令牌），
     *             系统据此判断该收起哪一个窗口的键盘；
     *             所以要收起某个输入框的键盘时，这里通常就传那个输入框本身，传 null 会不起作用。
     */
    public static void hideOneInputMethod(Activity act , View view){
        // 从系统服务中获取输入法管理器
        // getSystemService 返回的声明类型是 Object，必须强转成 InputMethodManager 才能调用其方法；
        // 常量 Context.INPUT_METHOD_SERVICE 是这套系统服务的名字（字符串 key）
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 关闭屏幕上的输入法软键盘
        // 形参：① view.getWindowToken() = 目标窗口的令牌，定位"收谁的键盘"；
        //       ② 0 = 收起时的附加标志位（flags），传 0 表示无特殊行为，这是最常用的写法
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

}
