package com.example.a747.smartlearningmanager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Main extends AppCompatActivity {

    ArrayList<String> al_desc;
    ArrayList<String> al_title;

    private String std_id;
    private String department;
    private int last_noti_id;
    private int nextday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }

        /*Initial*/
        clearAlarmNoti();
        SharedPreferences prefInitial = getApplicationContext().getSharedPreferences("Initial", 0);
        SharedPreferences.Editor editorInitial = prefInitial.edit();
        String iniStatus = prefInitial.getString("Initial",null);
        if(iniStatus != "Initialed"){
            setProfile();
            getMaxEnrollment();
            setRSS();
            setNotiSchedule();
            getSchedule();

            editorInitial.putString("Initial","Initialed");
            editorInitial.commit();
        }else{
            getRSS();
            getSchedule();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setProfile(){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            public String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Profile.php?student_id="+params[0]);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if(code==200){
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        if (in != null) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            String line;
                            while ((line = bufferedReader.readLine()) != null)
                                strJSON = line;
                        }
                        in.close();
                    }
                    return strJSON;
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }
            protected void onPostExecute(String strJSON) {
                Log.i("Initial","Initial Profile...");
                try {
                    JSONArray data = new JSONArray(strJSON);
                    JSONObject c = data.getJSONObject(0);
                    SQLiteDatabase mydatabase = openOrCreateDatabase("Profile",MODE_PRIVATE,null);
                    mydatabase.execSQL("DROP TABLE IF EXISTS Profile");
                    mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Profile(firstname VARCHAR, lastname VARCHAR, department VARCHAR, email VARCHAR, phonenum VARCHAR, image VARCHAR);");
                    mydatabase.execSQL("INSERT INTO Profile VALUES('"+c.getString("student_name")+"','"+c.getString("student_surname")+"','"+c.getString("department_briefly")+"','"+c.getString("student_email")+"','"+c.getString("student_phone")+"','"+c.getString("student_image")+"');");
                    mydatabase.close();
                    department = c.getString("department_briefly");
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("department",department);
                    editor.commit();
                    Log.i("Initial","Initial Profile success");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }

    private void setRSS (){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            public String strJSON;
            protected String doInBackground(String... params) {
                try {
                    if(department == null){
                        SQLiteDatabase Profile_db = openOrCreateDatabase("Profile",MODE_PRIVATE,null);
                        Cursor resultSet = Profile_db.rawQuery("SELECT * FROM Profile",null);
                        resultSet.moveToFirst();
                        department =  resultSet.getString(resultSet.getColumnIndex("department"));
                        Profile_db.close();
                    }
                    System.out.println("Dept: "+department);
                    URL url = new URL("http://54.169.58.93/RSS_Feed.php?department="+department);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if(code==200){
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        if (in != null) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            String line;
                            while ((line = bufferedReader.readLine()) != null)
                                strJSON = line;
                        }
                        in.close();
                    }
                    return strJSON;
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }
            protected void onPostExecute(String strJSON) {
                Log.i("Initial","Initial set RSS...");
                try{
                    JSONArray data = new JSONArray(strJSON);
                    SQLiteDatabase RSS_db = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
                    RSS_db.execSQL("DROP TABLE IF EXISTS RSS");
                    RSS_db.execSQL("CREATE TABLE IF NOT EXISTS RSS(id INT, title VARCHAR, description VARCHAR, pubDate DATE, count INT);");
                    for(int i=0;i<data.length();i++){
                        JSONObject c = data.getJSONObject(i);
                        RSS_db.execSQL("INSERT INTO RSS(id, title, description, pubDate, count) SELECT * FROM (SELECT '"+c.getInt("rss_id")+"','"+encodeUnicode(c.getString("rss_title"))+"','"+encodeUnicode(c.getString("rss_description"))+"','"+c.getString("rss_createdate")+"','"+c.getInt("rss_count")+"') AS tmp WHERE NOT EXISTS (SELECT * FROM RSS WHERE id='"+c.getInt("rss_id")+"');");
                        RSS_db.execSQL("UPDATE RSS SET count='"+c.getInt("rss_count")+"' WHERE id='"+c.getInt("rss_id")+"';");
                    }
                    RSS_db.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
                Log.i("Initial","Initial set RSS success");
                getRSS();
            }
        }
        new GetDataJSON().execute();
    }

    private String encodeUnicode(String str) {
        String strEncoded = "";
        try {
            byte[] bytes = str.getBytes("UTF-8");
            strEncoded = new String(bytes, "UTF-8");
        }catch(Exception e){
            e.printStackTrace();
        }
        return strEncoded;
    }

    private void getRSS(){
        Log.i("Initial","Initial get RSS...");
        SQLiteDatabase mydatabase = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("SELECT title, description FROM RSS ORDER BY count DESC LIMIT 5;",null);
        resultSet.moveToFirst();
        al_title = new ArrayList();
        al_desc = new ArrayList();
        while(!resultSet.isAfterLast()){
            al_title.add(resultSet.getString(resultSet.getColumnIndex("title")));
            al_desc.add(resultSet.getString(resultSet.getColumnIndex("description")));
            resultSet.moveToNext();
        }
        mydatabase.close();
        TableLayout tl_news = (TableLayout) findViewById(R.id.tl_news);
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams params2=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        int i;
        for(i=0;i<al_title.size();i++) {
            TableRow row = new TableRow(this);
            TextView title = new TextView(this);
            title.setId(i);
            title.setClickable(true);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickNews(v);
                }
            });
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            title.setPadding(20, 20, 0, 20);
            if ((i % 2) == 0) {
                title.setBackgroundColor(Color.parseColor("#E6E6E6"));
            }
            title.setText(al_title.get(i).toString());
            title.setLayoutParams(params1);
            row.addView(title);
            row.setLayoutParams(params2);
            tl_news.addView(row);
        }
        Log.i("Initial","Initial get RSS success");
    }

    private void getMaxEnrollment(){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Max_Enrollment.php");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if (code == 200) {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        if (in != null) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            String line;
                            while ((line = bufferedReader.readLine()) != null)
                                strJSON = line;
                        }
                        in.close();
                    }
                    return strJSON;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }

            protected void onPostExecute(String strJSON) {
                try {
                    Log.i("Initial","Initial set last enrollment...");
                    JSONArray data = new JSONArray(strJSON);
                    JSONObject c = data.getJSONObject(0);
                    SQLiteDatabase mydatabase = openOrCreateDatabase("Enrollment",MODE_PRIVATE,null);
                    mydatabase.execSQL("DROP TABLE IF EXISTS Enrollment");
                    mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Enrollment(semester VARCHAR,enroll_year VARCHAR);");
                    mydatabase.execSQL("INSERT INTO Enrollment VALUES('"+c.getString("enrollment_semester")+"','"+c.getString("enrollment_year")+"');");
                    Log.i("Initial","Initial set last enrollment success");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute();
    }

    private void setNotiSchedule(){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/Schedule.php?student_id="+params[0]+"&past_enroll=0");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    int code = urlConnection.getResponseCode();
                    if(code==200){
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        if (in != null) {
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                            String line;
                            while ((line = bufferedReader.readLine()) != null)
                                strJSON = line;
                        }
                        in.close();
                    }
                    return strJSON;
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    urlConnection.disconnect();
                }
                return strJSON;
            }
            protected void onPostExecute(String strJSON) {
                try {
                    Log.i("Initial","Initial set notification for schedule...");
                    SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
                    mydatabase.execSQL("DROP TABLE IF EXISTS Schedule");
                    mydatabase.execSQL("DROP TABLE IF EXISTS Lecturer");
                    mydatabase.execSQL("DROP TABLE IF EXISTS Subject_Lecturer");
                    mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Schedule(subject_id VARCHAR, subject_code VARCHAR, subject_name VARCHAR, subject_start_time VARCHAR, subject_end_time VARCHAR, day_id INT(2));");
                        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Lecturer(lecturer_id VARCHAR, lecturer_prefix VARCHAR,lecturer_fristname VARCHAR, lecturer_lastname VARCHAR, lecturer_email VARCHAR, lecturer_phone VARCHAR, lecturer_image VARCHAR);");
                    mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Subject_Lecturer(sl_id VARCHAR,subject_id VARCHAR, lecturer_id VARCHAR);");
                    JSONArray data = new JSONArray(strJSON);
                    Calendar calendar = Calendar.getInstance();
                    Date nDate;
                    int nowDayfoweek = calendar.get(Calendar.DAY_OF_WEEK)-1;
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        mydatabase.execSQL("INSERT INTO Schedule VALUES('"+c.getString("subject_id")+"','"+c.getString("subject_code")+"','"+c.getString("subject_name")+"','"+c.getString("subject_start_time")+"','"+c.getString("subject_end_time")+"','"+c.getString("day_id")+"');");
                        mydatabase.execSQL("INSERT INTO Lecturer VALUES('"+c.getString("lecturer_id")+"','"+c.getString("lecturer_prefix")+"','"+c.getString("lecturer_fristname")+"','"+c.getString("lecturer_lastname")+"','"+c.getString("lecturer_email")+"','"+c.getString("lecturer_phone")+"','"+c.getString("lecturer_image")+"');");
                        mydatabase.execSQL("INSERT INTO Subject_Lecturer VALUES('"+c.getString("subject_lecturer_id")+"','"+c.getString("subject_id")+"','"+c.getString("lecturer_id")+"');");
                        nDate = calendar.getTime();
                        Date sDate = calendar.getTime();
                        int scheDayofweek = c.getInt("day_id");
                        int diffDayofweek = scheDayofweek - nowDayfoweek;
                        if(diffDayofweek < 0){
                            sDate.setDate(sDate.getDate()+(diffDayofweek+7));
                            String hmstart = c.getString("subject_start_time");
                            sDate.setHours(Integer.valueOf(hmstart.substring(0, 2)));
                            sDate.setMinutes(Integer.valueOf(hmstart.substring(3,5)));
                            long diffSec = sDate.getTime() - nDate.getTime();
                            if(diffSec>0) {
                                scheduleNotification(getNotification(c.getString("subject_code") + " : " + c.getString("subject_name"),
                                        " เริ่มเรียนเวลา " + c.getString("subject_start_time") + " จนถึง " + c.getString("subject_end_time"))
                                        , diffSec);
                            }
                        }else{
                            sDate.setDate(sDate.getDate()+diffDayofweek);
                            String hmstart = c.getString("subject_start_time");
                            sDate.setHours(Integer.valueOf(hmstart.substring(0, 2)));
                            sDate.setMinutes(Integer.valueOf(hmstart.substring(3,5)));
                            long diffSec = sDate.getTime() - nDate.getTime();
                            if(diffSec>0) {
                                scheduleNotification(getNotification(c.getString("subject_code") + " : " + c.getString("subject_name"),
                                        " เริ่มเรียนเวลา " + c.getString("subject_start_time") + " จนถึง " + c.getString("subject_end_time"))
                                        , diffSec);
                            }
                        }
                    }
                    mydatabase.close();
                    Log.i("Initial","Initial set notification for schedule success");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }

    private void getSchedule(){
        Log.i("Initial","Initial get schedule...");
        Calendar calendar = Calendar.getInstance();
        int nowDayfoweek = calendar.get(Calendar.DAY_OF_WEEK)-1;
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        final Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule WHERE day_id='"+nowDayfoweek+"' ORDER BY day_id ASC;",null);
        resultSet.moveToFirst();
        TextView title_day = (TextView) findViewById(R.id.title_day);
        switch (nowDayfoweek) {
            case 0 : title_day.setText("Sunday");
                break;
            case 1 : title_day.setText("Monday");
                break;
            case 2 : title_day.setText("Tuesday");
                break;
            case 3 : title_day.setText("Wednesday");
                break;
            case 4 : title_day.setText("Thursday");
                break;
            case 5 : title_day.setText("Friday");
                break;
            case 6 : title_day.setText("Saturday");
                break;
        }
        if (resultSet.getCount() != 0) {
            TableLayout tb_schedule = (TableLayout) findViewById(R.id.tb_schedule);
            for (int i = 0; i < resultSet.getCount(); i++) {
                TableRow row = new TableRow(this);
                TextView cell = new TextView(this);
                cell.setId(i);
                cell.setClickable(true);
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoSubjectElarn(v);
                    }
                });
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                cell.setPadding(0, 8, 0, 8);
                if ((i % 2) == 1) {
                    cell.setBackgroundColor(Color.parseColor("#E6E6E6"));
                }
                String result = "";
                result += "  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_code"));
                result += " ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_name"));
                result += "\n  Time:  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_start_time"));
                result += " - ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_end_time"));
                cell.setText(result);
                cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(cell);
                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT, 1f));
                tb_schedule.addView(row);
                resultSet.moveToNext();
            }
            mydatabase.close();
            Log.i("Initial","Initial get schedule success");
        }else{
            Log.i("Initial","Initial empty schedule");
        }
    }

    protected void refreshSchedule(View v){
        Calendar calendar = Calendar.getInstance();
        int nowDayfoweek = calendar.get(Calendar.DAY_OF_WEEK)-1;
        nextday = nowDayfoweek;
        TableLayout tb_schedule = (TableLayout) findViewById(R.id.tb_schedule);
        tb_schedule.removeAllViews();
        getSchedule();
    }

    protected void nextSchedule(View v){
        Log.i("Initial", "Initial get next schedule...");
        Calendar calendar = Calendar.getInstance();
        if (nextday == 0) {
            nextday = (calendar.get(Calendar.DAY_OF_WEEK) - 1);
        }
        nextday++;
        if (nextday > 7) {
            nextday = 1;
        }
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        final Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule WHERE day_id='"+nextday+"' ORDER BY day_id ASC;",null);
        resultSet.moveToFirst();
        TextView title_day = (TextView) findViewById(R.id.title_day);
        switch (nextday) {
            case 0 : title_day.setText("Sunday");
                break;
            case 1 : title_day.setText("Monday");
                break;
            case 2 : title_day.setText("Tuesday");
                break;
            case 3 : title_day.setText("Wednesday");
                break;
            case 4 : title_day.setText("Thursday");
                break;
            case 5 : title_day.setText("Friday");
                break;
            case 6 : title_day.setText("Saturday");
                break;
        }
        TableLayout tb_schedule = (TableLayout) findViewById(R.id.tb_schedule);
        if (resultSet.getCount() != 0) {
            tb_schedule.removeAllViews();
            for (int i = 0; i < resultSet.getCount(); i++) {
                TableRow row = new TableRow(this);
                TextView cell = new TextView(this);
                cell.setId(i);
                cell.setClickable(true);
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoSubjectElarn(v);
                    }
                });
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                cell.setPadding(0, 8, 0, 8);
                if ((i % 2) == 1) {
                    cell.setBackgroundColor(Color.parseColor("#E6E6E6"));
                }
                String result = "";
                result += "  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_code"));
                result += " ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_name"));
                result += "\n  Time:  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_start_time"));
                result += " - ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_end_time"));
                cell.setText(result);
                cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(cell);
                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT, 1f));
                tb_schedule.addView(row);
                resultSet.moveToNext();
            }
            mydatabase.close();
            Log.i("Initial","Initial get next schedule success");
        }else{
            tb_schedule.removeAllViews();
            Log.i("Initial","Initial empty schedule");
        }
    }

    private void gotoSubjectElarn(View v){
        String subject = ((TextView)v).getText().toString().substring(2,8);
        Intent intent = new Intent(Main.this, Subject_elearn.class);
        intent.putExtra("subject", subject);
        intent.putExtra("from","Main");
        startActivity(intent);
        Log.i("GT","Go to subject elearning list");
    }

    protected void prevSchedule(View v) {
        Log.i("Initial", "Initial get prev schedule...");
        Calendar calendar = Calendar.getInstance();
        if (nextday == 0) {
            nextday = (calendar.get(Calendar.DAY_OF_WEEK) - 1);
        }
        nextday--;
        if (nextday < 1) {
            nextday = 7;
        }
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        final Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule WHERE day_id='"+nextday+"' ORDER BY day_id ASC;",null);
        resultSet.moveToFirst();
        TextView title_day = (TextView) findViewById(R.id.title_day);
        switch (nextday) {
            case 0 : title_day.setText("Sunday");
                break;
            case 1 : title_day.setText("Monday");
                break;
            case 2 : title_day.setText("Tuesday");
                break;
            case 3 : title_day.setText("Wednesday");
                break;
            case 4 : title_day.setText("Thursday");
                break;
            case 5 : title_day.setText("Friday");
                break;
            case 6 : title_day.setText("Saturday");
                break;
        }
        TableLayout tb_schedule = (TableLayout) findViewById(R.id.tb_schedule);
        if (resultSet.getCount() != 0) {
            tb_schedule.removeAllViews();
            for (int i = 0; i < resultSet.getCount(); i++) {
                TableRow row = new TableRow(this);
                TextView cell = new TextView(this);
                cell.setId(i);
                cell.setClickable(true);
                cell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoSubjectElarn(v);
                    }
                });
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                cell.setPadding(0, 8, 0, 8);
                if ((i % 2) == 1) {
                    cell.setBackgroundColor(Color.parseColor("#E6E6E6"));
                }
                String result = "";
                result += "  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_code"));
                result += " ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_name"));
                result += "\n  Time:  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_start_time"));
                result += " - ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_end_time"));
                cell.setText(result);
                cell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(cell);
                row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.MATCH_PARENT, 1f));
                tb_schedule.addView(row);
                resultSet.moveToNext();
            }
            mydatabase.close();
            Log.i("Initial","Initial get next schedule success");
        }else{
            tb_schedule.removeAllViews();
            Log.i("Initial","Initial empty schedule");
        }
    }

    private void onClickNews(View v){
        int idv = v.getId();
        String title = al_title.get(idv).toString();
        String desc = android.text.Html.fromHtml(al_desc.get(idv).toString()).toString();
        Intent intent = new Intent(Main.this, News.class);
        intent.putExtra("from","Main");
        intent.putExtra("title",title);
        intent.putExtra("desc", desc);
        startActivity(intent);
        Log.i("OC","On click news");
    }

    private void scheduleNotification(Notification notification, long delay) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        last_noti_id++;
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, last_noti_id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String title, String content) {
        Intent intent = new Intent(this, Main.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Main.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .build();
        return notification;
    }

    private void clearAlarmNoti(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent updateServiceIntent = new Intent(this, Main.class);
        PendingIntent pendingUpdateIntent = PendingIntent.getService(this, 0, updateServiceIntent, 0);

        // Cancel alarms
        try {
            alarmManager.cancel(pendingUpdateIntent);
            Log.e("ALM", "AlarmManager update was canceled. ");
        } catch (Exception e) {
            Log.e("ALM", "AlarmManager update was not canceled. " + e.toString());
        }
    }

    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo_List.class);
        startActivity(intent);
        Log.i("GT","Go to Todo");
    }

    public void gotoSetting(View v){
        Intent intent = new Intent(this, more_setting.class);
        startActivity(intent);
        Log.i("GT","Go to Setting");
    }

    public void gotoAbout(View v){
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }

    public void gotoHome(View v){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
        Log.i("GT","Go to Home");
    }

    public void gotoNoti(View v){
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
        Log.i("GT","Go to Notification");
    }

    public void gotoElean(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
        Log.i("GT","Go to Elearning");
    }

    public void gotoPageNews(View v){
        Intent intent = new Intent(this, Page_news.class);
        startActivity(intent);
        Log.i("GT","Go to Page News");
    }

}
//สำรหบัดึงหน้าเว็บ RSS ที่เป็นแบบ XML
class HandleXML {
    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    ArrayList al_title = new ArrayList();
    ArrayList al_desc = new ArrayList();

    protected ArrayList getAl_desc(){
        return al_desc;
    }
    protected ArrayList getAl_title(){ return al_title; }

    private void parseXMLAndStoreIt(XmlPullParser myParser) {

        int event;
        String text = null;

        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if(name.equals("title")){
                            al_title.add(text);
                        }else if(name.equals("description")){
                            al_desc.add(text);
                        }
                        else{
                        }
                        break;
                }
                event = myParser.next();
            }
            parsingComplete = false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected HandleXML(String url){
        this.urlString = url;
    }
    protected void fetchXML(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);

                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    parseXMLAndStoreIt(myparser);
                    stream.close();
                }
                catch (Exception e) {
                }
            }
        });
        thread.start();
    }
}