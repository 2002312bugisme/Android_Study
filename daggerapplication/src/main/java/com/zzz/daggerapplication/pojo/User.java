package com.zzz.daggerapplication.pojo;


import jakarta.inject.Inject;

public class User {
    // 第一种方式使用@Inject 注解在构造方法上：就是告知Dagger可以通过构造方法来创建并获取到User实例
    @Inject
    public User() {
    }
}
