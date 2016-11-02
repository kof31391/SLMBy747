package com.example.a747.smartlearningmanager;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;

import android.view.View;

import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;


import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Todo_List extends AppCompatActivity {
    private ArrayList<todoObj> items;
    private ListView lvItems;
    private int pos;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> show;
    private String stdid;
    private Spinner spinner;
    private EditText query;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat time = new SimpleDateFormat("HH:mm");
    private ArrayList<Integer> posTemp;
    private todoObj temps;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("INFO", "Loading...");
        dialog = new Dialog(this);
        dialog = getDialogLoading();
        dialog.show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_list);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        stdid = pref.getString("std_id", null);
        spinner = (Spinner)findViewById(R.id.spinner2);
        query = (EditText)findViewById(R.id.searchBox);
        try {
            lvItems = (MyListView) findViewById(R.id.lvItems);
            show = new ArrayList<>();
            items = new ArrayList<>();
            readItems();
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, show){
                @Override
                public View getView(int position, View convertView, ViewGroup parent){
                //    int[] colors = new int[] { Color.parseColor("#c0d4e0") , Color.parseColor("#cecece") };
                    View view = super.getView(position, convertView, parent);
                   //     int colorPos = new Util().getColors(items,position);
                    view.setBackgroundColor(Color.parseColor("#c0d4e0"));
                    return view;
                }
            };
            lvItems.setAdapter(adapter);
            setupListViewListener();
            registerForContextMenu(lvItems);
        }catch(Exception e){
            new File(stdid+".txt");
            lvItems = (MyListView) findViewById(R.id.lvItems);
            show = new ArrayList<>();
            items = new ArrayList<>();
            readItems();
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, show);
            lvItems.setAdapter(adapter);
            setupListViewListener();
            registerForContextMenu(lvItems);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
            }
        }, 1000);
        Log.i("INFO", "Loading complete");
    }

    private Dialog getDialogLoading(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return  dialog;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add("Edit");
        menu.add("Delete");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
               int index = info.position;
                if(posTemp==null) {
                    if (item.getTitle().equals("Delete")) {
                        items.remove(index);
                        items.trimToSize();
                        show.remove(index);
                        adapter.notifyDataSetChanged();
                        writeItems();
                    } else if (item.getTitle().equals("Edit")) {
                        sendToEditor(index);
                    }else{
                        items.get(index).setFinish(!items.get(index).isFinish());
                        if(items.get(index).isFinish()==true){
                            cancelNotification(getNotification(index),items.get(index).getNotiId());
                        }else{
                            scheduleNotification(getNotification(index),items.get(index).getNotiId());
                        }
                        adapter.notifyDataSetChanged();
                        writeItems();
                    }
                }else{
                    int pos = posTemp.get(index);
                    if (item.getTitle().equals("Delete")) {
                        cancelNotification(getNotification(pos),items.get(pos).getNotiId());
                        items.remove(pos);
                        items.trimToSize();
                        adapter.notifyDataSetChanged();
                        writeItems();
                        Intent intent = new Intent(this,Todo_List.class);
                        startActivity(intent);
                    } else if (item.getTitle().equals("Edit")) {
                        sendToEditor(pos);
                    }else{
                        items.get(index).setFinish(!items.get(index).isFinish());
                        adapter.notifyDataSetChanged();
                        writeItems();
                    }
                }
                return true;
    }



    private int scheduleNotification(Notification notification, long delay) {
        int id = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

        return id;

    }

    private Notification getNotification(int index) {
        temps = Util.setValue(items.get(index).getTopic(),items.get(index).getDesc(),items.get(index).getCategory(),items.get(index).getDate());
        Intent intent = new Intent(this, Todo_View.class);
        intent.putExtra("message", (Parcelable) temps);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Todo_View.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(temps.getTopic())
                .setContentText(temps.getDesc())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .build();
        return notification;
    }

    private void setupListViewListener(){
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                pos = position;
                sendToView(pos);
            }
        });
    }

    public void cancelNotification(Notification notification,int id) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, (int) (System.currentTimeMillis() % Integer.MAX_VALUE));
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntent.cancel();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private  void sendToEditor(int pos){
        Intent intent = new Intent(this,Todo_Edit.class);
        intent.putExtra("todo", pos);
        startActivity(intent);
    }


    private void sendToView(int pos){
        Intent intent = new Intent(this,Todo_View.class);
        intent.putExtra("todo", pos);
        intent.putExtra("source","list");
        startActivity(intent);
    }

    public void gotoEditor(View v){
    Intent intent = new Intent(this,Todo_Add.class);
        intent.putExtra("fileName",stdid+".txt");
    startActivity(intent);
    }

    public void gotoAbout(View v){
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }

    public void gotoAdd(View v){
        Intent intent = new Intent(this,Todo_Add.class);
        startActivity(intent);
    }


    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid+".txt");
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Date date;
                items = (ArrayList<todoObj>)ois.readObject();
            for(int j = 0 ;j<items.size();j++) {
                date = items.get(j).getDate();
                show.add(items.get(j).getTopic()+"\n"+"Deadline: "+sdf.format(date)+" at "+time.format(date));
            }


        } catch (EOFException e) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid+".txt");
        try {
            ObjectOutputStream fis = new ObjectOutputStream(new FileOutputStream(todoFile));
            fis.writeObject(items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void gotoSetting(View v){
        Intent intent = new Intent(this, more_setting.class);
        startActivity(intent);
    }

    public void gotoHome(View v){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void gotoNoti(View v){
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
    }
    public void gotoElean(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }
    public void gotopagenews(View v){
        Intent intent = new Intent(this, Page_news.class);
        startActivity(intent);
    }

    public void Query(View v){
        String temp ;
        String catTemp;
        Date date;
        String category = String.valueOf(spinner.getSelectedItem());
        posTemp = new ArrayList<>();
        show.clear();
        for(int i = 0;i<items.size();i++){
            temp = items.get(i).getTopic();
            catTemp = items.get(i).getCategory();
            if(temp.contains(query.getText().toString())&&category.equals(catTemp)){
                date = items.get(i).getDate();
                show.add(items.get(i).getTopic()+"\n"+"Deadline: "+sdf.format(date)+" at "+time.format(date));
                posTemp.add(i);
            }else if(temp.contains(query.getText().toString())&&category.equals("Any")){
                date = items.get(i).getDate();
                show.add(items.get(i).getTopic()+"\n"+"Deadline: "+sdf.format(date)+" at "+time.format(date));
                posTemp.add(i);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
