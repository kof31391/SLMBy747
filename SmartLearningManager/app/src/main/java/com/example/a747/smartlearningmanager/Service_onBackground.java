package com.example.a747.smartlearningmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Date;

/**
 * Created by lenovo on 24/10/2559.
 */

public class Service_onBackground extends Service {

    private Handler handler;
    private Runnable runnable;
    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        Service_onBackground getService() {
            return Service_onBackground.this;
        }
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        showNotification();
        return START_STICKY;
    }

    private void showNotification(){
        Log.i("Runnable","Runnable run...");
        handler = new Handler();
        runnable = new Runnable() {
            int num = 0;
            @Override
            public void run() {
                Date d = new Date();
                d.setHours(17);
                d.setMinutes(30);
                String s = String.valueOf(num);
                Notification notification = getNotification("Hello",s,d.getTime());
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(1000,notification);
                num++;

                handler.postDelayed(this,6000);
            }
        };
        handler.post(runnable);
    }

    private Notification getNotification(String title, String content, long time) {
        /*Intent intent = new Intent(this, Service_onBackground.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Service_onBackground.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);*/
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setWhen(time)
                .setSound(alarmSound)
                .build();
        return notification;
    }
}
