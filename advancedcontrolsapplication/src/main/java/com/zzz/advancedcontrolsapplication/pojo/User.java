package com.zzz.advancedcontrolsapplication.pojo;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String user_name;
    private int user_age;
    private String user_introduction;

    public User(String user_name, int user_age, String user_introduction) {
        this.user_name = user_name;
        this.user_age = user_age;
        this.user_introduction = user_introduction;
    }

    public String getUserName() {
        return user_name;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public int getUserAge() {
        return user_age;
    }

    public void setUserAge(int user_age) {
        this.user_age = user_age;
    }

    public String getUserIntroduction() {
        return user_introduction;
    }

    public void setUserIntroduction(String user_introduction) {
        this.user_introduction = user_introduction;
    }

    @Override
    public String toString() {
        return "User{user_name='" + user_name + "', user_age=" + user_age
                + ", user_introduction='" + user_introduction + "'}";
    }

    /**
     * 演示数据。真实项目里这一层换成数据库查询或网络请求，方法签名不变。
     */
    public static List<User> getUserList() {
        List<User> users = new ArrayList<>();
        users.add(new User("张三", 28, "热爱 Android 开发，正在学习 Fragment 的动态加载"));
        users.add(new User("李四", 24, "专注 UI 设计，喜欢研究各种控件的交互手感"));
        users.add(new User("王五", 31, "后端转移动端，正在补 Java 基础"));
        return users;
    }
}
