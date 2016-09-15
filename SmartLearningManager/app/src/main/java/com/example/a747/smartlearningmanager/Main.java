package com.example.a747.smartlearningmanager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Main extends AppCompatActivity {

    ArrayList<String> al_desc;
    ArrayList<String> al_title;

    private String finalUrl = "http://www4.sit.kmutt.ac.th/student/bsc_it_feed";
    private HandleXML obj;
    private String std_id;

    private String iniDate;

    private int lastest_news;

    private int last_noti_id;

    private int nextday = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }

        /*Initial*/
        SharedPreferences prefInitial = getApplicationContext().getSharedPreferences("Initial", 0);
        SharedPreferences.Editor editorInitial = prefInitial.edit();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        iniDate = prefInitial.getString("Initial",null);
        if(iniDate == null){
            setProfile();
            setRSS();
            getRSS();
            setNotiSchedule();
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            editorInitial.putString("Initial",df.format(now));
            editorInitial.commit();
        }else{
            try {
                Date past = df.parse(iniDate);
                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();
                long diffDate = now.getDate() - past.getDate();
                if(diffDate > 1){
                    editorInitial.clear();
                    editorInitial.putString("Initial",df.format(now));
                    editorInitial.commit();
                    setProfile();
                    setRSS();
                    getRSS();
                    setNotiSchedule();
                }else{
                    getRSS();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*End initial*/
        getSchedule();
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
                    URL url = new URL("http://54.169.58.93/Profile.php?std_id="+params[0]);
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
                    mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Profile(firstname VARCHAR, lastname VARCHAR, department VARCHAR, grade VARCHAR, email VARCHAR, phonenum VARCHAR, image VARCHAR);");
                    mydatabase.execSQL("INSERT INTO Profile VALUES('"+c.getString("firstname")+"','"+c.getString("lastname")+"','"+c.getString("department")+"','"+c.getString("grade")+"','"+c.getString("email")+"','"+c.getString("phonenum")+"','"+c.getString("image")+"');");
                    mydatabase.close();
                    if (c.getString("department") == "CS") {
                        finalUrl = "http://www4.sit.kmutt.ac.th/student/bsc_cs_feed";
                    }
                    Log.i("Initial","Initial Profile success");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
    }

    private void setRSS (){
        Log.i("Initial","Initial set RSS...");
        obj = new HandleXML(finalUrl);
        obj.fetchXML();
        al_title = obj.getAl_title();
        al_desc = obj.getAl_desc();
        while(obj.parsingComplete);
        SQLiteDatabase mydatabase = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
        mydatabase.execSQL("DROP TABLE IF EXISTS RSS");
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS RSS(title VARCHAR,description VARCHAR);");
        for(int i=0;i<al_title.size();i++){
            mydatabase.execSQL("INSERT INTO RSS VALUES('"+al_title.get(i).toString()+"','"+al_desc.get(i).toString()+"');");
        }
        mydatabase.close();
        Log.i("Initial","Initial set RSS success");
    }

    private void getRSS(){
        Log.i("Initial","Initial get RSS...");
        SQLiteDatabase mydatabase = openOrCreateDatabase("RSS",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("SELECT title, description FROM RSS",null);
        resultSet.moveToFirst();
        resultSet.moveToNext();
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
        lastest_news = 7;
        for(i=0;i<lastest_news;i++) {
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
        //More News
        TableRow row = new TableRow(this);
        TextView title = new TextView(this);
        title.setId(i);
        title.setClickable(true);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMoreNews(v);
            }
        });
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setPadding(500, 20, 0, 50);
        if ((i % 2) == 0) {
            title.setBackgroundColor(Color.parseColor("#E6E6E6"));
        }
        title.setText("More");
        title.setLayoutParams(params1);
        row.addView(title);
        row.setLayoutParams(params2);
        tl_news.addView(row);
        Log.i("Initial","Initial get RSS success");
    }

    private void setNotiSchedule(){
        class GetDataJSON extends AsyncTask<String,Void,String> {
            HttpURLConnection urlConnection = null;
            private String strJSON;
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("http://54.169.58.93/SchOfWeek.php?std_id="+params[0]);
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
                    mydatabase.execSQL("CREATE TABLE IF NOT EXISTS Schedule(subject_code VARCHAR, subject_name VARCHAR, lecturer VARCHAR, subject_room VARCHAR, subject_date VARCHAR, subject_time_start VARCHAR, subject_time_ended VARCHAR);");
                    JSONArray data = new JSONArray(strJSON);
                    Calendar calendar = Calendar.getInstance();
                    Date nDate;
                    int nowDayfoweek = calendar.get(calendar.DAY_OF_WEEK)-1;
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        mydatabase.execSQL("INSERT INTO Schedule VALUES('"+c.getString("subject_code")+"','"+c.getString("subject_name")+"','"+c.getString("lecturer")+"','"+c.getString("subject_room")+"','"+c.getString("subject_date")+"','"+c.getString("subject_time_start")+"','"+c.getString("subject_time_ended")+"');");
                        nDate = calendar.getTime();
                        Date sDate = calendar.getTime();
                        int scheDayofweek = c.getInt("subject_date");
                        int diffDayofweek = scheDayofweek - nowDayfoweek;
                        if(diffDayofweek < 0){
                            sDate.setDate(sDate.getDate()+(diffDayofweek+7));
                            String hmstart = c.getString("subject_time_start");
                            sDate.setHours(Integer.valueOf(hmstart.substring(0, 2)));
                            sDate.setMinutes(Integer.valueOf(hmstart.substring(3,5)));
                            long diffSec = sDate.getTime() - nDate.getTime();
                            scheduleNotification(getNotification(c.getString("subject_code")+" : "+c.getString("subject_name"),
                                    c.getString("subject_room")+" เริ่มเรียนเวลา "+c.getString("subject_time_start")+" จนถึง "+c.getString("subject_time_ended"))
                                    ,diffSec);
                        }else{
                            sDate.setDate(sDate.getDate()+diffDayofweek);
                            String hmstart = c.getString("subject_time_start");
                            sDate.setHours(Integer.valueOf(hmstart.substring(0, 2)));
                            sDate.setMinutes(Integer.valueOf(hmstart.substring(3,5)));
                            long diffSec = sDate.getTime() - nDate.getTime();
                            scheduleNotification(getNotification(c.getString("subject_code")+" : "+c.getString("subject_name"),
                                    c.getString("subject_room")+" เริ่มเรียนเวลา "+c.getString("subject_time_start")+" จนถึง "+c.getString("subject_time_ended"))
                                    ,diffSec);
                        }
                    }
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
        int nowDayfoweek = calendar.get(calendar.DAY_OF_WEEK)-1;
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule WHERE subject_date='"+nowDayfoweek+"' ORDER BY subject_date ASC LIMIT 2;",null);
        resultSet.moveToFirst();
        TextView tv_scheduleToday;
        TextView title_day = (TextView) findViewById(R.id.title_day);
        switch (nowDayfoweek){
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
            case 7 : title_day.setText("Sunday");
                break;
        }
        for(int i = 0; i<resultSet.getCount(); i++) {
            int findViewId = i+1;
            TextView tv_scheduleToday_code2 = (TextView) findViewById(R.id.tv_scheduleToday_code2);
            if(findViewId == 1){
                tv_scheduleToday_code2.setVisibility(View.GONE);
            }else{
                tv_scheduleToday_code2.setVisibility(View.VISIBLE);
            }
            String name = "tv_scheduleToday_code"+findViewId;
            String result = "";
            int id = getResources().getIdentifier(name, "id", getPackageName());

            if (id != 0) {
                result += "  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_code"));
                result += " ";
            }
            result += resultSet.getString(resultSet.getColumnIndex("subject_name"));
            result += "\t";
            result += "Room:  ";
            result += resultSet.getString(resultSet.getColumnIndex("subject_room"));
            result += "\n  Time:  ";
            result += resultSet.getString(resultSet.getColumnIndex("subject_time_start"));
            result += "\t - ";
            name = "tv_scheduleToday_code"+findViewId;
            id = getResources().getIdentifier(name, "id", getPackageName());
            if(id != 0){
                tv_scheduleToday = (TextView) findViewById(id);
                result+=resultSet.getString(resultSet.getColumnIndex("subject_time_ended"));
                tv_scheduleToday.setText(result);
            }
            resultSet.moveToNext();
        }
        Log.i("Initial","Initial get schedule success");
    }

    protected void refreshSchedule(View v){
        getSchedule();
    }

    protected void nextSchedule(View v) {
        Log.i("Initial", "Initial get next schedule...");
        Calendar calendar = Calendar.getInstance();
        if (nextday == 0) {
            nextday = (calendar.get(calendar.DAY_OF_WEEK) - 1);
        }
        nextday++;
        if (nextday > 7) {
            nextday = 1;
        }
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule WHERE subject_date='" + nextday + "' ORDER BY subject_date ASC LIMIT 2;", null);
        resultSet.moveToFirst();
        TextView tv_scheduleNextday;
        TextView title_day = (TextView) findViewById(R.id.title_day);
        switch (nextday) {
            case 1:
                title_day.setText("Monday");
                break;
            case 2:
                title_day.setText("Tuesday");
                break;
            case 3:
                title_day.setText("Wednesday");
                break;
            case 4:
                title_day.setText("Thursday");
                break;
            case 5:
                title_day.setText("Friday");
                break;
            case 6:
                title_day.setText("Saturday");
                break;
            case 7:
                title_day.setText("Sunday");
                break;
        }
        System.out.println("Get count: "+resultSet.getCount());
        if (resultSet.getCount() != 0) {
            TextView tv_scheduleNextday_code1 = (TextView) findViewById(R.id.tv_scheduleToday_code1);
            tv_scheduleNextday_code1.setVisibility(View.VISIBLE);
            tv_scheduleNextday_code1.setGravity(Gravity.LEFT | Gravity.LEFT);
            for (int i = 0; i < resultSet.getCount(); i++) {
                int findViewId = i + 1;
                TextView tv_scheduleNextday_code2 = (TextView) findViewById(R.id.tv_scheduleToday_code2);
                if (findViewId == 1) {
                    tv_scheduleNextday_code2.setVisibility(View.GONE);
                } else {
                    tv_scheduleNextday_code2.setVisibility(View.VISIBLE);
                }
                String result = "";
                String name = "tv_scheduleToday_code" + findViewId;
                int id = getResources().getIdentifier(name, "id", getPackageName());

                if (id != 0) {
                    result += "  ";
                    result += resultSet.getString(resultSet.getColumnIndex("subject_code"));
                    result += " ";
                }
                result += resultSet.getString(resultSet.getColumnIndex("subject_name"));
                result += "\t";
                result += "Room:  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_room"));
                result += "\n  Time:  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_time_start"));
                result += "\t - ";
                name = "tv_scheduleToday_code" + findViewId;
                id = getResources().getIdentifier(name, "id", getPackageName());
                if (id != 0) {
                    tv_scheduleNextday = (TextView) findViewById(id);
                    result += resultSet.getString(resultSet.getColumnIndex("subject_time_ended"));
                    tv_scheduleNextday.setText(result);
                }
                resultSet.moveToNext();
            }
            Log.i("Initial", "Initial get next schedule success");
        }else{
            TextView tv_scheduleNextday_code1 = (TextView) findViewById(R.id.tv_scheduleToday_code1);
            tv_scheduleNextday_code1.setText("No Class Today");
            tv_scheduleNextday_code1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            TextView tv_scheduleNextday_code2 = (TextView) findViewById(R.id.tv_scheduleToday_code2);
            tv_scheduleNextday_code2.setVisibility(View.GONE);
        }
    }

    protected void prevSchedule(View v) {
        Log.i("Initial", "Initial get next schedule...");
        Calendar calendar = Calendar.getInstance();
        if (nextday == 0) {
            nextday = (calendar.get(calendar.DAY_OF_WEEK) - 1);
        }
        nextday--;
        if (nextday < 1) {
            nextday = 7;
        }
        SQLiteDatabase mydatabase = openOrCreateDatabase("Schedule", MODE_PRIVATE, null);
        Cursor resultSet = mydatabase.rawQuery("SELECT * FROM Schedule WHERE subject_date='" + nextday + "' ORDER BY subject_date ASC LIMIT 2;", null);
        resultSet.moveToFirst();
        TextView tv_scheduleNextday;
        TextView title_day = (TextView) findViewById(R.id.title_day);
        switch (nextday) {
            case 1:
                title_day.setText("Monday");
                break;
            case 2:
                title_day.setText("Tuesday");
                break;
            case 3:
                title_day.setText("Wednesday");
                break;
            case 4:
                title_day.setText("Thursday");
                break;
            case 5:
                title_day.setText("Friday");
                break;
            case 6:
                title_day.setText("Saturday");
                break;
            case 7:
                title_day.setText("Sunday");
                break;
        }
        System.out.println("Get count: "+resultSet.getCount());
        if (resultSet.getCount() != 0) {
            TextView tv_scheduleNextday_code1 = (TextView) findViewById(R.id.tv_scheduleToday_code1);
            tv_scheduleNextday_code1.setVisibility(View.VISIBLE);
            tv_scheduleNextday_code1.setGravity(Gravity.LEFT | Gravity.LEFT);
            for (int i = 0; i < resultSet.getCount(); i++) {
                int findViewId = i + 1;
                TextView tv_scheduleNextday_code2 = (TextView) findViewById(R.id.tv_scheduleToday_code2);
                if (findViewId == 1) {
                    tv_scheduleNextday_code2.setVisibility(View.GONE);
                } else {
                    tv_scheduleNextday_code2.setVisibility(View.VISIBLE);
                }
                String result = "";
                String name = "tv_scheduleToday_code" + findViewId;
                int id = getResources().getIdentifier(name, "id", getPackageName());

                if (id != 0) {
                    result += "  ";
                    result += resultSet.getString(resultSet.getColumnIndex("subject_code"));
                    result += " ";
                }
                result += resultSet.getString(resultSet.getColumnIndex("subject_name"));
                result += "\t";
                result += "Room:  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_room"));
                result += "\n  Time:  ";
                result += resultSet.getString(resultSet.getColumnIndex("subject_time_start"));
                result += "\t - ";
                name = "tv_scheduleToday_code" + findViewId;
                id = getResources().getIdentifier(name, "id", getPackageName());
                if (id != 0) {
                    tv_scheduleNextday = (TextView) findViewById(id);
                    result += resultSet.getString(resultSet.getColumnIndex("subject_time_ended"));
                    tv_scheduleNextday.setText(result);
                }
                resultSet.moveToNext();
            }
            Log.i("Initial", "Initial get next schedule success");
        }else{
            TextView tv_scheduleNextday_code1 = (TextView) findViewById(R.id.tv_scheduleToday_code1);
            tv_scheduleNextday_code1.setText("No Class Today");
            tv_scheduleNextday_code1.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            TextView tv_scheduleNextday_code2 = (TextView) findViewById(R.id.tv_scheduleToday_code2);
            tv_scheduleNextday_code2.setVisibility(View.GONE);
        }
    }

    private void onClickNews(View v){
        int idv = v.getId();
        String title = al_title.get(idv).toString();
        String desc = android.text.Html.fromHtml(al_desc.get(idv).toString()).toString();
        Intent intent = new Intent(Main.this, News.class);
        intent.putExtra("title",title);
        intent.putExtra("desc", desc);
        startActivity(intent);
        Log.i("OC","On click news");
    }
    private void onClickMoreNews(View v){
        Log.i("OC","Load more news...");
        TableRow.LayoutParams params1 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams params2 = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableLayout tl_news = (TableLayout) findViewById(R.id.tl_news);
        TextView remove_more_news = (TextView) findViewById(v.getId());
        remove_more_news.setVisibility(v.GONE);
        for(int i=0;i<3;i++) {
            if(lastest_news != al_title.size()) {
                TableRow row = new TableRow(this);
                TextView title = new TextView(this);
                title.setId(lastest_news);
                title.setClickable(true);
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                title.setPadding(20, 20, 0, 20);
                if ((lastest_news % 2) == 0) {
                    title.setBackgroundColor(Color.parseColor("#E6E6E6"));
                }
                title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickNews(v);
                    }
                });
                title.setText(al_title.get(lastest_news).toString());
                row.addView(title);
                tl_news.addView(row);
                lastest_news++;
            }else{
                break;
            }
        }
        if(lastest_news != al_title.size()) {
            //More News
            TableRow row = new TableRow(this);
            TextView title = new TextView(this);
            title.setId(lastest_news);
            title.setClickable(true);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickMoreNews(v);
                }
            });
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            title.setPadding(500, 20, 0, 50);
            if ((lastest_news % 2) == 0) {
                title.setBackgroundColor(Color.parseColor("#E6E6E6"));
            }
            title.setText("More");
            title.setLayoutParams(params1);
            row.addView(title);
            row.setLayoutParams(params2);
            tl_news.addView(row);
        }
        Log.i("OC","Load more news success");
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