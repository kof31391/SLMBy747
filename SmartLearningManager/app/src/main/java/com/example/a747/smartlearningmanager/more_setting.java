package com.example.a747.smartlearningmanager;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;

public class more_setting extends AppCompatActivity{
    private RadioButton sound;
    private RadioButton vibrate;
    private RadioButton silent;
    private AudioManager audioManager;
    private int last_noti_id;
    private String std_id;
    private int NotiBefore = 0;
    private SharedPreferences pref;
    private Dialog dialog;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("INFO", "Loading...");
        intent = new Intent(this, Login.class);
        dialog = new Dialog(this);
        dialog = getDialogLoading();
        dialog.show();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_setting);

        pref = getApplicationContext().getSharedPreferences("Student", 0);
        std_id = pref.getString("std_id", null);
        if(std_id != null){
            if(isNetworkConnected()) {
                sound = (RadioButton) findViewById(R.id.soundAndVibrateSwitch);
                vibrate = (RadioButton) findViewById(R.id.vibrateSwitch);
                silent = (RadioButton) findViewById(R.id.silent);
                audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                LoadSetting();
                setProfile();

                EditText notiTime = (EditText)findViewById(R.id.notiTime);
                NotiBefore = pref.getInt("notiBefore",15);
                notiTime.setText(String.valueOf(NotiBefore));
                notiTime.setSelection(notiTime.length());
            }else{
                Intent intent = new Intent(this, Main.class);
                startActivity(intent);
            }
        }else{
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
            }
        }, 1000);
        Log.i("INFO", "Loading complete");
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private Dialog getDialogLoading(){
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading);
        dialog.setCancelable(true);
        return  dialog;
    }

    public void changeNotiSchedule(View v){
        try {
            EditText notiTime = (EditText) findViewById(R.id.notiTime);
            NotiBefore = Integer.parseInt(notiTime.getText().toString());
            clearAlarmNoti();
            setNotiSchedule();
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("notiBefore",NotiBefore);
            editor.commit();
            Toast.makeText(getApplicationContext(), "Save Settings",
                    Toast.LENGTH_LONG).show();
            saveSetting();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setProfile(){
        Log.i("Initial","Initial Profile...");
        TextView tv_ms_std_id = (TextView) findViewById(R.id.tv_ms_std_id);
        tv_ms_std_id.setText(std_id);

        Bitmap bitmap = DownloadImage("http://54.169.58.93/student_image/"+std_id+".jpg");
        ImageView iv_std = (ImageView) findViewById(R.id.iv_std);
        iv_std.setImageBitmap(getCroppedBitmap(bitmap));
        Log.i("Initial","Initial Profile success");
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    private InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        return in;
    }

    private Bitmap DownloadImage(String URL) {
        Bitmap bitmap = null;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return bitmap;
    }

    private void LoadSetting(){
            sound.setChecked(pref.getBoolean("soundAndVibrate", true));
            vibrate.setChecked(pref.getBoolean("vibrate", false));
            silent.setChecked(pref.getBoolean("silent",false));
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
                    JSONArray data = new JSONArray(strJSON);
                    Calendar calendar = Calendar.getInstance();
                    Date nDate;
                    int nowDayfoweek = calendar.get(Calendar.DAY_OF_WEEK)-1;
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);
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
                                        " start " + c.getString("subject_start_time") + " until " + c.getString("subject_end_time"),sDate.getTime())
                                        , (diffSec-(NotiBefore*1000)));
                            }
                        }else{
                            sDate.setDate(sDate.getDate()+diffDayofweek);
                            String hmstart = c.getString("subject_start_time");
                            sDate.setHours(Integer.valueOf(hmstart.substring(0, 2)));
                            sDate.setMinutes(Integer.valueOf(hmstart.substring(3,5)));
                            long diffSec = sDate.getTime() - nDate.getTime();
                            if(diffSec>0) {
                                scheduleNotification(getNotification(c.getString("subject_code") + " : " + c.getString("subject_name"),
                                        " start " + c.getString("subject_start_time") + " until " + c.getString("subject_end_time"),sDate.getTime())
                                        , (diffSec-(NotiBefore*1000)));
                            }
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
        Intent intent = new Intent(this, Profile.class);
        startActivity(intent);
    }
    public void gotoTodo(View v){
        Intent intent = new Intent(this, Todo_List.class);
        startActivity(intent);
    }

    public void gotoHome(View v){
        Intent intent = new Intent(this, Main.class);
        startActivity(intent);
    }

    public void gotoNoti(View v){
        Intent intent = new Intent(this, Noti.class);
        startActivity(intent);
    }

    public void gotoElean(View v){
        Intent intent = new Intent(this, Elearning.class);
        startActivity(intent);
    }

    public void gotoAbout(View v){
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
    }
    public void gotopagenews(View v){
        Intent intent = new Intent(this, Page_news.class);
        startActivity(intent);
    }

    private void saveSetting(){
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("soundAndVibrate",sound.isChecked());
        editor.putBoolean("vibrate",vibrate.isChecked());
        editor.putBoolean("silent",silent.isChecked());
        editor.commit();
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
        new AlertDialog.Builder(more_setting.this)
                .setTitle("Logout confirm ?")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("Student", 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.clear();
                        editor.commit();

                        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("Initial", 0);
                        SharedPreferences.Editor editor2 = pref2.edit();
                        editor2.clear();
                        editor2.commit();

                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            Intent setIntent = new Intent(this, Main.class);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
