package com.lianke.keepalive;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
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

import java.util.List;


public class KeepAliveService extends Service {

    private static final String NOTIFICATION_CHANNEL_NAME = "ForegroundService";
    private static final int NOTIFICATION_ID = 10091;

    public static boolean hasStartForegroundService = false;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
                int foregroundServiceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE;

                //ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK | ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
                if (this.hasPermissionInManifest(Manifest.permission.FOREGROUND_SERVICE_CAMERA)) {
                    foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;
                }

                if (this.hasPermissionInManifest(Manifest.permission.FOREGROUND_SERVICE_REMOTE_MESSAGING)) {
                    foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING;
                }


                if (this.hasPermissionInManifest(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)) {
                    foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;
                }

                if (this.hasPermissionInManifest(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)) {
                    foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK;
                }


                if (this.hasPermissionInManifest(Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE)) {
                    foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE;
                }


                if (this.hasPermissionInManifest(Manifest.permission.FOREGROUND_SERVICE_LOCATION)) {
                    if (hasLocationPermission()) {
                        foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
                    }
                }
                if (this.hasPermissionInManifest(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)) {
                    if (hasMicPermission()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            foregroundServiceType = foregroundServiceType | ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
                        }
                    }
                }
                startForeground(NOTIFICATION_ID, notification, foregroundServiceType);
                hasStartForegroundService = true;
            } else {
                startForeground(NOTIFICATION_ID, notification);
                hasStartForegroundService = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            hasStartForegroundService = false;
        }
    }

    public static String[] permissions = null;

    public boolean hasPermissionInManifest(String permission) {

        try {
            if (permissions == null) {
                PackageManager packageManager = getPackageManager();
                PackageInfo info = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
                permissions = info.requestedPermissions;
            }
            if (permissions != null) {
                for (int i = 0; i < permissions.length; i++) {
                    String name = permissions[i];
                    if (name.equals(permission)) {
                        return true;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
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
            if (hasStartForegroundService) {
                stopForeground(STOP_FOREGROUND_REMOVE);
                hasStartForegroundService = false;
            }
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
