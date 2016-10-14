package com.example.a747.smartlearningmanager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

public class more_setting extends AppCompatActivity{
    RadioButton sound;
    RadioButton vibrate;
    RadioButton silent;
    AudioManager audioManager;
    int last_noti_id;
    String std_id;
    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getApplicationContext().getSharedPreferences("Student", 0);
        std_id = pref.getString("std_id", null);
        if(std_id != null){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.more_setting);
            sound = (RadioButton)findViewById(R.id.soundAndVibrateSwitch);
            vibrate = (RadioButton)findViewById(R.id.vibrateSwitch);
            silent = (RadioButton)findViewById(R.id.silent);
            audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
            LoadSetting();
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        TextView tv_ms_std_id = (TextView) findViewById(R.id.tv_ms_std_id);
        tv_ms_std_id.setText(std_id);
        EditText notiTime = (EditText)findViewById(R.id.notiTime);
        notiTime.setText(""+pref.getInt("notiTime",15));
        notiTime.setSelection(notiTime.length());
    }

    private void LoadSetting(){
            sound.setChecked(pref.getBoolean("soundAndVibrate", true));
            vibrate.setChecked(pref.getBoolean("vibrate", false));
            silent.setChecked(pref.getBoolean("silent",false));
    }

    public void saveNotiTime(View v){
        clearAlarmNoti();
        setNotiSchedule();
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
                    SQLiteDatabase Schedule_db = openOrCreateDatabase("Schedule",MODE_PRIVATE,null);
                    Schedule_db.execSQL("DROP TABLE IF EXISTS Subject");
                    Schedule_db.execSQL("DROP TABLE IF EXISTS Lecturer");
                    Schedule_db.execSQL("DROP TABLE IF EXISTS Subject_Lecturer");
                    Schedule_db.execSQL("CREATE TABLE IF NOT EXISTS Subject(subject_id VARCHAR, subject_code VARCHAR, subject_name VARCHAR, subject_start_time VARCHAR, subject_end_time VARCHAR, day_id INT(2));");
                    Schedule_db.execSQL("CREATE TABLE IF NOT EXISTS Lecturer(lecturer_id VARCHAR, lecturer_prefix VARCHAR,lecturer_fristname VARCHAR, lecturer_lastname VARCHAR, lecturer_email VARCHAR, lecturer_phone VARCHAR, lecturer_image VARCHAR);");
                    Schedule_db.execSQL("CREATE TABLE IF NOT EXISTS Subject_Lecturer(sl_id VARCHAR,subject_id VARCHAR, lecturer_id VARCHAR);");
                    JSONArray data = new JSONArray(strJSON);
                    Calendar calendar = Calendar.getInstance();
                    Date nDate;
                    int nowDayfoweek = calendar.get(Calendar.DAY_OF_WEEK)-1;
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
                        Schedule_db.execSQL("INSERT INTO Subject VALUES('"+c.getString("subject_id")+"','"+c.getString("subject_code")+"','"+c.getString("subject_name")+"','"+c.getString("subject_start_time")+"','"+c.getString("subject_end_time")+"','"+c.getString("day_id")+"');");
                        Schedule_db.execSQL("INSERT INTO Lecturer VALUES('"+c.getString("lecturer_id")+"','"+c.getString("lecturer_prefix")+"','"+c.getString("lecturer_fristname")+"','"+c.getString("lecturer_lastname")+"','"+c.getString("lecturer_email")+"','"+c.getString("lecturer_phone")+"','"+c.getString("lecturer_image")+"');");
                        Schedule_db.execSQL("INSERT INTO Subject_Lecturer VALUES('"+c.getString("subject_lecturer_id")+"','"+c.getString("subject_id")+"','"+c.getString("lecturer_id")+"');");
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
                                        " เริ่มเรียนเวลา " + c.getString("subject_start_time") + " จนถึง " + c.getString("subject_end_time"),sDate.getTime())
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
                                        " เริ่มเรียนเวลา " + c.getString("subject_start_time") + " จนถึง " + c.getString("subject_end_time"),sDate.getTime())
                                        , diffSec);
                            }
                        }
                    }
                    Schedule_db.close();
                    Log.i("Initial","Initial set notification for schedule success");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        new GetDataJSON().execute(std_id);
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

    private Notification getNotification(String title, String content, long time) {
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
                .setWhen(time)
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

    public void gotoProfile(View v){
        saveSetting();
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
    public void gotoTodo(View v){
        saveSetting();
        Intent intent = new Intent(this, Todo_List.class);
        startActivity(intent);
    }

    public void gotoHome(View v){
        saveSetting();
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void gotoNoti(View v){
        saveSetting();
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
    }

    public void gotoElean(View v){
        saveSetting();
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }

    public void gotoAbout(View v){
        saveSetting();
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }
    public void gotopagenews(View v){
        saveSetting();
        Intent intent = new Intent(this, Page_news.class);
        startActivity(intent);
    }

    private void saveSetting(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("soundAndVibrate",sound.isChecked());
        editor.putBoolean("vibrate",vibrate.isChecked());
        editor.putBoolean("silent",silent.isChecked());
        editor.commit();
        finish();
        if(sound.isChecked()){
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        }
        else if(vibrate.isChecked()){
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        }else{
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    public void logout(View v){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("Initial", 0);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.commit();

        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {

        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();

            SharedPreferences pref2 = getApplicationContext().getSharedPreferences("Initial", 0);
            SharedPreferences.Editor editor2 = pref2.edit();
            editor2.clear();
            editor2.commit();

            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
