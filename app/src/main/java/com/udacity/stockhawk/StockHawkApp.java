package com.udacity.stockhawk;

import android.app.Application;

import timber.log.Timber;

public class StockHawkApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        /* Timber library to making debug easier , initiate log msg with class name
           use :     compile 'com.jakewharton.timber:timber:4.1.2'
           https://github.com/JakeWharton/timber
         */

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }
}
