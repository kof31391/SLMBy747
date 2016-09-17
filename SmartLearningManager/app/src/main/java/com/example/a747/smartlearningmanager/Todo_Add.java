package com.example.a747.smartlearningmanager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.app.DatePickerDialog;

import android.widget.DatePicker;



public class Todo_Add extends AppCompatActivity {
    private EditText desc;
    private EditText todoDate;
    private EditText todoTime;
    private TextView topic;
    private ArrayList<todoObj> items;
    private String fileName;
    private Calendar cal;
    private int day;
    private int month;
    private int year;
    private Spinner spin;
    String category;
    private todoObj temp;
    private Calendar mcurrentTime = Calendar.getInstance();
    private String stdid;
    private ArrayList<NotificationObj> NotiItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_add);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        stdid = pref.getString("std_id", null)+"Notification.txt";
        System.out.println(stdid);
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        items = new ArrayList<>();
        spin = (Spinner)findViewById(R.id.spinner);
        topic = (TextView)findViewById(R.id.topic);
		desc = (EditText)findViewById(R.id.todoDesc);
        todoTime =(EditText) findViewById(R.id.timePicker);
        todoDate = (EditText) findViewById(R.id.datePicker);
        DateFormat df = new SimpleDateFormat("HH:mm");
        Date todo_time = new Date();
        todo_time.setHours(mcurrentTime.get(Calendar.HOUR_OF_DAY));
        todo_time.setMinutes(mcurrentTime.get(Calendar.MINUTE));
        todoTime.setText(df.format(todo_time));
        todoDate.setText(mcurrentTime.get(Calendar.DAY_OF_MONTH)+"/"+mcurrentTime.get(Calendar.MONTH)+"/"+
                mcurrentTime.get(Calendar.YEAR));
            topic.requestFocus();
        todoTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Todo_Add.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if(selectedMinute<=9) {
                            todoTime.setText(selectedHour + ":0" + selectedMinute);
                        }else{
                            todoTime.setText(selectedHour + ":" + selectedMinute);
                        }
                    }
                }, hour, minute, true);
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
        todoDate.setFocusable(false);
        todoDate.setClickable(true);
    }






    public void onClickAddTodo(View view) {
        category = String.valueOf(spin.getSelectedItem());
        if(topic.length()>0) {
            writeItems();
            Intent intent = new Intent(this,Todo_List.class);
            startActivity(intent);
        }else{
            AlertDialog alertDialog = new AlertDialog.Builder(Todo_Add.this).create();
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


    public void DateDialog(){

        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth)
            {

                todoDate.setText(dayOfMonth+" / "+monthOfYear+" / "+year);

            }};

        DatePickerDialog dpDialog=new DatePickerDialog(this, listener, year, month, day);
        dpDialog.show();

    }


    public void onClickBack(View v) {
        todoTime.setText("");
        todoDate.setText("");
        Intent intent = new Intent(this,Todo_List.class);
        startActivity(intent);
    }

    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, fileName);
        items = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            items = (ArrayList<todoObj>)ois.readObject();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, fileName);
        try {
            readItems();
            temp = new todoObj();
            temp.setTopic(topic.getText().toString());
            temp.setDesc(desc.getText().toString());
            temp.setCategory(category);
            temp.setDate(Util.getDateFromEditText(todoDate,todoTime));
            items.add(temp);
            Collections.sort(items);
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(todoFile));
            ois.writeObject(items);

            /*Setup Notification*/
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateFuture = temp.getDate();
            dateFuture.setYear(dateFuture.getYear()-1900);
            Calendar c = Calendar.getInstance();
            Date dateNow = c.getTime();
            if(dateFuture.getTime()>dateNow.getTime()){
                String future = dateFormat.format(dateFuture);
                String title = temp.getTopic();
                String content = temp.getDesc();
                if(temp.isFinish()==false) {
                    scheduleNotification(getNotification(title, content), getSchedule(getTimeCurrent(), future));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTimeCurrent() {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = calendar.getTime();
        String sDate = dateFormat.format(date);
        return sDate;
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
            System.out.println(e.getMessage());
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

    private long getSchedule(String now, String future) {
        long TimeDifference = 0;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dNow = df.parse(future);
            Date dFuture = df.parse(now);
            TimeDifference = ((dNow.getTime() - dFuture.getTime())-1500);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return TimeDifference;
    }
    private void scheduleNotification(Notification notification, long delay) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) (System.currentTimeMillis() % Integer.MAX_VALUE), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }


    private Notification getNotification(String title,String content) {
        temp = new todoObj();       //start
        temp.setTopic(topic.getText().toString());
        temp.setDesc(desc.getText().toString());
        temp.setCategory(category);
        temp.setDate(Util.getDateFromEditText(todoDate,todoTime));
        NotificationObj noti = new NotificationObj(temp);
        writeNoti(noti);        //stop
        Intent intent = new Intent(this, Todo_View.class);
        intent.putExtra("message", (Parcelable) temp);
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
                .setSound(alarmSound)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .build();
        return notification;
    }


}
