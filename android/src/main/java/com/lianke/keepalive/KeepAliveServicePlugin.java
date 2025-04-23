package com.lianke.keepalive;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;

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

    /// 多引擎实例下,谁创建谁管理
    private boolean isStartByThisEngine = false;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "keep_alive_service");
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.getApplicationContext();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if ("start".equals(call.method)) {
            if (call.hasArgument("foreground")) {
                Intent service = new Intent(context, KeepAliveService.class);
                service.putExtra("foreground", ForegroundServiceConfig.fromJson(call.argument("foreground")));
                startService(service);
            } else if (call.hasArgument("wakeLock")) {
                boolean wakeLock = (boolean) call.argument("wakeLock");
                if (wakeLock) {
                    acquireWakeLock(context);
                } else {
                    releaseWakeLock();
                }

            } else if (call.hasArgument("wifiLock")) {

                boolean wifiLock = (boolean) call.argument("wifiLock");
                if (wifiLock) {
                    acquireWifiLock(context);
                } else {
                    releaseWifiLock();
                }

            } else if (call.hasArgument("playSilence")) {
                boolean playSilence = (boolean) call.argument("playSilence");
                if (playSilence) {
                    startPlaySilence(context);
                } else {
                    stopPlaySilence();
                }
            }
        } else if ("stop".equals(call.method)) {
            stopService();
        }
    }


    void startService(Intent service) {
        isStartByThisEngine = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }

    void stopService() {
        if (isStartByThisEngine) {
            if (KeepAliveService.hasStartForegroundService) {
                Intent service = new Intent(context, KeepAliveService.class);
                context.stopService(service);
            }
            isStartByThisEngine = false;
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        stopService();
        stopPlaySilence();
        releaseWifiLock();
        releaseWakeLock();
    }


    private PowerManager.WakeLock mWakeLock = null;

    /**
     * 获取唤醒锁
     */
    private void acquireWakeLock(Context context) {
        if (mWakeLock == null) {
            PowerManager mPM = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context.getPackageName() + ".wakelock");
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

    private WifiManager.WifiLock wifiLock;

    private void acquireWifiLock(Context context) {
        if (wifiLock == null) {
            wifiLock = ((WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, context.getPackageName() + ".wifilock");
            if (wifiLock != null) {
                wifiLock.acquire();
            }
        }
    }

    private void releaseWifiLock() {
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
    }


    private MediaPlayer mediaPlayer;

    private void startPlaySilence(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.silence);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.start();
        }
    }

    private void stopPlaySilence() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }


}
