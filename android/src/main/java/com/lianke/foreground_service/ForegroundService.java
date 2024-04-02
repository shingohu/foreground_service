package com.lianke.foreground_service;

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
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.PermissionChecker;


public class ForegroundService extends Service {

    private static final String NOTIFICATION_CHANNEL_NAME = "ForegroundService";
    private static final int NOTIFICATION_ID = 10091;


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationDetail detail = (NotificationDetail) intent.getSerializableExtra("notificationDetail");
        startForeground(detail);
        return START_NOT_STICKY;
    }


    void startForeground(NotificationDetail notificationDetail) {
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
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    notificationChannel.setAllowBubbles(false);
                }
                notificationManager.createNotificationChannel(notificationChannel);
            }

            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            builder.setNumber(0)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setOngoing(true);
            if (notificationDetail.icon != null) {
                builder.setSmallIcon(getDrawableResourceId(this, notificationDetail.icon));
            }
            if (notificationDetail.title != null) {
                builder.setContentTitle(notificationDetail.title);
            }
            if (notificationDetail.body != null) {
                builder.setContentText(notificationDetail.body);
            }

            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);


            Notification notification = builder.build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //dataSync|mediaPlayback|microphone|connectedDevice|remoteMessaging|location
                int foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
                if (hasLocationPermission()) {
                    foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
                } else if (hasMicPermission()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        stopSelf();
        System.exit(0);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
