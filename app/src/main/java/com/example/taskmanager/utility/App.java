package com.example.taskmanager.utility;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.time.format.DateTimeFormatter;

/**
 * Utility class which allows access to the application context from anywhere.
 */
public class App extends Application {

    //Using weak reference to avoid potential memory leaks
    private static WeakReference<Context> mContext;
    public final static DateTimeFormatter APP_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public final static DateTimeFormatter APP_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy H:mm");

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<>(this);
    }

    public static Context getContext(){
        return mContext.get();
    }
}
