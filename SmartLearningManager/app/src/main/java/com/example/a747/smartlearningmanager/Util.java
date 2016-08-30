package com.example.a747.smartlearningmanager;

import android.widget.DatePicker;
import android.widget.TimePicker;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by 747 on 30-Aug-16.
 */
public class Util {
    public static java.util.Date getDateFromDatePicker(DatePicker datePicker,TimePicker timePicker){
        Date date = new Date();
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();
        date.setDate(day);
        date.setMonth(month);
        date.setYear(year);
        date.setHours(timePicker.getCurrentHour());
        date.setMinutes(timePicker.getCurrentMinute());

        return date;
    }


}
