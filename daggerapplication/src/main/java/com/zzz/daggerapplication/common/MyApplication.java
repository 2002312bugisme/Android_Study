package com.zzz.daggerapplication.common;

import android.app.Application;

public class MyApplication extends Application {

    static ApplicationComponent applicationComponent = DaggerApplicationComponent.create();

    public static ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }
}
