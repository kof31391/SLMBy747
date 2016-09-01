package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class Todo_View extends AppCompatActivity {
    private TextView desc;
    private TextView topic;
    private ArrayList<todoObj> items;
    private todoObj recObj;
    private int pos;
    private TextView date;
    private TextView month;
    private TextView year;
    private TextView hour;
    private TextView minute;
    private String temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_view);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        temp = pref.getString("std_id", null);
        Intent intent = getIntent();
        items = new ArrayList<>();
        topic = (TextView)findViewById(R.id.topic);
        desc = (TextView) findViewById(R.id.todoDesc);
        date = (TextView)findViewById(R.id.Date);
        month = (TextView)findViewById(R.id.Month);
        year = (TextView)findViewById(R.id.Year);
        hour = (TextView)findViewById(R.id.Hour);
        minute = (TextView)findViewById(R.id.Minute);
        readItems();
        pos=intent.getIntExtra("todo",0);
        recObj = items.get(pos);
        topic.setText(recObj.getTopic());
        desc.setText(recObj.getDesc());
        date.setText(""+recObj.getDate().getDate());
        month.setText(""+recObj.getDate().getMonth());
        year.setText(""+recObj.getDate().getYear());
        hour.setText(""+recObj.getDate().getHours());
        minute.setText(""+recObj.getDate().getMinutes());
    }

    public void onClickBack(View v) {
        Intent intent = new Intent(this,Todo_List.class);
        startActivity(intent);
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
}
