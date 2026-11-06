package com.zzz.retrofitapplication;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

/**
 * 全局 Application，Hilt 的容器起点。
 *
 * <p>{@code @HiltAndroidApp} 会让注解处理器在编译期生成组件，并把这个类改写成继承
 * Hilt_MyApplication；所有 {@code @Singleton} 依赖都挂在这个组件上，生命周期与进程一致。
 *
 * <p>三个前提，缺一个就会在运行时报
 * "Hilt Activity must be attached to an @HiltAndroidApp Application"：
 * <ol>
 *   <li>类上加 {@code @HiltAndroidApp}；</li>
 *   <li>清单里 {@code <application android:name=".MyApplication">} 指向它；</li>
 *   <li>模块 apply 了 Hilt Gradle 插件（负责 @AndroidEntryPoint 的字节码插桩）。</li>
 * </ol>
 *
 * <p>注意它**不该放在 di 包**：di 放的是"怎么造依赖"的 Module，而 Application 是应用入口，
 * 属于最外层的宿主类。
 */
@HiltAndroidApp
public class MyApplication extends Application {
}
