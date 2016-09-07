package com.example.a747.smartlearningmanager;



import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
    private ArrayList<NotificationObj> NotiItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_edit);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        temp = pref.getString("std_id", null);
        stdid = pref.getString("std_id", null)+"Notification.txt";
        Intent intent = getIntent();
        readItems();
        pos=intent.getIntExtra("todo",0);
        recObj = items.get(pos);
        items = new ArrayList<>();
        spinner = (Spinner)findViewById(R.id.spinner);
        topic = (TextView)findViewById(R.id.topic);
        desc = (TextView) findViewById(R.id.todoDesc);
        todoDate =(EditText) findViewById(R.id.datePicker);
        todoTime = (EditText) findViewById(R.id.timePicker);
        date = recObj.getDate();
        category = recObj.getCategory();
        spinner.setSelection(recObj.getPosition());
        todoDate.setText(date.getDate()+"/"+date.getMonth()+"/"+date.getYear());
        todoTime.setText(date.getHours()+":"+date.getMinutes());
            topic.setText(recObj.getTopic());
            desc.setText(recObj.getDesc());
        todoTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Todo_edit.this, new TimePickerDialog.OnTimeSetListener() {
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

    private void writeItems() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, temp+".txt");
        try {
            readItems();
            todoObj temp = new todoObj();
            temp.setTopic(topic.getText().toString());
            temp.setDesc(desc.getText().toString());
            temp.setCategory(category);
            temp.setDate(Util.getDateFromEditText(todoDate,todoTime));
            items.remove(pos);
            items.add(temp);
            Collections.sort(items);
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(todoFile));
            ois.writeObject(items);
            NotificationObj noti = new NotificationObj(temp);
            writeNoti(noti);        //stop

        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
