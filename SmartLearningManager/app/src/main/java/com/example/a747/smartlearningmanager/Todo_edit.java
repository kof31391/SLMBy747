package com.example.a747.smartlearningmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


public class Todo_edit extends AppCompatActivity {

    private TextView desc;
    private DatePicker todoDate;
    private TimePicker todoTime;
    private TextView topic;
    private ArrayList<todoObj> items;
    private todoObj recObj;
    private int pos;
    private String temp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        temp = pref.getString("std_id", null);
        Intent intent = getIntent();
        items = new ArrayList<>();
        topic = (TextView)findViewById(R.id.topic);
        desc = (TextView) findViewById(R.id.todoDesc);
        todoDate =(DatePicker) findViewById(R.id.datePicker);
        todoTime = (TimePicker)findViewById(R.id.timePicker);
        readItems();
        pos=intent.getIntExtra("todo",0);
        recObj = items.get(pos);
            topic.setText(recObj.getTopic());
            desc.setText(recObj.getDesc());
            todoDate.updateDate(recObj.getDate().getYear(), recObj.getDate().getMonth(), recObj.getDate().getDate());
            todoTime.setCurrentHour(recObj.getDate().getHours());
            todoTime.setCurrentMinute(recObj.getDate().getMinutes());
    }

    public void onClickAddTodo(View view) {
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

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, temp+".txt");
        try {
            Date date;
            readItems();
            todoObj temp = new todoObj();
            temp.setTopic(topic.getText().toString());
            temp.setDesc(desc.getText().toString());
            date = Util.getDateFromDatePicker(todoDate,todoTime);
            temp.setDate(date);
            items.remove(pos);
            items.add(temp);
            Collections.sort(items);
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(todoFile));
            ois.writeObject(items);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
