package com.lianke.foreground_service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * ForegroundServicePlugin
 */
public class ForegroundServicePlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "foreground_service");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("start".equals(call.method)) {
            startService(call.argument("notificationDetail"));
            if (call.hasArgument("wakeLock")) {
                boolean wakeLock = call.argument("wakeLock");
                if (wakeLock) {
                    acquireWakeLock();
                }
            }
        } else if ("stop".equals(call.method)) {
            stopService();
        }
    }


    void startService(Map<String, Object> notificationDetail) {
        Intent service = new Intent(context, ForegroundService.class);
        service.putExtra("notificationDetail", NotificationDetail.fromJson(notificationDetail));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }

    void stopService() {
        Intent service = new Intent(context, ForegroundService.class);
        context.stopService(service);
        releaseWakeLock();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }


    private PowerManager.WakeLock mWakeLock = null;

    /**
     * 获取唤醒锁
     */
    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager mPM = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
                    PowerManager.ON_AFTER_RELEASE, context.getPackageName() + ":ForegroundService");
            if (mWakeLock != null) {
                mWakeLock.acquire();
            }
        }
    }

    /**
     * 释放锁
     */
    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

}
