package com.example.a747.smartlearningmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Array;
import java.util.ArrayList;

public class Todo_View extends AppCompatActivity {
    private TextView desc;
    private TextView topic;
    private ArrayList<NotificationObj> items;
    private NotificationObj recObj;
    private int pos;
    private TextView date;
    private TextView month;
    private TextView year;
    private TextView hour;
    private TextView minute;
    private TextView category;
    private TextView status;
    private String temp;
    private boolean fromNoti = false;
    private ArrayList<todoObj> item;
    private todoObj todo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_view);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        temp = pref.getString("std_id", null);
        Intent intent = getIntent();
        items = new ArrayList<>();
        topic = (TextView)findViewById(R.id.topic);
        status = (TextView)findViewById(R.id.finishStatus);
        category = (TextView)findViewById(R.id.category);
        desc = (TextView) findViewById(R.id.todoDesc);
        date = (TextView)findViewById(R.id.Date);
        month = (TextView)findViewById(R.id.Month);
        year = (TextView)findViewById(R.id.Year);
        hour = (TextView)findViewById(R.id.Hour);
        minute = (TextView)findViewById(R.id.Minute);
        try{
            ((todoObj) intent.getParcelableExtra("message")).getTopic();
            todo = intent.getParcelableExtra("message");
            fromNoti = true;
            topic.setText(todo.getTopic());
            category.setText(todo.getCategory());
            desc.setText(todo.getDesc());
            if(todo.isFinish()==true){
                status.setText(status.getText()+"Finish");
            }else{
                status.setText(status.getText()+"Not Finish");
            }
            date.setText("" + todo.getDate().getDate());
            month.setText("" + todo.getDate().getMonth());
            year.setText("" + (todo.getDate().getYear()+1900));
            hour.setText(Util.getMinuteFormat(todo.getDate().getHours(),todo.getDate().getMinutes()));
        }catch(NullPointerException np) {
            pos = intent.getIntExtra("todo",0);
            try {
                if (intent.getExtras().getString("source").equals("list")) {
                    readItems();
                    setForList();
                }
            }catch(Exception ex){
                try {
                    readItemsNoti();
                    setForNoti();
                } catch (Exception e) {
                    showAlert();
                }
            }
        }
    }

    private void setForList(){
        todo = item.get(pos);
        topic.setText(todo.getTopic());
        category.setText(todo.getCategory());
        desc.setText(todo.getDesc());
        if(todo.isFinish()==true){
            status.setText(status.getText()+"Finish");
        }else{
            status.setText(status.getText()+"Not Finish");
        }
        date.setText("" + todo.getDate().getDate());
        month.setText("" + todo.getDate().getMonth());
        year.setText("" + (todo.getDate().getYear()+1900));
        hour.setText(Util.getMinuteFormat(todo.getDate().getHours(),todo.getDate().getMinutes()));
        minute.setText("");
    }

    private void showAlert(){
        final Intent intents = new Intent(this, Noti.class);
        new AlertDialog.Builder(Todo_View.this)
                .setTitle("Alert")
                .setMessage("This todo isn't exists.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(intents);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void readItemsNoti() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, temp+"Notification.txt");
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            items = (ArrayList<NotificationObj>)ois.readObject();

        } catch (IOException e) {
            items = new ArrayList<>();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setForNoti(){
        recObj = items.get(pos);
        topic.setText(recObj.getTopic());
        category.setText(recObj.getCategory());
        desc.setText(recObj.getDesc());
        if (recObj.isFinish() == true) {
            status.setText(status.getText() + "Finish");
        } else {
            status.setText(status.getText() + "Not Finish");
        }
        date.setText("" + recObj.getDate().getDate());
        month.setText("" + recObj.getDate().getMonth());
        year.setText("" + (recObj.getDate().getYear() + 1900));
        hour.setText(Util.getMinuteFormat(recObj.getDate().getHours(), recObj.getDate().getMinutes()));
    }

    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, temp+".txt");
        try {
            FileInputStream fis = new FileInputStream(todoFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            item = (ArrayList<todoObj>)ois.readObject();
        } catch (IOException e) {
            item = new ArrayList<>();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onClickBack(View v) {
        Intent intent = new Intent(this,Noti.class);
        try {
            if (fromNoti == false && getIntent().getExtras().getString("check").equals("noti")) {
                intent = new Intent(this, Noti.class);
            }
        }catch(Exception e) {
            if (fromNoti == false) {
                intent = new Intent(this, Todo_List.class);
            } else {
                intent = new Intent(this, Noti.class);
            }
        }
        startActivity(intent);
    }
}
