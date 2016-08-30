package com.example.a747.smartlearningmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Todo_View extends AppCompatActivity {
    private EditText desc;
    private TextView topic;
    private ArrayList<todoObj> items;
    private todoObj recObj;
    private int pos;
    private TextView date;
    private TextView month;
    private TextView year;
    private TextView hour;
    private TextView minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_view);
        Intent intent = getIntent();
        items = new ArrayList<>();
        topic = (TextView)findViewById(R.id.topic);
        desc = (EditText)findViewById(R.id.todoDesc);
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

    public void onClickBack(View view) {
        Intent intent = new Intent(this,Todo_List.class);
        startActivity(intent);
    }

    private void readItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo_list.txt");
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
