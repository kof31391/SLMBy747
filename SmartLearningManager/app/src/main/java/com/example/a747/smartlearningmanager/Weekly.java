package com.example.a747.smartlearningmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Weekly extends AppCompatActivity {

    List<String> monday = new ArrayList<>();
    List<String> tuesday = new ArrayList<>();
    List<String> wednesday = new ArrayList<>();
    List<String> thursday = new ArrayList<>();
    List<String> friday = new ArrayList<>();
    List<String> saturday = new ArrayList<>();
    List<String> sunday = new ArrayList<>();
    TextView mon;
    TextView tue;
    TextView wed;
    TextView thu;
    TextView fri;
    TextView sat;
    TextView sun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weekly);
        mon = (TextView)findViewById(R.id.mon);
        tue = (TextView)findViewById(R.id.tue);
        wed = (TextView)findViewById(R.id.wed);
        thu = (TextView)findViewById(R.id.thu);
        fri = (TextView)findViewById(R.id.fri);
        sat = (TextView)findViewById(R.id.sat);
        sun = (TextView)findViewById(R.id.sun);
        getSchedule();
    }
    public void onClickBack(View v){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void getSchedule() {
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Subject ORDER BY day_id ASC;", null);
        resultSet.moveToFirst();
        while (!resultSet.isAfterLast()) {
            switch (resultSet.getString(resultSet.getColumnIndex("day_id"))) {
                case "1":
                    monday.add("Schedule: " + resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
                    monday.add("\nCode: " + resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    monday.add("\nName: " + resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    //monday.add("\nAttend / Total Class:      /      ");
                    break;
                case "2":
                    tuesday.add("Schedule: " + resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
                    tuesday.add("\nCode: " + resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    tuesday.add("\nName: " + resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    //tuesday.add("\nAttend / Total Class:      /      ");
                    break;
                case "3":
                    wednesday.add("Schedule: " + resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
                    wednesday.add("\nCode: " + resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    wednesday.add("\nName: " + resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    //wednesday.add("\nAttend / Total Class:      /      ");
                    break;
                case "4":
                    thursday.add("Schedule: " + resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
                    thursday.add("\nCode: " + resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    thursday.add("\nName: " + resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    //thursday.add("\nAttend / Total Class:      /      ");
                    break;
                case "5":
                    friday.add("Schedule: " + resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
                    friday.add("\nCode: " + resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    friday.add("\nName: " + resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    //friday.add("\nAttend / Total Class:      /      ");
                    break;
                case "6":
                    saturday.add("Schedule: " + resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
                    saturday.add("\nCode: " + resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    saturday.add("\nName: " + resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    //saturday.add("\nAttend / Total Class:      /      ");
                    break;
                case "7":
                    sunday.add("Schedule: " + resultSet.getString(resultSet.getColumnIndex("subject_start_time")) + " - " + resultSet.getString(resultSet.getColumnIndex("subject_end_time")));
                    sunday.add("\nCode: " + resultSet.getString(resultSet.getColumnIndex("subject_code")));
                    sunday.add("\nName: " + resultSet.getString(resultSet.getColumnIndex("subject_name")));
                    //sunday.add("\nAttend / Total Class:      /      ");
                    break;
            }
            resultSet.moveToNext();
        }
        mydatabase.close();
        String text = "";
        if(monday.size()>0) {
            for (int i = 0; i < monday.size(); i++) {
                text += monday.get(i);
            }
            mon.setText(text);
            text = "";
        }else{
          mon.setText("No Class");
        }
        if(tuesday.size()>0) {
            for (int i = 0; i < tuesday.size(); i++) {
                text += tuesday.get(i);
            }
            tue.setText(text);
            text = "";
        }else{
            tue.setText("No Class");
        }

        if(wednesday.size()>0) {
            for (int i = 0; i < wednesday.size(); i++) {
                text += wednesday.get(i);
            }
            wed.setText(text);
            text = "";
        }else{
            wed.setText("No Class");
        }

        if(thursday.size()>0) {
            for (int i = 0; i < thursday.size(); i++) {
                text += thursday.get(i);
            }
            thu.setText(text);
            text = "";
        }else{
            thu.setText("No Class");
        }

        if(friday.size()>0) {
            for (int i = 0; i < friday.size(); i++) {
                text += friday.get(i);
            }
            fri.setText(text);
            text = "";
        }else{
            fri.setText("No Class");
        }

        if(saturday.size()>0) {
            for (int i = 0; i < saturday.size(); i++) {
                text += saturday.get(i);
            }
            sat.setText(text);
            text = "";
        }else{
            sat.setText("No Class");
        }

        if(sunday.size()>0) {
            for (int i = 0; i < sunday.size(); i++) {
                text += sunday.get(i);
            }
            sun.setText(text);
        }else{
            sun.setText("No Class");
        }
    }
}
