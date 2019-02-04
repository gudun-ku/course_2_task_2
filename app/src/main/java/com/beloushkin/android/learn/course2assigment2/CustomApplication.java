package com.beloushkin.android.learn.course2assigment2;

import android.app.Application;
import android.content.Intent;

public class CustomApplication extends Application {

    //have to start our service before creating main activity
    @Override
    public void onCreate() {
        super.onCreate();
        Intent serviceIntent = new Intent(getApplicationContext(), ProgressService.class);
        startService(serviceIntent);
    }
}
