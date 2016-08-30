package com.example.a747.smartlearningmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;


/**
 * Created by 747 on 30-Aug-16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent one = new Intent(context,Todo_View.class);
        one.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending =PendingIntent.getActivity(context,100,one,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentIntent(pending)
                .setSmallIcon(R.drawable.home).setContentTitle("Todo Alarm").setContentText("BLA BLA")
                .setAutoCancel(true);
        nm.notify(100,builder.build());
    }
}