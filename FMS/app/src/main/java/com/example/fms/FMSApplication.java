package com.example.fms;

import android.app.Application;
import android.util.Log;

import com.example.fms.firebase.FirebaseManager;

public class FMSApplication extends Application {
    private static final String TAG = "FMSApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Khởi tạo Firebase
        FirebaseManager.initialize(this);
        Log.d(TAG, "Firebase initialized");
    }
} 