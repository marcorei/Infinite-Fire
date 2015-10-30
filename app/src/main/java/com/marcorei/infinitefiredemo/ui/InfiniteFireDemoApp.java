package com.marcorei.infinitefiredemo.ui;

import com.firebase.client.Firebase;

public class InfiniteFireDemoApp extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
