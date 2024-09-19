package com.lianke.keepalive;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

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
public class KeepAliveServicePlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "keep_alive_service");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("start".equals(call.method)) {
            Intent service = new Intent(context, KeepAliveService.class);
            if (call.hasArgument("foreground")) {
                service.putExtra("foreground", ForegroundServiceConfig.fromJson(call.argument("foreground")));
            } else if (call.hasArgument("wakeLock")) {
                service.putExtra("wakeLock", (boolean) call.argument("wakeLock"));
            } else if (call.hasArgument("wifiLock")) {
                service.putExtra("wifiLock", (boolean) call.argument("wifiLock"));
            } else if (call.hasArgument("playSilence")) {
                service.putExtra("playSilence", (boolean) call.argument("playSilence"));
            }
            startService(service);
        } else if ("stop".equals(call.method)) {
            stopService();
        }
    }


    void startService(Intent service) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }

    void stopService() {
        Intent service = new Intent(context, KeepAliveService.class);
        context.stopService(service);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

}
