package com.mrugen_practicle;

import android.app.Application;

import com.mrugen_practicle.di.components.ApplicationComponent;
import com.mrugen_practicle.di.components.DaggerApplicationComponent;
import com.mrugen_practicle.di.modules.ApplicationModule;
import com.mrugen_practicle.models.MyObjectBox;

import io.objectbox.BoxStore;

public class MyApplication extends Application {

    private static ApplicationComponent applicationComponent;

    private BoxStore boxStore;

    public MyApplication() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        applicationComponent = DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        boxStore = MyObjectBox.builder().androidContext(MyApplication.this).build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public BoxStore getBoxStore() {
        return boxStore;
    }
}
