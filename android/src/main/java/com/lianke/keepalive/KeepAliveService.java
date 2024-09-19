package com.lianke.keepalive;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.PermissionChecker;

import com.lianke.foreground_service.R;


public class KeepAliveService extends Service {

    private static final String NOTIFICATION_CHANNEL_NAME = "ForegroundService";
    private static final int NOTIFICATION_ID = 10091;


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra("playSilence")) {
            boolean playSilence = intent.getBooleanExtra("playSilence", false);
            if (playSilence) {
                startPlaySilence();
            } else {
                stopPlaySilence();
            }
        }

        if (intent.hasExtra("wakeLock")) {
            boolean wakeLock = intent.getBooleanExtra("wakeLock", false);
            if (wakeLock) {
                acquireWakeLock();
            } else {
                releaseWakeLock();
            }
        }

        if (intent.hasExtra("wifiLock")) {
            boolean wifiLock = intent.getBooleanExtra("wifiLock", false);
            if (wifiLock) {
                acquireWifiLock();
            } else {
                releaseWifiLock();
            }
        }

        if (intent.hasExtra("foreground")) {
            ForegroundServiceConfig foreground = (ForegroundServiceConfig) intent.getSerializableExtra("foreground");
            startForeground(foreground);
        }


        return START_NOT_STICKY;
    }


    void startForeground(ForegroundServiceConfig config) {
        try {
            String NOTIFICATION_CHANNEL_ID = getPackageName() + "_foreground_service";


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setShowBadge(false);
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationChannel.setSound(null, null);
                notificationChannel.setVibrationPattern(null);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    notificationChannel.setAllowBubbles(false);
                }
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            if (config.icon != null && getDrawableResourceId(this, config.icon) != 0) {
                builder.setSmallIcon(getDrawableResourceId(this, config.icon));
            } else {
                builder.setSmallIcon(getApplicationInfo().icon);
            }


            PackageManager packageManager = getPackageManager();
            String title = packageManager.getApplicationLabel(getApplicationInfo()).toString();
            String body = "\u200B";
            if (config.title != null) {
                title = config.title;
            }
            if (config.body != null) {
                body = config.body;
            }

            int visibility = NotificationCompat.VISIBILITY_SECRET;
            if (config.showOnLockscreens) {
                visibility = NotificationCompat.VISIBILITY_PUBLIC;
            }

            builder.setNumber(0)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setSilent(true)
                    .setVisibility(visibility)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setOngoing(true);


            Notification notification = builder.build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //dataSync|mediaPlayback|microphone|connectedDevice|remoteMessaging|location
                int foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK | ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
                if (hasLocationPermission()) {
                    foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
                }
                if (hasMicPermission()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
                    }
                }

                startForeground(NOTIFICATION_ID, notification, foregroundServiceType);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static int getDrawableResourceId(Context context, String name) {
        int id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        if (id == 0) {
            return context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
        }
        return id;
    }

    private boolean hasLocationPermission() {
        int p1 = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int p2 = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (p1 == PackageManager.PERMISSION_DENIED && p2 == PackageManager.PERMISSION_DENIED) {
            return false;
        }
        return true;
    }

    private boolean hasMicPermission() {
        int p1 = PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (p1 == PackageManager.PERMISSION_DENIED) {
            return false;
        }
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseResource();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        releaseResource();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        stopSelf();
        System.exit(0);
    }

    void releaseResource() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
        releaseWakeLock();
        releaseWifiLock();
        stopPlaySilence();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private PowerManager.WakeLock mWakeLock = null;

    /**
     * 获取唤醒锁
     */
    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getPackageName() + ".wakelock");
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

    private void acquireWifiLock() {
        if (wifiLock == null) {
            wifiLock = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, getPackageName() + ".wifilock");
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

    private void startPlaySilence() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.silence);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0, 0);
            mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
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
