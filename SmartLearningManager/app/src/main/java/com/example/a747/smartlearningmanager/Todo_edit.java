package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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


class Todo_Edit extends AppCompatActivity {

    private EditText desc;
    private DatePicker todoDate;
    private TimePicker todoTime;
    private TextView topic;
    private ArrayList<todoObj> items;
    private todoObj recObj;
    private int pos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);
        Intent intent = getIntent();
        items = new ArrayList<>();
        topic = (TextView)findViewById(R.id.topic);
        desc = (EditText)findViewById(R.id.todoDesc);
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
        writeItems();
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

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo_list.txt");
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
