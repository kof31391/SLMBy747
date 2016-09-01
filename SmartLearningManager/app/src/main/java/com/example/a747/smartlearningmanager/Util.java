package com.example.a747.smartlearningmanager;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by 747 on 30-Aug-16.
 */
public class Util {
    public static java.util.Date getDateFromEditText(EditText d,EditText t){
        Date dates = new Date();
        String dateArr[] = d.getText().toString().split("/");
        String time[] = t.getText().toString().split(":");
        int day = Integer.parseInt(dateArr[0]);
        int month = Integer.parseInt(dateArr[1]);
        int year = Integer.parseInt(dateArr[2]);
        int hour = Integer.parseInt(time[0]);
        int min = Integer.parseInt(time[1]);
        dates.setDate(day);
        dates.setMonth(month);
        dates.setYear(year);
        dates.setHours(hour);
        dates.setMinutes(min);
        return dates;
    }


}
