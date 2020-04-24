package com.app.coguardapp;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;

public class App extends Application {

    private static final String APP_ID = "a9d929723df03e40fd4a4e513e3616543c03339f";
    private static final String CLIENT_KEY = "cde79a1ba26a016634f09e0669917be36773d868";
    private static final String SERVER_URL = "http:/3.214.66.12/parse";

    @Override
    public void onCreate() {
        super.onCreate();

        initializeParse();

    }

    private void initializeParse(){

        Log.i("PARSE", "Initializing...");
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(APP_ID)
                .clientKey(CLIENT_KEY)
                .server(SERVER_URL)
                .build()
        );

    }

}
