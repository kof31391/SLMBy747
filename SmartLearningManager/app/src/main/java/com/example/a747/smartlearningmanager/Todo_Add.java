package com.example.a747.smartlearningmanager;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_add);
        Intent intent = getIntent();
        fileName = intent.getStringExtra("fileName");
        items = new ArrayList<>();
        spin = (Spinner)findViewById(R.id.spinner);
        topic = (TextView)findViewById(R.id.topic);
        desc = (EditText)findViewById(R.id.todoDesc);
        todoTime =(EditText) findViewById(R.id.timePicker);
        todoDate = (EditText) findViewById(R.id.datePicker);
            topic.setHint("Enter Topic Here");
            topic.requestFocus();
        todoTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(Todo_Add.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        todoTime.setText( selectedHour + ":" + selectedMinute);
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

                todoDate.setText(dayOfMonth+"/"+monthOfYear+"/"+year);

            }};

        DatePickerDialog dpDialog=new DatePickerDialog(this, listener, year, month, day);
        dpDialog.show();

    }


    public void onClickBack(View v) {
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
            todoObj temp = new todoObj();
            temp.setTopic(topic.getText().toString());
            temp.setDesc(desc.getText().toString());
            temp.setCategory(category);
            temp.setDate(Util.getDateFromEditText(todoDate,todoTime));
            items.add(temp);
            Collections.sort(items);
            ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(todoFile));
            ois.writeObject(items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
