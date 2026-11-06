package com.zzz.daggerapplication.common;

import com.zzz.daggerapplication.DaggerActivity;
import com.zzz.daggerapplication.SecondActivity;
import com.zzz.daggerapplication.module.NetModule;

import dagger.Component;
import jakarta.inject.Singleton;

@Singleton
@Component(modules = {NetModule.class})
public interface ApplicationComponent {

    void inject(DaggerActivity DaggerActivity);

    void inject(SecondActivity secondActivity);
}
