package com.example.a747.smartlearningmanager;



import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


public class Todo_edit extends AppCompatActivity {

    private TextView desc;
    private EditText todoDate;
    private EditText todoTime;
    private TextView topic;
    private ArrayList<todoObj> items;
    private todoObj recObj;
    private int pos;
    private String temp;
    private Calendar cal;
    private int day;
    private int month;
    private int year;
    private Date date;
    private Spinner spinner;
    private String category;
    private Calendar mcurrentTime;
    private String stdid;
    private todoObj temps;
    private ArrayList<NotificationObj> NotiItems;
    private CheckBox finish;
    private boolean origin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);
        DateFormat df = new SimpleDateFormat("HH:mm");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        temp = pref.getString("std_id", null);
        stdid = pref.getString("std_id", null)+"Notification.txt";
        Intent intent = getIntent();
        readItems();
        pos=intent.getIntExtra("todo",0);
        recObj = items.get(pos);
        items = new ArrayList<>();
        spinner = (Spinner)findViewById(R.id.spinner);
        finish = (CheckBox)findViewById(R.id.checkBox2);
        topic = (TextView)findViewById(R.id.topic);
        desc = (TextView) findViewById(R.id.todoDesc);
        todoDate =(EditText) findViewById(R.id.datePicker);
        todoTime = (EditText) findViewById(R.id.timePicker);
        date = recObj.getDate();
        category = recObj.getCategory();
        origin = recObj.isFinish();
        if(recObj.isFinish()==true){
            finish.setChecked(true);
        }
        spinner.setSelection(recObj.getPosition());
        todoDate.setText(date.getDate()+"/"+(date.getMonth())+"/"+(date.getYear()+1900));
        todoTime.setText(df.format(date));
            topic.setText(recObj.getTopic());
            desc.setText(recObj.getDesc());
        todoTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mcurrentTime = Calendar.getInstance();
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Todo_edit.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        todoTime.setText(Util.getMinuteFormat(selectedHour,selectedMinute));
                    }
                }, mcurrentTime.get(Calendar.HOUR_OF_DAY), mcurrentTime.get(Calendar.MINUTE), true);
                mTimePicker.show();
            }
        });
        cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        todoDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateDialog();
            }
        });
    }

    public void DateDialog(){

        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {

                todoDate.setText(dayOfMonth+"/"+monthOfYear+"/"+year);

            }};

        DatePickerDialog dpDialog=new DatePickerDialog(this, listener, year, month, day);
        dpDialog.show();

    }

    public void onClickAddTodo(View view) {
        category = String.valueOf(spinner.getSelectedItem());
        if(topic.length()>0) {
            writeItems();
            Intent intent = new Intent(this,Todo_List.class);
            startActivity(intent);
        }else{
            AlertDialog alertDialog = new AlertDialog.Builder(Todo_edit.this).create();
            alertDialog.setTitle("Alert: No Topic");
            alertDialog.setMessage("Please Enter Topic.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            topic.requestFocus();
                        }
                    });
            alertDialog.show();
        }
    }


    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, temp+".txt");
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            items = (ArrayList<todoObj>)ois.readObject();

        } catch (IOException e) {
            items = new ArrayList<>();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onClickBack(View v) {
        Intent intent = new Intent(this,Todo_List.class);
        startActivity(intent);
    }

    private void readNoti(){
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, stdid);
        NotiItems = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            NotiItems = (ArrayList<NotificationObj>)ois.readObject();
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeNoti(NotificationObj obj){
        File filesDir = getFilesDir();
        File notiFile = new File(filesDir, stdid);
        try{
            readNoti();
            NotiItems.add(obj);
            Collections.sort(NotiItems);
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(notiFile));
            ois.writeObject(NotiItems);
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, temp+".txt");
        try {
            readItems();
            todoObj temp = Util.setValue(topic.getText().toString(),desc.getText().toString(),category,Util.getDateFromEditText(todoDate,todoTime));
            if(finish.isChecked()==true){
                temp.setFinish(true);
            }else{
                temp.setFinish(false);
            }
            temp.setNotiId(recObj.getNotiId());
            items.remove(pos);
            items.add(temp);
            Collections.sort(items);
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(todoFile));
            ois.writeObject(items);
             /*Setup Notification*/
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateFuture = temp.getDate();
            String future = dateFormat.format(dateFuture);
            String title = temp.getTopic();
            String content = temp.getDesc();
            int id = temp.getNotiId();
            if(temp.isFinish()==false) {
                cancelNotification(getNotification(title,content,dateFuture.getTime()),id);
                scheduleNotification(getNotification(title, content,dateFuture.getTime()), getSchedule(getTimeCurrent(), future));
            }else{
                cancelNotification(getNotification(title, content,dateFuture.getTime()),id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Notification getNotification(String title, String content,long time) {
        temps = Util.setValue(topic.getText().toString(),desc.getText().toString(),category,Util.getDateFromEditText(todoDate,todoTime));
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
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(time)
                .setSound(alarmSound)
                .build();
        return notification;
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



    private long getSchedule(String now, String future) {
        long TimeDifference = 0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dNow = df.parse(future);
            Date dFuture = df.parse(now);
            TimeDifference = ((dNow.getTime() - dFuture.getTime())+500);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return TimeDifference;
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
    private String getTimeCurrent() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = calendar.getTime();
        String sDate = dateFormat.format(date);
        return sDate;
    }



}
