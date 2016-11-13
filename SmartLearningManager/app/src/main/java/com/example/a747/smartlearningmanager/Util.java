package com.example.a747.smartlearningmanager;

import android.widget.EditText;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Util {
    static Calendar mcurrentTime = Calendar.getInstance();
    static DateFormat df = new SimpleDateFormat("HH:mm");

    public static java.util.Date getDateFromEditText(EditText d,EditText t){
        Date dates = new Date();
        String dateArr[] = d.getText().toString().split("/");
        String time[] = t.getText().toString().split(":");
        int day = Integer.parseInt(dateArr[0].trim());
        int month = Integer.parseInt(dateArr[1].trim());
        int year = Integer.parseInt(dateArr[2].trim());
        int hour = Integer.parseInt(time[0].trim());
        int min = Integer.parseInt(time[1].trim());
        dates.setDate(day);
        dates.setMonth(month);
        dates.setYear(year-1900);
        dates.setHours(hour);
        dates.setMinutes(min);
        dates.setSeconds(0);
        return dates;
    }

    public static String getTimeFormat(){
        Date todo_time = new Date();
        todo_time.setHours(mcurrentTime.get(Calendar.HOUR_OF_DAY));
        todo_time.setMinutes(mcurrentTime.get(Calendar.MINUTE));
        return df.format(todo_time);
    }

    public static String getDateFormat(){
       return mcurrentTime.get(Calendar.DAY_OF_MONTH)+"/"+mcurrentTime.get(Calendar.MONTH)+"/"+
        mcurrentTime.get(Calendar.YEAR);
    }

    public static String getMinuteFormat(int hour,int minute){
        Date date = new Date();
        date.setHours(hour);
        date.setMinutes(minute);
        return df.format(date);
    }

    public static todoObj setValue(String title,String desc,String category,Date date){
        todoObj temp = new todoObj();
        temp.setTopic(title);
        temp.setDesc(desc);
        temp.setCategory(category);
        temp.setDate(date);
        return temp;
    }
}
