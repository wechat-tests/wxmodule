package com.example.wxmodule;

import android.app.Application;

public class App extends Application {
    private static App ins;

    public static App Ins(){
        return ins;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ins = this;
    }
}
